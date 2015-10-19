name := "PcapStream"

version := "1.0"

scalaVersion := "2.10.3"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.2"

libraryDependencies += "org.apache.spark" % "spark-streaming_2.10" % "1.4.1"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.4.1"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.3"

libraryDependencies += "com.fasterxml.jackson.module" % "jackson-module-scala_2.10" % "2.6.1"

scalacOptions += "-deprecation"


