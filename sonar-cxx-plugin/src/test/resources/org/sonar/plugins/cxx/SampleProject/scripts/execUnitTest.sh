#!/bin/sh -f

EXECUTABLEPATH=".\SAMPLE-PROJECT-TESTS"
REPORTFILEPATH=".\TEST-result-SAMPLE.xml"

if [ $# -ge 1 ]; then
	EXECUTABLEPATH=$1
fi
if [ $# -ge 2 ]; then
	REPORTFILEPATH=$2
fi

# create full report path location
echo "Create directiory `echo "$REPORTFILEPATH" | sed -r "s/(.*\/).*/\1/"`"
mkdir -p "`echo "$REPORTFILEPATH" | sed -r "s/(.*\/).*/\1/"`"


"$EXECUTABLEPATH" -xunitxml -o "$REPORTFILEPATH"
