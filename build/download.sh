#!/usr/bin/env bash
# Resolve a dependency and copy the jar to the current dir

DEP=$(echo $1 | sed s/\:/\ /g -)
DEST=${2:-${DRIVERS_DIR:-$(pwd)}}
CLASSIFIER=$3
echo "Saving $DEP to $DEST"

grape install $DEP $CLASSIFIER
cp $(grape resolve $DEP${CLASSIFIER:+"-$CLASSIFIER"}) $DEST
# test -z $CLASSIFIER && \
#   cp $(grape resolve $DEP) $DEST || \
#   cp $(grape resolve $DEP)-$CLASSIFIER $DEST || \
