#!/bin/bash -ex

. env.sh

mkdir -p `date +$CAPTURE_DIR_FORMAT`

# Create captures in a directory hierarchy for the date. Lets us find
# captures by date later.
sudo tcpdump -v -i $IF -w "$CAPTURE_PATH_FORMAT" -G $TIME_LIMIT -W $FILE_LIMIT -s0 -Z $USER
