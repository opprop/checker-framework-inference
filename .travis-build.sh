#!/bin/bash
ROOT=$TRAVIS_BUILD_DIR/..

echo "Entering checker-framework-inference/.travis-build.sh in" `pwd`

# Fail the whole script if any command fails
set -e

export SHELLOPTS

# Optional argument $1 is one of:
#   cfi-tests, downstream
# If it is omitted, this script does everything.
export GROUP=$1
if [[ "${GROUP}" == "" ]]; then
  export GROUP=all
fi

SLUGOWNER=${TRAVIS_REPO_SLUG%/*}
if [[ "$SLUGOWNER" == "" ]]; then
  SLUGOWNER=opprop
fi

if [[ "${GROUP}" != "cfi-tests" && "${GROUP}" != "downstream" && "${GROUP}" != "all" ]]; then
  echo "Bad argument '${GROUP}'; should be omitted or one of: cfi-tests, downstream, all."
  exit 1
fi

. ./.travis-build-without-test.sh

# Test CF Inference
if [[ "${GROUP}" == "cfi-tests" || "${GROUP}" == "all" ]]; then
    ./gradlew testCheckerInferenceScript
    ./gradlew testCheckerInferenceDevScript

    ./gradlew test

    ./gradlew testDataflowExternalSolvers
fi

# Downstream tests
if [[ "${GROUP}" == "downstream" || "${GROUP}" == "all" ]]; then

  # Only perform downstream test in opprop.
  if [[ "${SLUGOWNER}" == "opprop" ]]; then
    # Ontology test
    ONTOLOGY_GIT=https://github.com/opprop/ontology.git
    ONTOLOGY_BRANCH=master
    ONTOLOGY_COMMAND="git clone -b $ONTOLOGY_BRANCH --depth 1 $ONTOLOGY_GIT"
    ONTOLOGY_BUILD="cd ../ontology && gradle build -x test && ./test-ontology.sh"
    echo "Running: (cd .. && $ONTOLOGY_COMMAND)"
    (cd .. && eval $ONTOLOGY_COMMAND)
    echo "... done: (cd .. && $ONTOLOGY_COMMAND)"
    echo "Running: ($ONTOLOGY_BUILD)"
    (eval $ONTOLOGY_BUILD)
    echo "... done: ($ONTOLOGY_BUILD)"



    # # Units test
    # echo "Running: (cd .. && git clone --depth 1 https://github.com/opprop/ontology.git)"
    # (cd .. && git clone --depth 1 https://github.com/opprop/ontology.git)
    # echo "... done: (cd .. && git clone --depth 1 https://github.com/opprop/ontology.git)"
  fi
fi

echo "Exiting checker-framework-inference/.travis-build.sh in" `pwd`
