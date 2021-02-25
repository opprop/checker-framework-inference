#!/bin/bash

#
# This script setups the runtime environmental variables necessary to
# run type inference.
#
# Should be executed first by downstream inference starter script.
# The following environmental variable are required to be specified in
# downstream starter:
# 1. checker & solver class
# 2. classpath
#

ROOT=$(cd $(dirname "$0")/.. && pwd)
CFI=$ROOT/checker-framework-inference
AFU=$ROOT/annotation-tools/annotation-file-utilities
export PATH=$AFU/scripts:$PATH

