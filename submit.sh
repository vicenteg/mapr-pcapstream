#!/usr/bin/env bash
SPARK_SUBMIT=/opt/mapr/spark/spark-1.4.1/bin/spark-submit
SPARK_MASTER=yarn-cluster
$SPARK_SUBMIT --jars lib_managed/bundles/com.google.guava/guava/guava-14.0.1.jar \
    --master $SPARK_MASTER target/scala-2.10/pcapstream_2.10-1.0.jar \
    --num-executors 4 \
    --driver-memory 4g \
    --executor-memory 2g \
    --executor-cores 1 \
    $@
