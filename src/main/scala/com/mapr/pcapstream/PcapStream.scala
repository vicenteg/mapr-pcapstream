package com.mapr.pcapstream

import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import org.joda.time._

import net.ripe.hadoop.pcap.io.PcapInputFormat
import net.ripe.hadoop.pcap.packet.Packet
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark._

import org.apache.hadoop.io.{LongWritable, ObjectWritable}
import org.apache.hadoop.mapred.FileInputFormat
import org.apache.hadoop.mapred.JobConf

import org.apache.spark.streaming.kafka.producer._

object PcapStream {
  case class IPFlags(ipFlagsDf: Boolean,
                     ipFlagsMf: Boolean) extends Serializable

  case class TCPFlags(tcpFlagNs: Boolean,
                      tcpFlagCwr: Boolean,
                      tcpFlagEce: Boolean,
                      tcpFlagUrg: Boolean,
                      tcpFlagAck: Boolean,
                      tcpFlagPsh: Boolean,
                      tcpFlagRst: Boolean,
                      tcpFlagSyn: Boolean,
                      tcpFlagFin: Boolean) extends Serializable

  case class PacketSchema(timestamp: Long,
                          year: Int,
                          month: Int,
                          day: Int,
                          hour: Int,
                          minute: Int,
                          src: String,
                          srcPort: Int,
                          dst: String,
                          dstPort: Int,
                          protocol: String,
                          ttl: Int,
                          ipVersion: Int,
                          length: Long,
                          tcpSeq: Long,
                          tcpAck: Long,
                          udpSum: Int,
                          udpLength: Int,
                          reassembledTcpFragments: String,
                          reassembledUdpFragments: String,
                          ipFlags: IPFlags,
                          tcpFlags: TCPFlags) extends Serializable


  def main(args: Array[String]) {
    val inputPath = args(0)
    val outputPath = args(1)
    val interval = args(2).toInt

    val kafkaBrokers = "host:port,host:port"
    val producerConf = new ProducerConf(bootstrapServers = kafkaBrokers.split(",").toList)

    val conf = new SparkConf().setAppName("PcapStreamingDemo")

    val ssc = StreamingContext.getOrCreate("/apps/spark/checkpoints/PcapStreamingDemo", () => {
      new StreamingContext(conf, Seconds(interval))
    })

    val sc = ssc.sparkContext
    val input = inputPath

    val jobConf = new JobConf(sc.hadoopConfiguration)
    jobConf.setJobName("PcapStreamingDemo")
    FileInputFormat.setInputPaths(jobConf, input)

    val pcapData = ssc.fileStream[LongWritable, ObjectWritable, PcapInputFormat](directory = input)

    pcapData.map(r => (r._1.get, r._2.get)).foreachRDD(rdd => {
      val packets = rdd.map(t => {
        t._2 match {
          case p: Packet => p
          case _ => throw new ClassCastException
        }
      })
      val packetSchema = packets.map(packet => {
        val timestamp = (packet.get(Packet.TIMESTAMP).asInstanceOf[Long] * 1000) + (packet.get(Packet.TIMESTAMP_MICROS).asInstanceOf[Long] / 1000)
        val dateTime = new DateTime(timestamp)

        new PacketSchema(
          timestamp = timestamp,
          year = dateTime.year().get,
          month = dateTime.monthOfYear().get,
          day = dateTime.dayOfMonth().get,
          hour = dateTime.hourOfDay().get,
          minute = dateTime.minuteOfHour().get,
          src = packet.get(Packet.SRC).asInstanceOf[String],
          srcPort = packet.get(Packet.SRC_PORT).asInstanceOf[Int],
          dst = packet.get(Packet.DST).asInstanceOf[String],
          dstPort = packet.get(Packet.DST_PORT).asInstanceOf[Int],
          protocol = packet.get(Packet.PROTOCOL).asInstanceOf[String],
          ttl = packet.get(Packet.TTL).asInstanceOf[Int],
          ipVersion = packet.get(Packet.IP_VERSION).asInstanceOf[Int],
          length = packet.get(Packet.LEN).asInstanceOf[Int],
          tcpSeq = packet.get(Packet.TCP_SEQ).asInstanceOf[Long],
          tcpAck = packet.get(Packet.TCP_ACK).asInstanceOf[Long],
          udpSum = packet.get(Packet.UDPSUM).asInstanceOf[Int],
          udpLength = packet.get(Packet.UDP_LENGTH).asInstanceOf[Int],
          reassembledTcpFragments = packet.get(Packet.REASSEMBLED_TCP_FRAGMENTS).asInstanceOf[String],
          reassembledUdpFragments = packet.get(Packet.REASSEMBLED_DATAGRAM_FRAGMENTS).asInstanceOf[String],
          new IPFlags(ipFlagsDf = packet.get(Packet.IP_FLAGS_DF).asInstanceOf[Boolean],
            ipFlagsMf = packet.get(Packet.IP_FLAGS_MF).asInstanceOf[Boolean]),
          new TCPFlags(tcpFlagNs = packet.get(Packet.TCP_FLAG_NS).asInstanceOf[Boolean],
            tcpFlagCwr = packet.get(Packet.TCP_FLAG_CWR).asInstanceOf[Boolean],
            tcpFlagEce = packet.get(Packet.TCP_FLAG_ECE).asInstanceOf[Boolean],
            tcpFlagUrg = packet.get(Packet.TCP_FLAG_URG).asInstanceOf[Boolean],
            tcpFlagAck = packet.get(Packet.TCP_FLAG_ACK).asInstanceOf[Boolean],
            tcpFlagPsh = packet.get(Packet.TCP_FLAG_PSH).asInstanceOf[Boolean],
            tcpFlagRst = packet.get(Packet.TCP_FLAG_RST).asInstanceOf[Boolean],
            tcpFlagFin = packet.get(Packet.TCP_FLAG_FIN).asInstanceOf[Boolean],
            tcpFlagSyn = packet.get(Packet.TCP_FLAG_SYN).asInstanceOf[Boolean]))
      })

      packetSchema.sendToKafka[JsonSerializer]("/apps/pcap/stream:in", producerConf)

      LogHolder.log.info(s"${rdd.count} packets in $rdd")
      LogHolder.log.info(s"${packetSchema.count} packets in $packetSchema")
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
