#! /bin/bash

# halt on error
set -e

ant clean
(ant doc &) > /dev/null
(ant test &) > /dev/null
./run-combiner.sh
