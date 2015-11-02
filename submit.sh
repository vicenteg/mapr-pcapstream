#!/usr/bin/env bash

source env.sh

SPARK_SUBMIT=/opt/mapr/spark/spark-1.4.1/bin/spark-submit
SPARK_MASTER=yarn-cluster
$SPARK_SUBMIT \
    --jars lib_managed/jars/org.elasticsearch/elasticsearch-spark_2.10/elasticsearch-spark_2.10-2.2.0-m1.jar \
    --master $SPARK_MASTER \
    target/scala-2.10/pcapstream_2.10-1.0.jar `date +$MFS_CAPTURE_DIR_FORMAT` $MFS_OUTPUT_DIR
