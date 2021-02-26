#!/bin/bash

#
# This script setups the runtime environmental variables necessary to
# run downstream type inference.
#
# Should be imported first by downstream inference starter script.
# The following environmental variable should be specified in
# downstream starter:
# 1. checker & solver class
# 2. classpath
# 3. other inference arguments if necessary
#

ROOT=$(cd $(dirname "$0")/.. && pwd)
CFI=$ROOT/checker-framework-inference
AFU=$ROOT/annotation-tools/annotation-file-utilities
Z3=$ROOT/z3/bin

export PATH=$AFU/scripts:$Z3:$PATH

