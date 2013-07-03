#!/bin/bash
reports_directory="rats-reports"
report_filename="rats-result-SAMPLE.xml"
if [ ! -e $reports_directory ]; then
    mkdir $reports_directory
fi
rats -w 3 --xml sources > $report_filename
mv $report_filename $reports_directory
