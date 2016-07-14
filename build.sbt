name := "PcapStream"

version := "1.0"

scalaVersion := "2.10.4"

retrieveManaged := true

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.3.14"

libraryDependencies += "org.apache.spark" % "spark-streaming_2.10" % "1.5.2"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.5.2"

libraryDependencies += "org.apache.spark" % "spark-sql_2.10" % "1.5.2"

libraryDependencies += "org.elasticsearch" % "elasticsearch-spark_2.10" % "2.2.0-m1"

libraryDependencies += "joda-time" % "joda-time" % "2.9.1"

scalacOptions += "-deprecation"

unmanagedBase := baseDirectory.value / "lib_unmanaged"

