SPARK_SUBMIT=/opt/spark-1.5.0-bin-hadoop2.6/bin/spark-submit
SPARK_MASTER=local[1]
$SPARK_SUBMIT --jars lib/jackson-annotations-2.5.0.jar,lib/jackson-core-2.5.3.jar,lib/jackson-module-scala_2.10-2.4.4.jar,lib/jackson-databind-2.5.3.jar,lib/guava-14.0.1.jar,lib/hadoop-pcap-lib-1.2-SNAPSHOT.jar --master $SPARK_MASTER target/scala-2.10/pcapstream_2.10-1.0.jar $@
