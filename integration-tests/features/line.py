import re

def _build_regexp(multiline_str):
    print multiline_str
    lines = [line for line in multiline_str.split("\n") if line != '']
    print lines
    expr = "|".join(lines)
    print expr
    print re.compile(expr)

lala = """
.*WARN.*cannot find the sources for '#include <iostream>'
.*WARN.*cannot find the sources for '#include <lala>'
"""
_build_regexp(lala)

    
#line = "14:04:45.685 WARN  - [/home/wenns/src/sonar-cxx/integration-tests/testdata/googletest_project/tests/unittests/test_component1.cc:1]: cannot find the sources for '#include <iostream>'"
#pattern = ".*WARN.*cannot find the sources for '#include <iostream>'"

#r = re.compile(pattern)
#print r.match(line)

