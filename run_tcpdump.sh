#!/bin/bash -ex

# This works on Mac OS, probably not on linux.
IF=$(netstat -rn | grep default | head -1 | tr -s " " | cut -f 6 -d " ")
FILE_LIMIT=30
TIME_LIMIT=60
CAPTURE_PATH="$HOME/pcap/%Y/%m/%d/dump-%H%M%S"

# Create captures in a directory hierarchy for the date. Lets us find
# captures by date later.
sudo tcpdump -v -i $IF -w "$CAPTURE_PATH" -G $TIME_LIMIT -W $FILE_LIMIT -s0
