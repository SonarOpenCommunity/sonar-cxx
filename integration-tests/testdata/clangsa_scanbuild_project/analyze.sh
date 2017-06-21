#!/bin/bash

make clean
scan-build -plist --intercept-first --analyze-headers -o analyzer_reports make
