package com.mapr.pcapstream


import java.net.InetAddress
import org.apache.hadoop.mapreduce.lib.input.FileSplit
import org.apache.spark.rdd.NewHadoopRDD

import scala.collection.JavaConversions.mapAsScalaMap

import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark._
import org.apache.spark.sql.DataFrame

import org.apache.hadoop.io.{BytesWritable, NullWritable}
import org.apache.hadoop.mapred.FileInputFormat
import org.apache.hadoop.mapred.JobConf

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import com.mapr.sample.WholeFileInputFormat
import edu.gatech.sjpcap._

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._

object PcapStream {
  case class Header(timestampMillis: Long, srcIP: InetAddress, dstIP: InetAddress, srcPort: Integer, dstPort: Integer, protocol: String, length: Integer, captureFilename: String)

  def main(args: Array[String]) {
    val inputPath = args(0)

    val conf = new SparkConf().setAppName("Simple Application")
    //val ssc = new StreamingContext(conf, Seconds(5))
    val sc = new SparkContext(conf)
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    import sqlContext.implicits._

    val input = inputPath

    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    // val jobConf = new JobConf(ssc.sparkContext.hadoopConfiguration)
    val jobConf = new JobConf(sc.hadoopConfiguration)
    jobConf.setJobName("PCAP Stream Processing")
    FileInputFormat.setInputPaths(jobConf, input)
    val pcapBytes = sc.newAPIHadoopRDD(jobConf, classOf[WholeFileInputFormat], classOf[NullWritable], classOf[BytesWritable])

    val hadoopRdd = pcapBytes.asInstanceOf[NewHadoopRDD[NullWritable,BytesWritable]].mapPartitionsWithInputSplit { ( inputSplit, iterator) =>
      val file = inputSplit.asInstanceOf[FileSplit]
      iterator.map { tpl => (file.getPath.toString, tpl._2) }
    }

    //val myStream = ssc.fileStream[LongWritable, ObjectWritable, PcapInputFormat](input)

    val packets = hadoopRdd.flatMap {
        case (key, value) =>
          val pcapParser = new PcapParser()
          pcapParser.openFile(value.getBytes)

          val pi = new PcapIterator(pcapParser)
          for (flowData <- pi.toList if flowData != None)
            yield (key, flowData.get)
    }

    println(packets.count())
    packets.collect map { tpl => println(tpl) }
    //ssc.start()             // Start the computation
    //ssc.awaitTermination()  // Wait for the computation to terminate
  }

  class PcapIterator(pcapParser: PcapParser) extends Iterator[Option[Header]] {
    private var _headerMap: Option[Header] = None

    def next = {
        _headerMap
    }

    def hasNext: Boolean = {
      val packet = pcapParser.getPacket
      if (packet == Packet.EOF)
        _headerMap = None
      else
        _headerMap = headerMatch(packet)
      packet != Packet.EOF
    }
  }

  def headerMatch(packet: Packet): Option[Header] = {
    packet match {
      case t: TCPPacket => Some(new Header(t.timestamp, t.src_ip, t.dst_ip, t.src_port, t.dst_port, "TCP", t.data.length, "foo"))
      case u: UDPPacket => Some(new Header(u.timestamp, u.src_ip, u.dst_ip, u.src_port, u.dst_port, "UDP", u.data.length, "foo"))
      case _ => None
    }
  }

  def headerToJson(mapper: ObjectMapper, header: Header): String = {
    mapper.writeValueAsString(header)
  }
}
