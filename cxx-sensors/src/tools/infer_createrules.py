# Sonar C++ Plugin (Community)
# Copyright (C) 2010-2019 SonarOpenCommunity
# http://github.com/SonarOpenCommunity/sonar-cxx
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 3 of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with this program; if not, write to the Free Software Foundation,
# Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

import sys
import os

RULES = """ANALYSIS_STOPS|(disabled by default)
ARRAY_OUT_OF_BOUNDS_L1|(disabled by default)
ARRAY_OUT_OF_BOUNDS_L2|(disabled by default)
ARRAY_OUT_OF_BOUNDS_L3|(disabled by default)
Abduction_case_not_implemented|(enabled by default)
Array_of_pointsto|(enabled by default)
Assert_failure|(enabled by default)
BIABD_USE_AFTER_FREE|(enabled by default)
BUFFER_OVERRUN_L1|(enabled by default)
BUFFER_OVERRUN_L2|(enabled by default)
BUFFER_OVERRUN_L3|(enabled by default)
BUFFER_OVERRUN_L4|(disabled by default)
BUFFER_OVERRUN_L5|(disabled by default)
BUFFER_OVERRUN_R2|(enabled by default)
BUFFER_OVERRUN_S2|(enabled by default)
BUFFER_OVERRUN_T1|(enabled by default)
BUFFER_OVERRUN_U5|(disabled by default)
Bad_footprint|(enabled by default)
CAPTURED_STRONG_SELF|(enabled by default)
CHECKERS_ALLOCATES_MEMORY|(enabled by default)
CHECKERS_ANNOTATION_REACHABILITY_ERROR|(enabled by default)
CHECKERS_CALLS_EXPENSIVE_METHOD|(enabled by default)
CHECKERS_EXPENSIVE_OVERRIDES_UNANNOTATED|(enabled by default)
CHECKERS_FRAGMENT_RETAINS_VIEW|(enabled by default)
CHECKERS_IMMUTABLE_CAST|(enabled by default)
CHECKERS_PRINTF_ARGS|(enabled by default)
CLASS_CAST_EXCEPTION|(disabled by default)
CLASS_LOAD|(enabled by default)
COMPARING_FLOAT_FOR_EQUALITY|(enabled by default)
COMPONENT_FACTORY_FUNCTION|(enabled by default)
COMPONENT_FILE_CYCLOMATIC_COMPLEXITY|(enabled by default)
COMPONENT_FILE_LINE_COUNT|(disabled by default)
COMPONENT_INITIALIZER_WITH_SIDE_EFFECTS|(enabled by default)
COMPONENT_WITH_MULTIPLE_FACTORY_METHODS|(enabled by default)
COMPONENT_WITH_UNCONVENTIONAL_SUPERCLASS|(enabled by default)
CONDITION_ALWAYS_FALSE|(disabled by default)
CONDITION_ALWAYS_TRUE|(disabled by default)
CONSTANT_ADDRESS_DEREFERENCE|(disabled by default)
CREATE_INTENT_FROM_URI|(enabled by default)
CROSS_SITE_SCRIPTING|(enabled by default)
Cannot_star|(enabled by default)
Codequery|(enabled by default)
DANGLING_POINTER_DEREFERENCE|(disabled by default)
DEADLOCK|(enabled by default)
DEAD_STORE|(enabled by default)
DEALLOCATE_STACK_VARIABLE|(enabled by default)
DEALLOCATE_STATIC_MEMORY|(enabled by default)
DEALLOCATION_MISMATCH|(enabled by default)
DIVIDE_BY_ZERO|(disabled by default)
DO_NOT_REPORT|(enabled by default)
EMPTY_VECTOR_ACCESS|(enabled by default)
ERADICATE_BAD_NESTED_CLASS_ANNOTATION|(enabled by default)
ERADICATE_CONDITION_REDUNDANT|(enabled by default)
ERADICATE_CONDITION_REDUNDANT_NONNULL|(enabled by default)
ERADICATE_FIELD_NOT_INITIALIZED|(enabled by default)
ERADICATE_FIELD_NOT_NULLABLE|(enabled by default)
ERADICATE_FIELD_OVER_ANNOTATED|(enabled by default)
ERADICATE_INCONSISTENT_SUBCLASS_PARAMETER_ANNOTATION|(enabled by default)
ERADICATE_INCONSISTENT_SUBCLASS_RETURN_ANNOTATION|(enabled by default)
ERADICATE_META_CLASS_CAN_BE_NULLSAFE|(disabled by default)
ERADICATE_META_CLASS_IS_NULLSAFE|(disabled by default)
ERADICATE_META_CLASS_NEEDS_IMPROVEMENT|(disabled by default)
ERADICATE_NULLABLE_DEREFERENCE|(enabled by default)
ERADICATE_PARAMETER_NOT_NULLABLE|(enabled by default)
ERADICATE_REDUNDANT_NESTED_CLASS_ANNOTATION|(enabled by default)
ERADICATE_RETURN_NOT_NULLABLE|(enabled by default)
ERADICATE_RETURN_OVER_ANNOTATED|(enabled by default)
ERADICATE_UNCHECKED_USAGE_IN_NULLSAFE|(enabled by default)
ERADICATE_UNVETTED_THIRD_PARTY_IN_NULLSAFE|(enabled by default)
EXECUTION_TIME_COMPLEXITY_INCREASE|(enabled by default)
EXECUTION_TIME_COMPLEXITY_INCREASE_UI_THREAD|(enabled by default)
EXECUTION_TIME_UNREACHABLE_AT_EXIT|(disabled by default)
EXPENSIVE_EXECUTION_TIME|(disabled by default)
EXPENSIVE_EXECUTION_TIME_UI_THREAD|(disabled by default)
EXPENSIVE_LOOP_INVARIANT_CALL|(enabled by default)
EXPOSED_INSECURE_INTENT_HANDLING|(enabled by default)
Failure_exe|(enabled by default)
GLOBAL_VARIABLE_INITIALIZED_WITH_FUNCTION_OR_METHOD_CALL|(disabled by default)
GUARDEDBY_VIOLATION|(enabled by default)
IMPURE_FUNCTION|(enabled by default)
INEFFICIENT_KEYSET_ITERATOR|(enabled by default)
INFERBO_ALLOC_IS_BIG|(enabled by default)
INFERBO_ALLOC_IS_NEGATIVE|(enabled by default)
INFERBO_ALLOC_IS_ZERO|(enabled by default)
INFERBO_ALLOC_MAY_BE_BIG|(enabled by default)
INFERBO_ALLOC_MAY_BE_NEGATIVE|(enabled by default)
INFERBO_ALLOC_MAY_BE_TAINTED|(enabled by default)
INFINITE_EXECUTION_TIME|(disabled by default)
INHERENTLY_DANGEROUS_FUNCTION|(enabled by default)
INSECURE_INTENT_HANDLING|(enabled by default)
INTEGER_OVERFLOW_L1|(enabled by default)
INTEGER_OVERFLOW_L2|(enabled by default)
INTEGER_OVERFLOW_L5|(disabled by default)
INTEGER_OVERFLOW_R2|(enabled by default)
INTEGER_OVERFLOW_U5|(disabled by default)
INTERFACE_NOT_THREAD_SAFE|(enabled by default)
INVARIANT_CALL|(disabled by default)
IVAR_NOT_NULL_CHECKED|(enabled by default)
Internal_error|(enabled by default)
JAVASCRIPT_INJECTION|(enabled by default)
LOCKLESS_VIOLATION|(enabled by default)
LOCK_CONSISTENCY_VIOLATION|(enabled by default)
LOGGING_PRIVATE_DATA|(enabled by default)
Leak_after_array_abstraction|(enabled by default)
Leak_in_footprint|(enabled by default)
MEMORY_LEAK|(enabled by default)
MISSING_REQUIRED_PROP|(enabled by default)
MIXED_SELF_WEAKSELF|(enabled by default)
MULTIPLE_WEAKSELF|(enabled by default)
MUTABLE_LOCAL_VARIABLE_IN_COMPONENT_FILE|(enabled by default)
Missing_fld|(enabled by default)
NULLPTR_DEREFERENCE|(disabled by default)
NULL_DEREFERENCE|(enabled by default)
NULL_TEST_AFTER_DEREFERENCE|(disabled by default)
PARAMETER_NOT_NULL_CHECKED|(enabled by default)
POINTER_SIZE_MISMATCH|(enabled by default)
PRECONDITION_NOT_FOUND|(enabled by default)
PRECONDITION_NOT_MET|(enabled by default)
PREMATURE_NIL_TERMINATION_ARGUMENT|(enabled by default)
PULSE_MEMORY_LEAK|(disabled by default)
PURE_FUNCTION|(enabled by default)
QUANDARY_TAINT_ERROR|(enabled by default)
REGISTERED_OBSERVER_BEING_DEALLOCATED|(enabled by default)
RESOURCE_LEAK|(enabled by default)
RETAIN_CYCLE|(enabled by default)
RETURN_EXPRESSION_REQUIRED|(enabled by default)
RETURN_STATEMENT_MISSING|(enabled by default)
RETURN_VALUE_IGNORED|(disabled by default)
SHELL_INJECTION|(enabled by default)
SHELL_INJECTION_RISK|(enabled by default)
SKIP_FUNCTION|(disabled by default)
SKIP_POINTER_DEREFERENCE|(disabled by default)
SQL_INJECTION|(enabled by default)
SQL_INJECTION_RISK|(enabled by default)
STACK_VARIABLE_ADDRESS_ESCAPE|(disabled by default)
STARVATION|(enabled by default)
STATIC_INITIALIZATION_ORDER_FIASCO|(enabled by default)
STRICT_MODE_VIOLATION|(enabled by default)
STRONG_SELF_NOT_CHECKED|(enabled by default)
Symexec_memory_error|(enabled by default)
THREAD_SAFETY_VIOLATION|(enabled by default)
TOPL_ERROR|(enabled by default)
UNARY_MINUS_APPLIED_TO_UNSIGNED_EXPRESSION|(disabled by default)
UNINITIALIZED_VALUE|(enabled by default)
UNREACHABLE_CODE|(enabled by default)
UNTRUSTED_BUFFER_ACCESS|(disabled by default)
UNTRUSTED_DESERIALIZATION|(enabled by default)
UNTRUSTED_DESERIALIZATION_RISK|(enabled by default)
UNTRUSTED_ENVIRONMENT_CHANGE_RISK|(enabled by default)
UNTRUSTED_FILE|(enabled by default)
UNTRUSTED_FILE_RISK|(enabled by default)
UNTRUSTED_HEAP_ALLOCATION|(disabled by default)
UNTRUSTED_INTENT_CREATION|(enabled by default)
UNTRUSTED_URL_RISK|(enabled by default)
UNTRUSTED_VARIABLE_LENGTH_ARRAY|(enabled by default)
USER_CONTROLLED_SQL_RISK|(enabled by default)
USE_AFTER_DELETE|(enabled by default)
USE_AFTER_FREE|(enabled by default)
USE_AFTER_LIFETIME|(enabled by default)
Unknown_proc|(enabled by default)
VECTOR_INVALIDATION|(enabled by default)
WEAK_SELF_IN_NO_ESCAPE_BLOCK|(enabled by default)
Wrong_argument_number|(enabled by default)"""


TEMPLATE_BEGINNING = """<?xml version="1.0" encoding="us-ascii"?>
<rules>
  <rule>
    <key>CustomRuleTemplate</key>
    <cardinality>MULTIPLE</cardinality>
    <name><![CDATA[Template for custom Custom rules]]></name>
    <description>
      <![CDATA[
<p>
Follow these steps to make your custom Custom rules available in SonarQube:
</p>

<ol>
  <ol>
    <li>Create a new rule in SonarQube by "copying" this rule template and specify the <code>CheckId</code> of your custom rule, a title, a description, and a default severity.</li>
    <li>Enable the newly created rule in your quality profile.</li>
    <li>Relaunch an analysis on your projects, et voila, your custom rules are executed!</li>
  </ol>
</ol>
]]>
    </description>
    <severity>MAJOR</severity>
  </rule>"""


TEMPLATE_END = """
</rules>
"""


TEMPLATE_RULE = """
  <rule>
    <key>%s</key>
    <name>%s</name>
    <description>
      <![CDATA[
%s
]]>
    </description>
    <tag>bug</tag>
    <tag>infer</tag>
    <internalKey>%s</internalKey>
    <severity>CRITICAL</severity>
    <type>BUG</type>
    <remediationFunction>LINEAR</remediationFunction>
    <remediationFunctionGapMultiplier>5min</remediationFunctionGapMultiplier>
  </rule>"""


def print_usage_and_exit():
    script_name = os.path.basename(sys.argv[0])
    print("""Usage: %s """ % (script_name))
    sys.exit(1)


def create_infer_rules(rules):
    with open('infer.xml', 'w') as output:
        output.write(TEMPLATE_BEGINNING)
        for rule in RULES.split('\n'):
            key = rule.split('|')[0]
            desc = rule.replace('|', ' ')
            output.write(TEMPLATE_RULE % (key, key, desc, key))
        output.write(TEMPLATE_END)


if __name__ == "__main__":

    if len(sys.argv) > 1:
        print_usage_and_exit()

    create_infer_rules(RULES)
