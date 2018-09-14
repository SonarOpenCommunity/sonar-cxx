# Sonar C++ Plugin (Community)
# Copyright (C) 2010-2018 SonarOpenCommunity
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
import textwrap

from utils_createrules import CDATA
from utils_createrules import write_rules_xml
from utils_createrules import get_cdata_capable_xml_etree

SEVERITY_TO_SONARQUBE = {
    "error": {"type": "BUG", "priority": "MAJOR"},
    "warning": {"type": "BUG", "priority": "MINOR"},
    "portability": {"type": "BUG", "priority": "MINOR"},
    "performance": {"type": "BUG", "priority": "MINOR"},
    "style": {"type": "CODE_SMELL", "priority": "MINOR"},
    "information": {"type": "CODE_SMELL", "priority": "MINOR"},
}

CWE_MAP = None
et = get_cdata_capable_xml_etree()


def message_with_cwe_reference(msg, cwe_nr):
    cwe_msg = CWE_MAP.get(str(cwe_nr), None)
    if cwe_msg is None:
        sys.stderr.write('CWE ID ' + cwe_nr + ' was not found!\n')
    href_text = "CWE-{}".format(
        cwe_nr) if cwe_msg is None else "CWE-{}: {}".format(cwe_nr, cwe_msg)
    msg_wrapped = textwrap.fill(msg)
    return """<p>
{}
</p>
<h2>References</h2>
<p><a href="https://cwe.mitre.org/data/definitions/{}.html" target="_blank">{}</a></p>""".format(msg_wrapped.replace("\\012", "\n"), cwe_nr, href_text)


def error_to_rule(error):
    rule = et.Element('rule')

    errId = error.attrib["id"]
    errMsg = error.attrib["msg"]
    errDetails = error.attrib["verbose"]
    errSeverity = error.attrib["severity"]

    sonarQubeIssueType = SEVERITY_TO_SONARQUBE[errSeverity]["type"]
    sonarQubeIssueSeverity = SEVERITY_TO_SONARQUBE[errSeverity]["priority"]

    et.SubElement(rule, 'key').text = errId
    et.SubElement(rule, 'name').text = errMsg if not errMsg.endswith(
        ".") else errMsg[:-1]

    cweNr = None

    if "cwe" in error.attrib:
        cweNr = error.attrib["cwe"]
    elif errId.endswith("Called") and errSeverity == "style":
        # there is no CWE number, but such checks come from libraries and
        # warn about obsolete or not thread-safe functions
        cweNr = 477

    if cweNr is not None:
        errDetails = message_with_cwe_reference(errDetails, cweNr)

    # encode description tag always as CDATA
    cdata = CDATA(errDetails)
    et.SubElement(rule, 'description').append(cdata)

    if cweNr is not None:
        et.SubElement(rule, 'tag').text = "cwe"
    if sonarQubeIssueType == "BUG":
        et.SubElement(rule, 'tag').text = "bug"

    et.SubElement(rule, 'internalKey').text = errId
    et.SubElement(rule, 'severity').text = sonarQubeIssueSeverity
    et.SubElement(rule, 'type').text = sonarQubeIssueType
    et.SubElement(rule, 'remediationFunction').text = "LINEAR"
    et.SubElement(rule, 'remediationFunctionGapMultiplier').text = "5min"

    return rule


def create_cppcheck_rules(cppcheck_errors):
    rules = et.Element('rules')
    for cppcheck_error in cppcheck_errors:
        rules.append(error_to_rule(cppcheck_error))
    return rules


def load_cwe(path):
    id_to_name = {}

    tree = et.parse(path)
    cwe_root = tree.getroot()
    ns0 = '{http://cwe.mitre.org/cwe-6}'
    for catalog_tag in cwe_root.iter(ns0 + 'Weakness_Catalog'):
        for weaknesses_tag in catalog_tag.iter(ns0 + 'Weaknesses'):
            for weakness_tag in weaknesses_tag.iter(ns0 + 'Weakness'):
                id_attr = weakness_tag.attrib["ID"]
                name_attr = weakness_tag.attrib["Name"]
                id_to_name[id_attr] = name_attr

        for categories_tag in catalog_tag.iter(ns0 + 'Categories'):
            for category_tag in categories_tag.iter(ns0 + 'Category'):
                id_attr = category_tag.attrib["ID"]
                name_attr = category_tag.attrib["Name"]
                id_to_name[id_attr] = name_attr

    return id_to_name


def print_usage_and_exit():
    script_name = os.path.basename(sys.argv[0])
    print """Usage: %s rules <cwec_vN.N.xml> < cppcheck --errorlist --xml-version=2 --library=<lib0.cfg> --library=<lib1.cfg>
       see generate_cppcheck_resources.sh for more details""" % (script_name)
    sys.exit(1)


def parse_cppcheck_errorlist(f):
    tree = et.parse(f)
    cppcheck_root = tree.getroot()
    cppcheck_errors = []
    for errors_tag in cppcheck_root.iter('errors'):
        for error_tag in errors_tag.iter('error'):
            cppcheck_errors.append(error_tag)
    return cppcheck_errors

if __name__ == "__main__":

    if len(sys.argv) < 3:
        print_usage_and_exit()

    # transform to an other elementtree
    if sys.argv[1] == "rules":
        errors = parse_cppcheck_errorlist(sys.stdin)
        CWE_MAP = load_cwe(sys.argv[2])
        root = create_cppcheck_rules(errors)
        write_rules_xml(root, sys.stdout)
    else:
        print_usage_and_exit()
