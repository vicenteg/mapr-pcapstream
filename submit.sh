#!/usr/bin/env bash

home=$(dirname $0)
source "$home/env.sh"

SPARK_SUBMIT=/opt/mapr/spark/spark-1.5.2/bin/spark-submit
SPARK_MASTER=yarn-cluster
(cd $home;
$SPARK_SUBMIT \
    --jars lib_managed/jars/org.elasticsearch/elasticsearch-spark_2.10/elasticsearch-spark_2.10-2.2.0-m1.jar,lib_managed/bundles/com.google.guava/guava/guava-19.0.jar,lib/hadoop-pcap-lib-1.2-SNAPSHOT.jar \
    --master $SPARK_MASTER \
    --num-executors 4 \
    --executor-cores 2 \
    --executor-memory 4G \
    target/scala-2.10/pcapstream_2.10-1.0.jar `date +$MFS_CAPTURE_DIR_FORMAT` $MFS_OUTPUT_DIR "$ES_HOSTS")
