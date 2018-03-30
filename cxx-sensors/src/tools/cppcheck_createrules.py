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
import xml.etree.ElementTree as et

severity_2_sonarqube = {
    "error": {"type": "BUG", "priority": "MAJOR"},
    "warning": {"type": "BUG", "priority": "MINOR"},
    "portability": {"type": "BUG", "priority": "MINOR"},
    "performance": {"type": "BUG", "priority": "MINOR"},
    "style": {"type": "CODE_SMELL", "priority": "MINOR"},
    "information": {"type": "CODE_SMELL", "priority": "MINOR"},
}

# xml prettifyer
#
def indent(elem, level=0):
    i = "\n" + level*"  "
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = i + "  "
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
        for elem in elem:
            indent(elem, level+1)
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
    return '<?xml version="1.0" encoding="us-ascii"?>\n'


def error_to_rule(error):
    rule = et.Element('rule')

    errId = error.attrib["id"]
    errMsg = error.attrib["msg"]
    errSeverity = error.attrib["severity"]
    isCWE = "cwe" in error.attrib

    et.SubElement(rule, 'key').text = errId
    et.SubElement(rule, 'name').text = errMsg

    # encode description tag always as CDATA
    cdata = CDATA(errMsg)
    et.SubElement(rule, 'description').append(cdata)

    if isCWE:
        et.SubElement(rule, 'tag').text = "cwe"

    et.SubElement(rule, 'internalKey').text = errId
    et.SubElement(rule, 'severity').text = severity_2_sonarqube[errSeverity]["priority"]
    et.SubElement(rule, 'type').text = severity_2_sonarqube[errSeverity]["type"]
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
    entries = 0
    keys = set()
    keys_to_ruleelement = {}

    for rules_tag in root.iter('rules'):
        for rule_tag in rules_tag.iter('rule'):
            for key_tag in rule_tag.iter('key'):
                entries = entries + 1
                keys.add(key_tag.text)
                keys_to_ruleelement[key_tag.text] = rule_tag
    return entries, keys, keys_to_ruleelement


def compareRules(old_path, new_path):
    old_entries, old_keys, old_keys_mapping = parseRules(old_path)
    new_entries, new_keys, new_keys_mapping = parseRules(new_path)
    print "# OLD RULE SET vs NEW RULE SET\n"

    print "## OLD RULE SET\n"
    print "nr of xml entries: ", old_entries, "\n"
    print "nr of unique rules: ", len(old_keys), "\n"
    print "rules which are only in the old rule set:"
    only_in_old = old_keys.difference(new_keys)
    for key in sorted(only_in_old):
        print "*", key
    print ""

    print "## NEW RULE SET\n"
    print "nr of xml entries: ", new_entries, "\n"
    print "nr of unique rules: ", len(new_keys), "\n"
    print "rules which are only in the new rule set:"
    only_in_new = new_keys.difference(old_keys)
    for key in sorted(only_in_new):
        print "*", key
    print ""

    print "## COMMON RULES\n"
    common_keys = old_keys.intersection(new_keys)
    print "nr of rules: ", len(common_keys), "\n"
    for key in sorted(common_keys):
        print "*", key
    print ""

    print "### NEW RULES WHICH MUST BE ADDED\n"
    print "```XML"
    for key in only_in_new:
        rule_tag = new_keys_mapping[key]
        indent(rule_tag)
        # after parsing we lost the CDATA information
        # restore it for <description> tag, assume it always encoded as CDATA
        for description_tag in rule_tag.iter('description'):
            desc = description_tag.text
            description_tag.text = ""
            description_tag.append(CDATA(desc))
        et.ElementTree(rule_tag).write(sys.stdout)
    print "```\n"

    # we might have more analysis here: e.g. check if severity didn't change
    # after cppcheck updated its rules


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
else:
    print usage()
    exit()
