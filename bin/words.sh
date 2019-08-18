#!/usr/bin/env bash

SCRIPTDIR=`dirname $0`
JARDIR=$SCRIPTDIR/../target
JARPATH=$(ls $JARDIR/sftest-*-standalone.jar|head -1)

if [ -z "$1" ]; then
    java -cp $JARPATH sftest.strings
else
    cat $1| java -cp $JARPATH sftest.strings
fi
