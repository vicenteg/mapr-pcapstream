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
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.mapred.FileInputFormat
import org.apache.hadoop.mapred.JobConf

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
//import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
// ScalaObjectMapper causes the error here: http://stackoverflow.com/questions/32334517/error-in-running-job-on-spark-1-4-0-with-jackson-module-with-scalaobjectmapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import net.ripe.hadoop.pcap.io.PcapInputFormat
import net.ripe.hadoop.pcap.packet.Packet

object PcapStream {
  def main(args: Array[String]) {
    val inputPath = args(0)

    val conf = new SparkConf().setAppName("Simple Application")
    val sc = new SparkContext(conf)
    val input = inputPath

    val mapper = new ObjectMapper() //with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    // Setup the path for the job vai a Hadoop JobConf
    val jobConf= new JobConf(sc.hadoopConfiguration)
    jobConf.setJobName("Test Scala Job")
    FileInputFormat.setInputPaths(jobConf, input)

    val packetsRDD = sc.hadoopRDD(jobConf, classOf[PcapInputFormat], classOf[LongWritable], classOf[ObjectWritable], 10)
    val serializablePacketsRDD = packetsRDD.map { case (lw, ow) => (lw.get(), ow.get()) }

    serializablePacketsRDD.collect.map { 
        case (key, value) => {
            val packetMap = value.asInstanceOf[Map[String, Object]]
            val m = packetMap.map { case (k,v) => k -> v }
            println(key + "\t" + mapper.writeValueAsString(m))
        }
    }
  }
}
