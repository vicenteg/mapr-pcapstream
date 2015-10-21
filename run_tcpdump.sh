#!/bin/bash

# This works on Mac OS, probably not on linux.
IF=$(netstat -rn | grep default | head -1 | tr -s " " | cut -f 6 -d " ")
LIMIT=30

# Create captures in a directory hierarchy for the date. Lets us find
# captures by date later.
sudo tcpdump -i $IF -w %Y/%m/%d/dump-%H%M%S -G 60 -W $LIMIT -s0
