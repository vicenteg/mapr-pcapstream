#!/usr/bin/env bash

source env.sh

SPARK_SUBMIT=/opt/spark-1.5.1-bin-hadoop2.6/bin/spark-submit
SPARK_MASTER=local[3]
$SPARK_SUBMIT \
    --jars lib_managed/jars/org.elasticsearch/elasticsearch-spark_2.10/elasticsearch-spark_2.10-2.2.0-m1.jar \
    --master $SPARK_MASTER \
    target/scala-2.10/pcapstream_2.10-1.0.jar `date +$NFS_CAPTURE_DIR_FORMAT` $NFS_OUTPUT_DIR localhost
