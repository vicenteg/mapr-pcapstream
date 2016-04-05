#!/usr/bin/env bash

os=$(uname | awk '{print $1}')

if [ $os == "Darwin" ]; then
	# This works on Mac OS, probably not on linux.
	IF=$(netstat -rn | grep default | head -1 | tr -s " " | cut -f 6 -d " ")
elif [ $os == "Linux" ]; then
	# linux
	IF=$(netstat -rn | egrep '^(default|0.0.0.0)' | tr -s ' ' | cut -f 8 -d ' ')
else
	echo "Sorry, only Mac OS or Linux allowed."
	exit 1
fi

FILE_LIMIT=100
TIME_LIMIT=30
SIZE_LIMIT=512

USER=${USER:=ec2-user}
ES_HOSTS=${ES_HOSTS:=localhost}
CLUSTER_NAME=${CLUSTER_NAME:=telco.mapr.com}

MFS_CAPTURE_DIR_FORMAT="/mapr/$CLUSTER_NAME/apps/pcap/in/%Y/%m/%d"
NFS_CAPTURE_DIR_FORMAT="$MFS_CAPTURE_DIR_FORMAT"
MFS_OUTPUT_DIR="/mapr/$CLUSTER_NAME/apps/pcap/out"
NFS_OUTPUT_DIR="$MFS_OUTPUT_DIR"
CAPTURE_FILE_PREFIX=".dump-$(hostname)"
CAPTURE_FILE_FORMAT="$CAPTURE_FILE_PREFIX-%Y-%m-%d_%H_%M_%S.pcap"
NFS_CAPTURE_DIR=$(date +"$NFS_CAPTURE_DIR_FORMAT")
MFS_CAPTURE_DIR=$(date +"$MFS_CAPTURE_DIR_FORMAT")

CAPTURE_PATH_FORMAT="$NFS_CAPTURE_DIR_FORMAT/$CAPTURE_FILE_FORMAT"
CAPTURE_PATH=$(date +"$NFS_CAPTURE_DIR_FORMAT/$CAPTURE_FILE_FORMAT")

