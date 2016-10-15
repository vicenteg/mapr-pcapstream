name := "PcapStream"

version := "1.0"

val sparkVersion = "1.6.1-mapr-1609"
val kafkaVersion = "0.9.0.0-mapr-1607"

scalaVersion := "2.10.5"

retrieveManaged := true

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "MapR Repository" at "http://repository.mapr.com/maven/"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.3.14"

libraryDependencies += "org.apache.spark" % "spark-streaming_2.10" % sparkVersion

libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion

libraryDependencies += "org.apache.spark" % "spark-sql_2.10" % sparkVersion

libraryDependencies += "org.apache.spark" % "spark-streaming-kafka-producer_2.10" % sparkVersion

libraryDependencies += "org.apache.kafka" % "kafka-clients" % kafkaVersion

libraryDependencies += "joda-time" % "joda-time" % "2.9.1"

scalacOptions += "-deprecation"


