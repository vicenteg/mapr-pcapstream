#!/usr/bin/env bash

. env.sh

mkdir -p `date +$CAPTURE_DIR_FORMAT`

# Create captures in a directory hierarchy for the date. Lets us find
# captures by date later.
if [ ! -z $IF ]; then
    sudo -E tcpdump -v -i $IF -w "$CAPTURE_PATH_FORMAT" -G $TIME_LIMIT -W $FILE_LIMIT -C $SIZE_LIMIT -s0 -Z $USER
else
	echo "No interface found."
fi
