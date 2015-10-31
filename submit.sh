#!/usr/bin/env bash
SPARK_SUBMIT=/opt/mapr/spark/spark-1.4.1/bin/spark-submit
SPARK_MASTER=yarn-cluster
$SPARK_SUBMIT \
    --jars lib_managed/jars/org.elasticsearch/elasticsearch-spark_2.10/elasticsearch-spark_2.10-2.2.0-m1.jar \
    --master $SPARK_MASTER \
    --num-executors 4 \
    --driver-memory 4g \
    --executor-memory 2g \
    --executor-cores 1 \
    target/scala-2.10/pcapstream_2.10-1.0.jar $@
