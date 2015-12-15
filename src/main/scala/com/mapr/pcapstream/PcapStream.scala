package com.mapr.pcapstream

import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date

import net.ripe.hadoop.pcap.io.reader.NewApiPcapInputFormat
import net.ripe.hadoop.pcap.packet.Packet
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark._

import org.apache.hadoop.io.{LongWritable, ObjectWritable}
import org.apache.hadoop.mapred.FileInputFormat
import org.apache.hadoop.mapred.JobConf

import org.elasticsearch.spark._

object PcapStream {
  case class FlowData(timestampMillis: Long, srcIP: String, dstIP: String, srcPort: Integer, dstPort: Integer, protocol: String, length: Integer, payload: Array[Byte], captureFilename: String)

  case class FlowC(src: String, srcPort: Int, dst: String, dstPort: Int, protocol: String)

  def main(args: Array[String]) {
    val inputPath = args(0)
    val outputPath = args(1)
    val esNodes = args(2)

    val conf = new SparkConf().setAppName("PCAP Streaming Demo")
    conf.set("es.index.auto.create", "true")
    conf.set("es.nodes", esNodes)

    val ssc = new StreamingContext(conf, Seconds(30))
    val sc = ssc.sparkContext
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    import sqlContext.implicits._

    val input = inputPath
    val output = outputPath
    val directoryFormat = new SimpleDateFormat("'flows'/yyyy/MM/dd/HH/mm/ss")
    val indexFormat = new SimpleDateFormat("'telco'.yyyy.MM.dd/'flows'")

    val jobConf = new JobConf(sc.hadoopConfiguration)
    jobConf.setJobName("PCAP Stream Processing")
    FileInputFormat.setInputPaths(jobConf, input)

    val pcapData = ssc.fileStream[LongWritable, ObjectWritable, NewApiPcapInputFormat](directory = input)

    pcapData.map(r => (r._1.get, r._2.get)).foreachRDD(rdd => {
      val flows = rdd.map(p => {
        val packet = p._2 match {
          case pkt: Packet => pkt
          case _ => throw new ClassCastException
        }

        val f = new FlowC(
          src=packet.get(Packet.SRC).asInstanceOf[String],
          srcPort=packet.get(Packet.SRC_PORT).asInstanceOf[Int],
          dst=packet.get(Packet.DST).asInstanceOf[String],
          dstPort=packet.get(Packet.DST_PORT).asInstanceOf[Int],
          protocol=packet.get(Packet.PROTOCOL).asInstanceOf[String])
        (f, 1)
      })

      val flowCounts = flows.reduceByKey((n,m) => n+m)
      flowCounts.cache()

      LogHolder.log.info(s"${rdd.count} packets in $rdd")
      LogHolder.log.info(s"$flowCounts")
      if (rdd.count > 0) {
        flowCounts.foreach(t => {
          LogHolder.log.info(s"Flow: ${t._1} has ${t._2} packets.")
        })

        val date = new Date()
        //val out = Paths.get(outputPath, directoryFormat.format(date)).toString
        //val df = sqlContext.createDataFrame(rdd)
        //df.write.parquet(out)
        val packets = rdd.map(t => t._2.asInstanceOf[Packet])
        // This one's for you, Elasticsearch 1.7.
        val packetsWithMillsecondTimestamps = packets.map(p => {
          val timestamp = (p.get(Packet.TIMESTAMP).asInstanceOf[Long] * 1000) + (p.get(Packet.TIMESTAMP_MICROS).asInstanceOf[Long]/1000)
          p.put("@timestamp", timestamp.asInstanceOf[Object])
          p
        })
        packetsWithMillsecondTimestamps.saveToEs(indexFormat.format(date))
      }
    })

    ssc.start()
    ssc.awaitTermination()
  }

  /*
  def extractFlowData(packet: Packet, filename: Option[String] = Some("")): Option[FlowData] = {
    packet match {
      case t: TCPPacket => Some(new FlowData(t.timestamp, t.src_ip.getHostAddress(), t.dst_ip.getHostAddress(), t.src_port, t.dst_port, "TCP", t.data.length, t.data, filename.get))
      case u: UDPPacket => Some(new FlowData(u.timestamp, u.src_ip.getHostAddress(), u.dst_ip.getHostAddress(), u.src_port, u.dst_port, "UDP", u.data.length, u.data, filename.get))
      case _ => None
    }
  }
  */
}
