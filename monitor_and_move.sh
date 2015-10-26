#!/usr/bin/env bash

source env.sh

while true; do
    inotifywait -e create $CAPTURE_DIR
    # get the second most recently created file
    # the most recent is being written to and is 
    # not ready for processing.
    FILENAME_TO_RENAME=$(ls -atr $CAPTURE_DIR | tail -3 | head -1)
    NEW_NAME=$(echo $FILENAME_TO_RENAME | sed -e 's/\.//g')
    if ! $(mv "$CAPTURE_DIR/$FILENAME_TO_RENAME" "$CAPTURE_DIR/$NEW_NAME"); then
        echo "Failed to rename $CAPTURE_DIR/$FILENAME_TO_RENAME"
    fi
    sleep .5
done
