# Sonar Python Plugin
# Copyright (C) 2011 Waleri Enns
# Author(s) : Waleri Enns
# waleri.enns@gmail.com

# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 3 of the License, or (at your option) any later version.

# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.

# You should have received a copy of the GNU Lesser General Public
# License along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02


#
# Reads cppcheck --errostlist from the stdin and writes XML to the stdout.
# The latter is compatible with sonar rules.xml-schema.
#

import sys
import os
import subprocess
import xml.etree.ElementTree as et
import textwrap

severity_2_sonarqube = {
    "error": {"type": "BUG", "priority": "MAJOR"},
    "warning": {"type": "BUG", "priority": "MINOR"},
    "portability": {"type": "BUG", "priority": "MINOR"},
    "performance": {"type": "BUG", "priority": "MINOR"},
    "style": {"type": "CODE_SMELL", "priority": "MINOR"},
    "information": {"type": "CODE_SMELL", "priority": "MINOR"},
}

CWE_MAP = None

# xml prettifyer
#


def indent(elem, level=0):
    i = "\n" + level * "  "
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = i + "  "
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
        for elem in elem:
            indent(elem, level + 1)
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
    else:
        if level and (not elem.tail or not elem.tail.strip()):
            elem.tail = i

# see https://stackoverflow.com/questions/174890/how-to-output-cdata-using-elementtree


def CDATA(text=None):
    element = et.Element('![CDATA[')
    element.text = text
    return element

et._original_serialize_xml = et._serialize_xml


def _serialize_xml(write, elem, encoding, qnames, namespaces):
    if elem.tag == '![CDATA[':
        write("<%s%s]]>%s" % (elem.tag, elem.text, "" if elem.tail is None else elem.tail))
    else:
        et._original_serialize_xml(write, elem, encoding, qnames, namespaces)

et._serialize_xml = et._serialize['xml'] = _serialize_xml


def header():
    return '<?xml version="1.0" encoding="UTF-8"?>\n'


def message_with_CWE_reference(msg, cwe_nr):
    cwe_msg = CWE_MAP.get(str(cwe_nr), None)
    if cwe_msg is None:
        sys.stderr.write('CWE ID ' + cwe_nr + ' was not found!\n')
    href_text = "CWE-{}".format(cwe_nr) if cwe_msg is None else "CWE-{}: {}".format(cwe_nr, cwe_msg)
    msgWrapped = textwrap.fill(msg)
    return """<p>
{}
</p>
<h2>References</h2>
<p><a href="https://cwe.mitre.org/data/definitions/{}.html" target="_blank">{}</a></p>""".format(msgWrapped.replace("\\012", "\n"), cwe_nr, href_text)


def error_to_rule(error):
    rule = et.Element('rule')

    errId = error.attrib["id"]
    errMsg = error.attrib["msg"]
    errDetails = error.attrib["verbose"]
    errSeverity = error.attrib["severity"]

    sonarQubeIssueType = severity_2_sonarqube[errSeverity]["type"]
    sonarQubeIssueSeverity = severity_2_sonarqube[errSeverity]["priority"]

    et.SubElement(rule, 'key').text = errId
    et.SubElement(rule, 'name').text = errMsg if not errMsg.endswith(".") else errMsg[:-1]

    cweNr = None

    if "cwe" in error.attrib:
        cweNr = error.attrib["cwe"]
    elif errId.endswith("Called") and errSeverity == "style":
        # there is no CWE number, but such checks come from libraries and
        # warn about obsolete or not thread-safe functions
        cweNr = 477

    if cweNr is not None:
        errDetails = message_with_CWE_reference(errDetails, cweNr)

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


def error_to_rule_in_profile(error):
    rule = et.Element('rule')

    errId = error.attrib["id"]
    errSeverity = error.attrib["severity"]
    et.SubElement(rule, 'repositoryKey').text = "cppcheck"
    et.SubElement(rule, 'key').text = errId
    et.SubElement(rule, 'priority').text = severity_2_sonarqube[errSeverity]["priority"]

    return rule


def createRules(errors, converter):
    rules = et.Element('rules')
    for error in errors:
        rules.append(converter(error))
    return rules


def parseRules(path):
    tree = et.parse(path)
    root = tree.getroot()
    keys = []
    keys_to_ruleelement = {}

    for rules_tag in root.iter('rules'):
        for rule_tag in rules_tag.iter('rule'):
            for key_tag in rule_tag.iter('key'):
                keys.append(key_tag.text)
                keys_to_ruleelement[key_tag.text] = rule_tag
    return keys, keys_to_ruleelement


def loadCWE(path):
    id_to_name = {}

    tree = et.parse(path)
    root = tree.getroot()
    ns0 = '{http://cwe.mitre.org/cwe-6}'
    for catalog_tag in root.iter(ns0 + 'Weakness_Catalog'):
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


def makeDescriptionToCDATA(rule_t):
    """after parsing we lost the CDATA information
       restore it for <description> tag, assume it always encoded as CDATA"""
    for description_tag in rule_t.iter('description'):
        desc = description_tag.text
        description_tag.text = ""
        description_tag.append(CDATA(desc))


def call_xmllint(file_path):
    command = ["xmllint", file_path]
    p = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    out, err = p.communicate()
    if p.returncode != 0:
        print "### XMLLINT", file_path
        print "### ERR"
        print err
        print "### OUT"
        print out
        print "\n"
        return True
    return False


def call_tidy(file_path):
    command = ["tidy", file_path]
    p = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    out, err = p.communicate()
    if p.returncode != 0:
        print "### TIDY ", file_path
        print "### ERR"
        print err
        print "### SUGGESTION FOR FIXING"
        print out
        print "\n"
        return True
    return False


def checkRules(path):
    print "### CHECK ", path
    has_xmllint_errors = call_xmllint(path)
    if has_xmllint_errors:
        return 1

    has_tidy_errors = False
    keys, keys_mapping = parseRules(path)
    for key in keys:
        for rule_tag in keys_mapping[key].iter('rule'):
            for description_tag in rule_tag.iter('description'):
                description_dump_path = "/tmp/" + key + ".ruledump"
                with open(description_dump_path, "w") as f:
                    html_start = u"""<!DOCTYPE html>
<html>
  <head>
    <meta charset=\"utf-8\">
    <title>Rule Description</title>
  </head>
  <body>
"""
                    html_stop = u"""
  </body>
</html>"""

                    f.write(html_start)
                    f.write(description_tag.text.encode("UTF-8"))
                    f.write(html_stop)
                is_tidy_error = call_tidy(description_dump_path)
                has_tidy_errors = has_tidy_errors or is_tidy_error

    if has_tidy_errors:
        return 2

    print "no errors found"
    return 0


def compareRules(old_path, new_path):
    old_keys, _ = parseRules(old_path)
    new_keys, new_keys_mapping = parseRules(new_path)
    old_keys_set = set(old_keys)
    new_keys_set = set(new_keys)
    print "# OLD RULE SET vs NEW RULE SET\n"

    print "## OLD RULE SET\n"
    print "nr of xml entries: ", len(old_keys), "\n"
    print "nr of unique rules: ", len(old_keys_set), "\n"
    print "rules which are only in the old rule set:"
    only_in_old = old_keys_set.difference(new_keys_set)
    for key in sorted(only_in_old):
        print "*", key
    print ""

    print "## NEW RULE SET\n"
    print "nr of xml entries: ", len(new_keys), "\n"
    print "nr of unique rules: ", len(new_keys_set), "\n"
    print "rules which are only in the new rule set:"
    only_in_new = new_keys_set.difference(old_keys_set)
    for key in sorted(only_in_new):
        print "*", key
    print ""

    print "## COMMON RULES\n"
    common_keys = old_keys_set.intersection(new_keys_set)
    print "nr of rules: ", len(common_keys), "\n"
    for key in sorted(common_keys):
        print "*", key
    print ""

    print "### NEW RULES WHICH MUST BE ADDED\n"
    print "```XML"
    for key in sorted(only_in_new):
        rule_tag = new_keys_mapping[key]
        indent(rule_tag)
        makeDescriptionToCDATA(rule_tag)
        et.ElementTree(rule_tag).write(sys.stdout)
    print "```\n"

    # create a rule xml from the new rules, where rules are stored in the same
    # order as in the old rule file. this will make xml files comparable by diff/meld etc.
    comparable_rules = et.Element('rules')
    for common_key in old_keys:
        if common_key in new_keys_mapping:
            rule_tag = new_keys_mapping[common_key]
            makeDescriptionToCDATA(rule_tag)
            comparable_rules.append(rule_tag)
    for only_new_key in sorted(only_in_new):
        rule_tag = new_keys_mapping[only_new_key]
        makeDescriptionToCDATA(rule_tag)
        comparable_rules.append(rule_tag)

    with open(new_path + ".comparable", 'w') as f:
        writeXML(comparable_rules, f)

    print """### DIFF EXISTRING vs GENERATED"""
    print "run\n\n"
    print "```bash"
    print "<your favorite diff>", os.path.abspath(old_path), os.path.abspath(new_path) + ".comparable # e.g."
    print "meld", os.path.abspath(old_path), os.path.abspath(new_path) + ".comparable"
    print "```\n"


def usage():
    return """Usage: %s <"rules"|"profile"> < cppcheck --errorlist
       %s comparerules old_cppcheck.xml new_cppcheck.xml
           """ % (sys.argv[0], sys.argv[0])


def parseCppcheckErrorlist(f):
    tree = et.parse(f)
    root = tree.getroot()
    errors = []
    for errors_tag in root.iter('errors'):
        for error_tag in errors_tag.iter('error'):
            errors.append(error_tag)
    return errors


def writeXML(root, f):
    indent(root)
    f.write(header())
    et.ElementTree(root).write(f)

if len(sys.argv) < 2:
    print usage()
    exit()

# transform to an other elementtree
if sys.argv[1] == "rules":
    errors = parseCppcheckErrorlist(sys.stdin)
    CWE_MAP = loadCWE(sys.argv[2])
    root = createRules(errors, error_to_rule)
    writeXML(root, sys.stdout)
elif sys.argv[1] == "profile":
    errors = parseCppcheckErrorlist(sys.stdin)
    rules = createRules(errors, error_to_rule_in_profile)
    root = et.Element('profile')
    et.SubElement(root, 'name').text = "Default C++ Profile"
    et.SubElement(root, 'language').text = "c++"
    root.append(rules)
    writeXML(root, sys.stdout)
elif sys.argv[1] == "comparerules" and len(sys.argv) == 4:
    compareRules(sys.argv[2], sys.argv[3])
elif sys.argv[1] == "check" and len(sys.argv) == 3:
    rc = checkRules(sys.argv[2])
    sys.exit(rc)
else:
    print usage()
    exit()
