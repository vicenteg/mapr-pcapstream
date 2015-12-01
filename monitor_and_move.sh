#!/usr/bin/env bash

trap "echo Bye!; exit 1" SIGINT SIGTERM

home=$(dirname $0)

# inotifywait for Linux
inotifywait=$(which inotifywait 2>/dev/null)

# fswatch for Mac OS (for running spark in local mode for dev)
fswatch=$(which fswatch 2>/dev/null)

if [ -z "$inotifywait" -a -z "$fswatch" ]; then
    echo "Could not find a suitable monitor. Please install inotify-tools (linux) or fswatch (Mac OS)"
    exit 1
fi

if [ ! -z "$inotifywait" ]; then
    notifyprogram=$inotifywait
elif [ ! -z "$fswatch" ]; then
    notifyprogram=$fswatch
fi

while true; do
    # Source the environment each time through the loop to cross
    # from day to day.
    source "$home/env.sh"

    if [ ! -z $inotifywait ]; then
        NOTIFY_ARGS="-q -t 300 -e create $NFS_CAPTURE_DIR"
    elif [ ! -z $fswatch ]; then
        NOTIFY_ARGS="-1 --event Created -x $NFS_CAPTURE_DIR"
    fi

    if [ -d $NFS_CAPTURE_DIR ]; then
        $notifyprogram $NOTIFY_ARGS
        # get the second most recently created file
        # the most recent is being written to and is 
        # not ready for processing. Filter out . and ..
	if [ ! $? -eq 0 ]; then
            exit -1
        fi
        FILENAME_TO_RENAME=$(ls -atr $NFS_CAPTURE_DIR | egrep "$CAPTURE_FILE_PREFIX" | egrep -v '^\.{1,2}$' | tail -2 | head -1)
        NEW_NAME=$(echo $FILENAME_TO_RENAME | sed -e 's/\.//')
        if ! [ $FILENAME_TO_RENAME == $NEW_NAME ]; then
            echo "renaming $FILENAME_TO_RENAME to $NEW_NAME"
            if ! $(mv "$NFS_CAPTURE_DIR/$FILENAME_TO_RENAME" "$NFS_CAPTURE_DIR/$NEW_NAME"); then
                echo "Failed to rename $NFS_CAPTURE_DIR/$FILENAME_TO_RENAME"
            fi
        fi
    else
        echo "waiting for $NFS_CAPTURE_DIR to exist."
    fi
    sleep .5
done
