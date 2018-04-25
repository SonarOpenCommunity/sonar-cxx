#!/bin/sh

# use cppcheck_createrules.py in order to check the validity of created rules
#
# currently available checks:
#
# 1. xmllint - check if XML file is valid
#
# 2. tidy-html5 - check if rule description is written in valid HTML
#    (see https://github.com/htacg/tidy-html5)
#
# use
#    bash [path/]check_rules.sh
# it will help the script to find the relative directory
#
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

RULES_DIR="$(readlink -f $SCRIPT_DIR/../main/resources/)"
RULES=( "clangsa.xml" "clangtidy.xml" "compiler-gcc.xml" "compiler-vc.xml" \
"cppcheck.xml" "drmemory.xml" "external-rule.xml" "pclint.xml" \
"rats.xml" "valgrind.xml" "vera++.xml" )

declare -i RC_CHECK=0
for RULE in "${RULES[@]}"
do
   ABS_PATH_TO_RULE="${RULES_DIR}/${RULE}"
   BASE_NAME="$(basename $ABS_PATH_TO_RULE)"
   REPORT_PATH="${PWD}/${BASE_NAME}.tidy"
   declare -i RC=0
   $(python ${SCRIPT_DIR}/cppcheck_createrules.py check ${ABS_PATH_TO_RULE} > ${REPORT_PATH})
   RC=$?
   if [[ ${RC} -ne 0 ]]
   then
       echo "[ FAILED ] ${ABS_PATH_TO_RULE}"
       cat ${REPORT_PATH}
   else
       echo "[ PASSED ] ${ABS_PATH_TO_RULE}"
   fi
   RC_CHECK+=${RC}
done

exit ${RC_CHECK}
