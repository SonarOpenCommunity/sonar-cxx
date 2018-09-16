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
import subprocess

from utils_createrules import CDATA
from utils_createrules import write_rules_xml
from utils_createrules import get_cdata_capable_xml_etree

et = get_cdata_capable_xml_etree()


def rstfile_to_description(path, filename):
    html = subprocess.check_output(
        ['pandoc', path, '--no-highlight', '-f', 'rst', '-t', 'html5'])
    footer = """<h2>References</h2>
<p><a href="http://clang.llvm.org/extra/clang-tidy/checks/%s.html" target="_blank">clang.llvm.org</a></p>""" % (filename)

    return html + footer


def rstfile_to_rule(path):
    rule = et.Element('rule')

    filename_with_extension = os.path.basename(path)
    filename = os.path.splitext(filename_with_extension)[0]

    key = filename
    name = filename
    description = rstfile_to_description(path, filename)

    default_issue_type = "CODE_SMELL"
    default_issue_severity = "MAJOR"

    et.SubElement(rule, 'key').text = key
    et.SubElement(rule, 'name').text = name

    cdata = CDATA(description)
    et.SubElement(rule, 'description').append(cdata)
    et.SubElement(rule, 'severity').text = default_issue_severity
    et.SubElement(rule, 'type').text = default_issue_type

    return rule


def rstfiles_to_rules_xml(directory):
    rules = et.Element('rules')
    for subdir, _, files in os.walk(directory):
        for f in files:
            ext = os.path.splitext(f)[-1].lower()
            if ext == ".rst" and f != "list.rst":
                rst_file_path = os.path.join(subdir, f)
                rules.append(rstfile_to_rule(rst_file_path))
    write_rules_xml(rules, sys.stdout)

# 0. install pandoc
# see https://pandoc.org/
#
# 1. download clang-tidy source code:
# cd /tmp
# svn co http://llvm.org/svn/llvm-project/clang-tools-extra/trunk
# cd -
#
# 2. generate the new version of the rules file
# python clangtidy_createrules.py rules /tmp/trunk/docs/clang-tidy/checks > clangtidy_new.xml
#
# 3. compare the new version with the old one
# python utils_createrules.py comparerules clangtidy_new.xml clangtidy.xml
# meld clangtidy.xml clangtidy_new.xml.comparable


def print_usage_and_exit():
    script_name = os.path.basename(sys.argv[0])
    print """Usage: %s rules <path to clang-tidy source directory with RST files>
       see generate_cppcheck_resources.sh for more details""" % (script_name)
    sys.exit(1)


if __name__ == "__main__":

    if len(sys.argv) < 2:
        print_usage_and_exit()

    # transform to an other elementtree
    if sys.argv[1] == "rules":
        rstfiles_to_rules_xml(sys.argv[2])
    else:
        print_usage_and_exit()
