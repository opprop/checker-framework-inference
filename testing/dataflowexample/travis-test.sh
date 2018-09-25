#!/bin/bash

set -e

WORKING_DIR=$(cd $(dirname "$0") && pwd)

JSR308=$(cd $WORKING_DIR/../../../ && pwd)

export AFU=$JSR308/annotation-tools/annotation-file-utilities
export LINGELING=$JSR308/lingeling

export PATH=$LINGELING:$AFU/scripts:$PATH

## Build libs for test
(cd $WORKING_DIR && ant compile-libs)

echo -e "\nRunning DataflowSolver\n"
# test using basic dataflow solver
$WORKING_DIR/runDataflowSolver.sh
$WORKING_DIR/cleanup.sh

echo -e "\nRunning MaxSatSolver\n"
# test using maxsat (internal) solver
$WORKING_DIR/runMaxSatSolver.sh
$WORKING_DIR/cleanup.sh

echo -e "\nRunning LingelingSolver\n"
# test using lingeling (external) solver
$WORKING_DIR/runLingelingSolver.sh
$WORKING_DIR/cleanup.sh
