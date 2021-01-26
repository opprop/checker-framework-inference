#!/bin/bash

set -e

WORKING_DIR=$(cd $(dirname "$0") && pwd)

JSR308=$(cd $WORKING_DIR/../../../ && pwd)

export AFU=$JSR308/annotation-tools/annotation-file-utilities
export LINGELING=$JSR308/checker-framework-inference/lib/lingeling

export PATH=$LINGELING:$AFU/scripts:$PATH

## Build libs for test
(cd $WORKING_DIR && ant compile-libs)

# test using basic RefVal solver
echo -e "\nRunning RefValSolver\n"
$WORKING_DIR/runRefValSolver.sh
$WORKING_DIR/cleanup.sh

# test using maxsat (internal) solver
echo -e "\nRunning MaxSatSolver\n"
$WORKING_DIR/runMaxSatSolver.sh
$WORKING_DIR/cleanup.sh

# test using lingeling (external) solver
echo -e "\nRunning LingelingSolver\n"
$WORKING_DIR/runLingelingSolver.sh
$WORKING_DIR/cleanup.sh
