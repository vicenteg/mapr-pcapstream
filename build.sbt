name := "PcapStream"

version := "1.0"

val sparkVersion = "2.0.2"

scalaVersion := "2.10.5"

retrieveManaged := true

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.3.14"

libraryDependencies += "org.apache.spark" % "spark-streaming_2.10" % sparkVersion

libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion

libraryDependencies += "org.apache.spark" % "spark-sql_2.10" % sparkVersion

// https://mvnrepository.com/artifact/org.elasticsearch/elasticsearch-spark_2.10
libraryDependencies += "org.elasticsearch" % "elasticsearch-spark_2.10" % "2.4.4"

libraryDependencies += "joda-time" % "joda-time" % "2.9.1"

scalacOptions += "-deprecation"

unmanagedBase := baseDirectory.value / "lib_unmanaged"

