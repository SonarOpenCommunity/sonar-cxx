# C++ Community Plugin (cxx plugin)
# Copyright (C) 2010-2022 SonarOpenCommunity
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
import subprocess
import xml.etree.ElementTree as et

# xml prettifyer
#


def indent(elem, level=0):
    i = "\n" + level * "  "
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = i + "  "
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
        for sub_elem in elem:
            indent(sub_elem, level + 1)
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
    else:
        if level and (not elem.tail or not elem.tail.strip()):
            elem.tail = i

# see
# https://stackoverflow.com/questions/174890/how-to-output-cdata-using-elementtree


def CDATA(text=None):
    element = et.Element('![CDATA[')
    element.text = text
    return element

et._original_serialize_xml = et._serialize_xml


def _serialize_xml_2(write, elem, encoding, qnames, namespaces):
    if elem.tag == '![CDATA[':
        tail = "" if elem.tail is None else elem.tail
        try:
            write("<%s%s]]>%s" % (elem.tag, elem.text, tail))
        except UnicodeEncodeError as e:
            sys.stderr.write("[WARNING] {}\n".format(e))
            sys.stderr.write("{}\n{}\n{}\n".format(elem.tag, elem.text, tail))
            write(("<%s%s]]>%s" % (elem.tag, elem.text.encode('ascii', 'ignore').decode('utf-8'), tail)))

    else:
        et._original_serialize_xml(write, elem, encoding, qnames, namespaces)


def _serialize_xml_3(write, elem, qnames, namespaces, short_empty_elements):
    if elem.tag == '![CDATA[':
        tail = "" if elem.tail is None else elem.tail
        try:
            write("<%s%s]]>%s" % (elem.tag, elem.text, tail))
        except UnicodeEncodeError as e:
            sys.stderr.write("[WARNING] {}\n".format(e))
            sys.stderr.write("{}\n{}\n{}\n".format(elem.tag, elem.text, tail))
            write(("<%s%s]]>%s" % (elem.tag, elem.text.encode('ascii', 'ignore').decode('utf-8'), tail)))

    else:
        et._original_serialize_xml(write, elem, qnames, namespaces, short_empty_elements)


if sys.version_info[0] > 2:
    et._serialize_xml = et._serialize['xml'] = _serialize_xml_3
else:
    et._serialize_xml = et._serialize['xml'] = _serialize_xml_2


def get_cdata_capable_xml_etree():
    return et


def _header():
    return '<?xml version="1.0" encoding="UTF-8"?>\n'


def write_rules_xml(root, f):
    indent(root)
    f.write(_header())
    et.ElementTree(root).write(f, encoding='unicode')
    f.flush()


def parse_rules_xml(path):
    sys.stderr.write("[INFO] parse .xml file {}\n".format(path))
    keys = []
    keys_to_ruleelement = {}    
    try :
        tree = et.parse(path)    
    except et.ParseError as e:
        sys.stderr.write("[ERROR] {}: {}\n".format(path, e))
        return keys, keys_to_ruleelement
    
    root = tree.getroot()
    for rules_tag in root.iter('rules'):
        for rule_tag in rules_tag.iter('rule'):
            for key_tag in rule_tag.iter('key'):
                keys.append(key_tag.text)
                keys_to_ruleelement[key_tag.text] = rule_tag
    return keys, keys_to_ruleelement


def make_rules_description_to_cdata(rule_t):
    """after parsing we lost the CDATA information
       restore it for <description> tag, assume it always encoded as CDATA"""
    for description_tag in rule_t.iter('description'):
        desc = description_tag.text
        description_tag.text = ""
        description_tag.append(CDATA(desc))


def call_xmllint(file_path):
    command = ["xmllint", file_path]
    p = subprocess.Popen(command, stdout=subprocess.PIPE,
                         stderr=subprocess.PIPE)
    out, err = p.communicate()
    if p.returncode != 0:
        print("### XMLLINT", file_path)
        print("### ERR")
        print(err)
        print("### OUT")
        print(out)
        print("\n")
        return True
    return False


def call_tidy(file_path):
    command = ["tidy", file_path]
    p = subprocess.Popen(command, stdout=subprocess.PIPE,
                         stderr=subprocess.PIPE)
    out, err = p.communicate()
    if p.returncode < 0 or p.returncode > 1: # error: not ok, not warning
        print("### TIDY ", file_path)
        with open(file_path, 'r') as f:
            print(f.read())
        print("### ERR")
        print(err)
        print("### SUGGESTION FOR FIXING")
        print(out)
        print("\n")
        return True
    return False


def escape(s):
    # in case it's already escaped
    s = s.replace("&amp;", "&")
    s = s.replace("&lt;", "<")
    s = s.replace("&gt;", ">")
    s = s.replace("&quot;", '"')
    s = s.replace("&", "&amp;") # Must be done first!
    s = s.replace("<", "&lt;")
    s = s.replace(">", "&gt;")
    s = s.replace('"', "&quot;")
    return s

    
def check_rules(path):
    print("### CHECK ", path)
    has_xmllint_errors = call_xmllint(path)
    if has_xmllint_errors:
        return 1

    has_tidy_errors = False
    has_len_errors = False
    keys, keys_mapping = parse_rules_xml(path)
    for key in keys:
        for rule_tag in keys_mapping[key].iter('rule'):
            name_tag = rule_tag.find('name')
            description_tag = rule_tag.find('description')
            if len(name_tag) > 200:
                print("### ERR: <name> too long (max 200)")
                has_len_errors = True             
            description_dump_path = "/tmp/" + key + ".ruledump"
            with open(description_dump_path, "w") as f:
                html = """<!DOCTYPE html>
<html>
  <head>
    <meta charset=\"utf-8\">
    <title>{name}</title>
  </head>
  <body>{description}</body>
</html>
""".format(name=escape(name_tag.text), description=description_tag.text)
                f.write(html)
            is_tidy_error = call_tidy(description_dump_path)
            has_tidy_errors = has_tidy_errors or is_tidy_error

    if has_tidy_errors:
        return 2

    if has_len_errors:
        return 3
        
    print("no errors found")
    return 0


def compare_rules(old_path, new_path):
    old_keys, _ = parse_rules_xml(old_path)
    new_keys, new_keys_mapping = parse_rules_xml(new_path)
    old_keys_set = set(old_keys)
    new_keys_set = set(new_keys)
    print("# OLD RULE SET vs NEW RULE SET\n")

    print("## OLD RULE SET\n")
    print("nr of xml entries: ", len(old_keys), "\n")
    print("nr of unique rules: ", len(old_keys_set), "\n")
    print("rules which are only in the old rule set:")
    only_in_old = old_keys_set.difference(new_keys_set)
    for key in sorted(only_in_old):
        print("*", key)
    print("")

    print("## NEW RULE SET\n")
    print("nr of xml entries: ", len(new_keys), "\n")
    print("nr of unique rules: ", len(new_keys_set), "\n")
    print("rules which are only in the new rule set:")
    only_in_new = new_keys_set.difference(old_keys_set)
    for key in sorted(only_in_new):
        print("*", key)
    print("")

    print("## COMMON RULES\n")
    common_keys = old_keys_set.intersection(new_keys_set)
    print("nr of rules: ", len(common_keys), "\n")
    for key in sorted(common_keys):
        print("*", key)
    print("")

    print("### NEW RULES WHICH MUST BE ADDED\n")
    for key in sorted(only_in_new):
        print("*", key)
    print("")    
    print("```XML")
    for key in sorted(only_in_new):
        rule_tag = new_keys_mapping[key]
        indent(rule_tag)
        make_rules_description_to_cdata(rule_tag)
        et.ElementTree(rule_tag).write(sys.stdout, encoding='unicode')
    print("```\n")

    # create a rule xml from the new rules, where rules are stored in the same
    # order as in the old rule file. this will make xml files comparable by
    # diff/meld etc.
    comparable_rules = et.Element('rules')
    for common_key in old_keys:
        if common_key in new_keys_mapping:
            rule_tag = new_keys_mapping[common_key]
            make_rules_description_to_cdata(rule_tag)
            comparable_rules.append(rule_tag)
    for only_new_key in sorted(only_in_new):
        rule_tag = new_keys_mapping[only_new_key]
        make_rules_description_to_cdata(rule_tag)
        comparable_rules.append(rule_tag)

    with open(new_path + ".comparable", 'w') as f:
        write_rules_xml(comparable_rules, f)

    print("""### DIFF EXISTRING vs GENERATED""")
    print("run\n\n")
    print("```bash")
    print("<your favorite diff>", os.path.abspath(old_path), os.path.abspath(new_path) + ".comparable # e.g.")
    print("meld", os.path.abspath(old_path), os.path.abspath(new_path) + ".comparable")
    print("```\n")


def print_usage_and_exit():
    script_name = os.path.basename(sys.argv[0])
    print("""%s comparerules <old_rules.xml> <new_rules.xml>
%s check <rules.xml>

comparerules: generates <new_rules.xml.comparable> which re-sorts new rules according to the order of old_rules.xml
              call 'diff <old_rules.xml> <new_rules.xml.comparable>' in order to see how the existing <old_rules.xml> must be updated
              also generates the human-readable summary to STDOUT

check:        for each rule in <rules.xml> generate a file "/tmp/<rule_key>.ruledump", which contains the HTML code of rules description
              check each *.ruledump file with tidy in order to validate the HTML description""" % (script_name, script_name))
    sys.exit(1)

if __name__ == "__main__":

    if len(sys.argv) < 2:
        print_usage_and_exit()

    # transform to an other elementtree
    if sys.argv[1] == "comparerules" and len(sys.argv) == 4:
        compare_rules(sys.argv[2], sys.argv[3])
    elif sys.argv[1] == "check" and len(sys.argv) == 3:
        rc = check_rules(sys.argv[2])
        sys.exit(rc)
    else:
        print_usage_and_exit()
