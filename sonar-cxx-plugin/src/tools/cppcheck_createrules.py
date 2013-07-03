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

severity_2_priority = {
    "error": "MAJOR",
    "style": "MINOR"
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

# monkey pathing element tree to support CDATA as by 
# Eli Golovinsky, 2008, www.gooli.org
#            
def CDATA(text=None):
    """
    A CDATA element factory function that uses the function itself as the tag
    (based on the Comment factory function in the ElementTree implementation).
    """
    element = et.Element(CDATA)
    element.text = text
    return element

old_ElementTree = et.ElementTree
class ElementTree_CDATA(old_ElementTree):
    def _write(self, file, node, encoding, namespaces):
        if node.tag is CDATA:
            if node.text:
                text = node.text.encode(encoding)
                file.write("<![CDATA[%s]]>" % text)
        else:
            old_ElementTree._write(self, file, node, encoding, namespaces)
et.ElementTree = ElementTree_CDATA
       
def header():
    return '<?xml version="1.0" encoding="ASCII"?>\n'

def error_to_rule(error):
    rule = et.Element('rule')
    
    errId = error.attrib["id"]
    errMsg = error.attrib["msg"]
    et.SubElement(rule, 'key').text = errId
    et.SubElement(rule, 'configkey').text = errId
    et.SubElement(rule, 'name').text = errMsg
    et.SubElement(rule, 'description').text = "\n%s\n" % errMsg
    
    return rule

def error_to_rule_in_profile(error):
    rule = et.Element('rule')
    
    errId = error.attrib["id"]
    errSeverity = error.attrib["severity"]
    et.SubElement(rule, 'repositoryKey').text = "cppcheck"
    et.SubElement(rule, 'key').text = errId
    et.SubElement(rule, 'priority').text = severity_2_priority[errSeverity]
    
    return rule

def createRules(errors, converter):
    rules = et.Element('rules')
    for error in errors:
        rules.append(converter(error))
    return rules

def usage():
    return 'Usage: %s <"rules"|"profile"> < cppcheck --errorlist' % sys.argv[0]

if len(sys.argv) != 2:
    print usage()
    exit()

# transform the std input into an elementtree
tree = et.parse(sys.stdin)
results = tree.getroot()
errors = results.findall("error")

# transform to an other elementtree
root = None
if sys.argv[1] == "rules":
    root = createRules(errors, error_to_rule)
elif sys.argv[1] == "profile":
    rules = createRules(errors, error_to_rule_in_profile)
    root = et.Element('profile')
    et.SubElement(root, 'name').text = "Default C++ Profile"
    et.SubElement(root, 'language').text = "c++"
    root.append(rules)
else:
    print usage()
    exit()

# write the resulting elementtree to the out stream
indent(root)
sys.stdout.write(header())
et.ElementTree(root).write(sys.stdout)
