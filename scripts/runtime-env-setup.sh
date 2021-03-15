#!/bin/bash

# This script setups the common runtime environment variables necessary 
# to run type inference, and should be imported firstly by any inference 
# launch script.
#
# The following environment variables should be set separately 
# in the inference launch script:
# 1. checker & solver class
# 2. classpath
# 3. other inference arguments if required

# Get the path of this script
SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# Set ROOT as the home directory of current user
ROOT=$(cd $SCRIPTDIR/../../ && pwd)

# CFI may be used in the outer script, e.g. an inference launch script
CFI=$ROOT/checker-framework-inference

# Executables from AFU and external solvers (z3 and lingeling) are possibly 
# run as commands at runtime, so their paths are added to PATH
AFU=$ROOT/annotation-tools/annotation-file-utilities
Z3=$ROOT/z3/bin
LINGELING=$CFI/lib/lingeling

export PATH=$AFU/scripts:$Z3:$LINGELING:$PATH
