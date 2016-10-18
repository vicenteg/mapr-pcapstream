package com.mapr.pcapstream

import java.util

import org.apache.kafka.common.serialization.{Serializer, StringSerializer}
import com.google.gson.Gson
import com.mapr.pcapstream.PcapStream.PacketSchema

class JsonSerializer extends Serializer[PacketSchema] {
  private val stringSerializer = new StringSerializer
  private val gson = new Gson

  override def configure(configs: util.Map[String, _], isKey: Boolean) =
    stringSerializer.configure(configs, isKey)

  override def serialize(topic: String, data: PacketSchema) =
    stringSerializer.serialize(topic, gson.toJson(data))

  override def close() =
    stringSerializer.close()
}

