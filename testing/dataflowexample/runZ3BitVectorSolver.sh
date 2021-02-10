#!/bin/bash

WORKING_DIR=$(cd $(dirname "$0") && pwd)

JSR308=$(cd $WORKING_DIR/../../../ && pwd)

CFI=$JSR308/checker-framework-inference

Z3=$JSR308/z3/bin
export PATH=$Z3:$PATH

DLJC=$JSR308/do-like-javac

CFI_LIB=$CFI/lib
export DYLD_LIBRARY_PATH=$CFI_LIB
export LD_LIBRARY_PATH=$CFI_LIB

( cd $WORKING_DIR && \
$DLJC/dljc -t inference \
    --guess --crashExit \
    --checker dataflow.DataflowChecker \
    --solver dataflow.solvers.backend.DataflowSolverEngine \
    --solverArgs="solver=Z3" \
    --mode ROUNDTRIP -o $WORKING_DIR/logs \
    -afud $WORKING_DIR/annotated -- ant compile-project )
