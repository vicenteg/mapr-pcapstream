#!/usr/bin/env bash
SPARK_SUBMIT=/opt/mapr/spark/spark-1.4.1/bin/spark-submit
SPARK_MASTER=yarn-cluster
$SPARK_SUBMIT \
    --master $SPARK_MASTER \
    --num-executors 4 \
    --driver-memory 2g \
    --executor-memory 2g \
    --executor-cores 1 \
    target/scala-2.10/pcapstream_2.10-1.0.jar $@
