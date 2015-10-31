#!/usr/bin/env bash

# This works on Mac OS, probably not on linux.
# IF=$(netstat -rn | grep default | head -1 | tr -s " " | cut -f 6 -d " ")

# linux
IF=$(netstat -rn | egrep '^(default|0.0.0.0)' | tr -s ' ' | cut -f 8 -d ' ')
FILE_LIMIT=100
TIME_LIMIT=20
SIZE_LIMIT=100
CAPTURE_DIR_FORMAT="$HOME/pcap/in/%Y/%m/%d"
CAPTURE_FILE_PREFIX=".dump"
CAPTURE_FILE_FORMAT="$CAPTURE_FILE_PREFIX-%H%M%S"
CAPTURE_DIR=$(date +"$CAPTURE_DIR_FORMAT")

CAPTURE_PATH_FORMAT="$CAPTURE_DIR_FORMAT/$CAPTURE_FILE_FORMAT"
CAPTURE_PATH=$(date +"$CAPTURE_DIR_FORMAT/$CAPTURE_FILE_FORMAT")

