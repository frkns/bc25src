#!/bin/bash

# Check if the source directory and destination directory are provided
if [ "$#" -ne 2 ]; then
  echo "Usage: $0 <source_directory> <destination_directory>"
  exit 1
fi

# Define source directory and destination directory
SOURCE_DIR=$1
SOURCE_NAME=$(basename "$SOURCE_DIR")
DEST_DIR=$2

# Copy the source directory to the destination in pwd
cp -r "$SOURCE_DIR" "$DEST_DIR"

# Replace occurrences of the source directory name with the destination directory name in the new directory
find "$DEST_DIR" -type f -exec sed -i "s/${SOURCE_NAME}/$(basename "$DEST_DIR")/g" {} +

rm -r ./"$DEST_DIR"/${SOURCE_NAME}
echo "done."

