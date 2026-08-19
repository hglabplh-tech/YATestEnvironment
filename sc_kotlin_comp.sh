#!/bin/bash
# Check if required arguments are missing
if [ $# -lt 3 ]; then
    echo "Error: Missing arguments."
    echo "Usage: $0 <input_dir_scala> <input_dir_kotlin> <output_dir>"
    exit 1
fi
# Assign the parameters to env
INPUT_PATH_SCALA=$1
INPUT_PATH_KOTLIN=$2
OUTPUT_PATH_BOTH=$3
COMPILE_CLASS_PATH=$(lein classpath | tail -n 1)
RET=$?

# 2. Check if Leiningen successfully resolved the classpath
if [ $RET -ne 0 ] || [ -z "$COMPILE_CLASS_PATH" ]; then
    echo "Error: Failed to retrieve project.clj classpath."
    exit 1
fi
echo "Called $0 with scala in: $INPUT_PATH_SCALA\n //
kotlin in: $INPUT_PATH_KOTLIN\n//
output dir:  $OUTPUT_PATH_BOTH\n //
class path: $COMPILE_CLASS_PATH\n"
pwd
echo "compile scala....";
echo "get files....";
find $INPUT_PATH_SCALA -type f -name "*.scala" > ./test.lst;
echo "compile scala....";
scalac -d $OUTPUT_PATH_BOTH @./test.lst;
RET=$?
echo "Scala compiled with exit code was: $RET"

if [ $RET = 0 ]; then
  echo "Compile Kotlin..."
  kotlinc $INPUT_PATH_KOTLIN -d $OUTPUT_PATH_BOTH -cp $COMPILE_CLASS_PATH;
  RET=$?
  echo "Kotlin compiled with exit code was: $RET"
fi
echo "Exit compile script";
exit $RET