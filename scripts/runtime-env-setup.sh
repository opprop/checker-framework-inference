#!/bin/bash

# This script setups the common runtime environment variables necessary 
# to run type inference.
#
# Should be imported firstly by any inference launch script or from 
# the command line.
#
# The following environment variables should be specified separately 
# in the inference launch script:
# 1. checker & solver class
# 2. classpath
# 3. other inference arguments if necessary

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# Set ROOT as the home directory of current user
ROOT=$(cd $SCRIPTDIR/../../ && pwd)

CFI=$ROOT/checker-framework-inference
AFU=$ROOT/annotation-tools/annotation-file-utilities
Z3=$ROOT/z3/bin
LINGELING=$CFI/lib/lingeling

export PATH=$AFU/scripts:$Z3:$LINGELING:$PATH
