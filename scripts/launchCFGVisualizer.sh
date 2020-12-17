#!/bin/bash

#
# This file simply redirects cfg-related arguments 
# to Checker Framework to generate
#

JSR308=$(cd $(dirname "$0")/../.. && pwd)

CFI=$JSR308/checker-framework-inference

export AFU=$JSR308/annotation-tools/annotation-file-utilities
export PATH=$AFU/scripts:$PATH

DEBUG_SOLVER=checkers.inference.solver.DebugSolver
IS_HACK=true

SOLVER="$DEBUG_SOLVER"
DEBUG_CLASSPATH=""

# SOLVERARGS=collectStatistics=true,writeSolutions=true,noAppend=true
SOLVERARGS=

export CLASSPATH=$CLASSPATH:$DEBUG_CLASSPATH:.

CFI_LIB=$CFI/lib
export DYLD_LIBRARY_PATH=$CFI_LIB
export LD_LIBRARY_PATH=$CFI_LIB
export JAVA_LIBRARY_PATH=$CFI_LIB

# NOTE: ROUNDTRIP mode actually writes out files to annotated, INFER mode only
# performs inference without writing to annotated

if [ -n "$1" ] && [[ "$1" == *Checker ]]; then
    CHECKER=$1
    CFGPATH=$2
    JAVAFILES=${@:3}
else
    echo "Please specify the checker name as the first argument."
    exit
fi

$CFI/scripts/inference-dev --checker "$CHECKER" --solver "$SOLVER" \
                           --cfArgs "-Averbosecfg\ -Aflowdotdir=$CFGPATH" --solverArgs "$SOLVERARGS" \
                           --hacks="$IS_HACK" --logLevel=FINER -m INFER \
                           "$JAVAFILES"
