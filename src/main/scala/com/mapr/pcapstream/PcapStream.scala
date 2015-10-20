package com.mapr.pcapstream

import java.io.File
import java.util.{Map,HashMap}
import scala.collection.JavaConversions.mapAsScalaMap

import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd.HadoopRDD

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.mapred.FileInputFormat
import org.apache.hadoop.mapred.JobConf

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import net.ripe.hadoop.pcap.io.PcapInputFormat
import net.ripe.hadoop.pcap.packet.Packet

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._

object PcapStream {
  def main(args: Array[String]) {
    val inputPath = args(0)

    val conf = new SparkConf().setAppName("Simple Application")
    //val ssc = new StreamingContext(conf, Seconds(5))
    val sc = new SparkContext(conf)
    val input = inputPath

    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    // val jobConf = new JobConf(ssc.sparkContext.hadoopConfiguration)
    val jobConf = new JobConf(sc.hadoopConfiguration)
    jobConf.setJobName("PCAP Stream Processing")
    FileInputFormat.setInputPaths(jobConf, input)
    val packetsRDD = sc.hadoopRDD(jobConf, classOf[PcapInputFormat], classOf[LongWritable], classOf[ObjectWritable], 10)

    //val myStream = ssc.fileStream[LongWritable, ObjectWritable, PcapInputFormat](input)

    val serializablePacketsRDD = packetsRDD.map { case (lw, ow) => (lw.get(), ow.get()) }

    serializablePacketsRDD.collect.foreach {
        case (key, value) => {
            val m = packetToJson(mapper, value)
            println(key + "\t" + m)
        }
    }

    //ssc.start()             // Start the computation
    //ssc.awaitTermination()  // Wait for the computation to terminate
  }

  def packetToJson(mapper: ObjectMapper, packet: Object): String = {
    val packetMap = packet.asInstanceOf[Map[String, Object]]
    mapper.writeValueAsString(packetMap)
  }

}
