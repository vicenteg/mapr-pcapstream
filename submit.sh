#!/usr/bin/env bash

home=$(dirname $0)
source "$home/env.sh"

SPARK_VERSION=${SPARK_VERSION:=1.6.1}
SPARK_SUBMIT=/opt/mapr/spark/spark-$SPARK_VERSION/bin/spark-submit
SPARK_MASTER=yarn-cluster
(cd $home;
$SPARK_SUBMIT \
    --conf yarn.nodemanager.resource.memory-mb="$((64 * 1024))" \
    --conf yarn.nodemanager.resource.cpu-vcores="12" \
    --jars lib_managed/jars/org.elasticsearch/elasticsearch-spark_2.10/elasticsearch-spark_2.10-2.3.3.jar,lib_unmanaged/hadoop-pcap-serde-1.2-SNAPSHOT-jar-with-dependencies.jar,lib_managed/jars/joda-time/joda-time/joda-time-2.9.1.jar \
    --master $SPARK_MASTER \
    --num-executors 15 \
    --executor-cores 3 \
    --executor-memory 6G \
    target/scala-2.10/pcapstream_2.10-1.0.jar `date +$MFS_CAPTURE_DIR_FORMAT` $MFS_OUTPUT_DIR "$ES_HOSTS")
