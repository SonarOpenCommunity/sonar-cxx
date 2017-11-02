#!/usr/bin/env python3
"""
Simple script to generate the rules xml file for sonar-cxx plugin
from the Clang Static Analyzer checkers.

The clang compiler should be available in the PATH.
"""

from xml.dom import minidom

import argparse
import re
import subprocess
import sys
import xml.etree.ElementTree as ET


def CDATA(text=None):
    element = ET.Element('![CDATA[')
    element.text = text
    return element


ET._original_serialize_xml = ET._serialize_xml


def _serialize_xml(write, elem, qnames, namespaces,
                   short_empty_elements, **kwargs):
    if elem.tag == '![CDATA[':
        write("<%s%s]]>" % (elem.tag, elem.text))
        return
    return ET._original_serialize_xml(
        write, elem, qnames, namespaces, short_empty_elements, **kwargs)


ET._serialize_xml = ET._serialize['xml'] = _serialize_xml


def collect_checkers(clangsa_output):
    """
    Parse clang static analyzer output.
    Return the list of checkers and the description.
    """

    checkers_data = {}
    # Checker name and description in one line.
    pattern = re.compile(r'^\s\s(?P<checker_name>\S*)\s*(?P<description>.*)')

    checker_name = None
    for line in clangsa_output.splitlines():
        line = line.decode(encoding='UTF-8')
        if re.match(r'^CHECKERS:', line) or line == '':
            continue
        elif checker_name and not re.match(r'^\s\s\S', line):
            # Collect description for the checker name.
            checkers_data[checker_name] = line.strip()
            checker_name = None
        elif re.match(r'^\s\s\S+$', line.rstrip()):
            # Only checker name is in the line.
            checker_name = line.strip()
        else:
            # Checker name and description is in one line.
            match = pattern.match(line.rstrip())
            if match:
                current = match.groupdict()
                checkers_data[current['checker_name']] = current['description']

    # Filter out debug checkers.
    non_debug = {k: v for k, v in checkers_data.items() if 'debug' not in k}

    return non_debug


def main():

    parser = argparse.ArgumentParser(
            description="""Generate the rules xml file for sonar-cxx
                           plugin from the Clang Static Analyzer checkers.
                           https://clang-analyzer.llvm.org/""",
            usage='%(prog)s -o clangsa.xml')

    parser.add_argument('-o', '--output', dest='output_file', action='store',
                        required=True,
                        help="""Output file to write the xml rules.
                                If the file already exists
                                it will be overwritten.""")

    args = parser.parse_args()

    try:
        clang_version = ['clang', '--version']
        version_info = subprocess.run(clang_version,
                                      stdout=subprocess.PIPE,
                                      check=True).stdout
    except subprocess.CalledProcessError as cpe:
        sys.exit(cpe.returncode)

    # Only the first line is interesting.
    clang_version = version_info.splitlines()[0]

    try:
        clang_checkers = ['clang', '-cc1', '-analyzer-checker-help']
        checkers_output = subprocess.run(clang_checkers,
                                         stdout=subprocess.PIPE,
                                         check=True).stdout

        print("Collecting clang checkers ...", end='')
        checker_data = collect_checkers(checkers_output)

    except subprocess.CalledProcessError as cpe:
        sys.exit(cpe.returncode)

    if not checker_data:
        print("No checkers could be processed.")
        sys.exit(1)

    print(" done.")

    print("Generating rules xml ...", end='')

    # build a tree structure
    rules = ET.Element("rules")
    comment = " C and C++ rules for Clang Static Analyzer. " \
        "https://clang-analyzer.llvm.org/\n" + \
        "Rules list was generated based on " + \
        clang_version.decode() + " "

    rules.append(ET.Comment(comment))

    for checker_name, description in checker_data.items():

        rule = ET.SubElement(rules, "rule")
        key = ET.SubElement(rule, "key")
        name = ET.SubElement(rule, "name")
        desc = ET.SubElement(rule, "description")
        sev = ET.SubElement(rule, "severity")
        c_type = ET.SubElement(rule, "type")

        key.text = checker_name
        name.text = checker_name
        sev.text = "MAJOR"
        c_type.text = "BUG"

        auto_tag = checker_name.split('.')[0]
        tag = ET.SubElement(rule, "tag")
        tag.text = auto_tag.lower()

        cdata = CDATA('\n<p>' + description.strip() +
                      '\n</p>\n <h2>References</h2>'
                      ' <p><a href="https://clang-analyzer.llvm.org/"'
                      ' target="_blank">clang-analyzer.llvm.org</a></p> \n')
        desc.append(cdata)

    xmlstr = minidom.parseString(
            ET.tostring(rules, method='xml')).toprettyxml(indent="  ")

    print(" done.")

    with open(args.output_file, 'w') as out:
        out.write(xmlstr)


if __name__ == '__main__':
    main()
