#!/usr/bin/env python3

# -*- coding: utf-8 -*-
# SonarQube C++ Community Plugin (cxx plugin)
# Copyright (C) 2025 SonarOpenCommunity
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

"""Python script to read the GCC warnings from the internet."""

import re
import sys
import textwrap
import urllib.request

from bs4 import BeautifulSoup

from utils_createrules import CDATA
from utils_createrules import write_rules_xml
from utils_createrules import get_cdata_capable_xml_etree


GCC_VERSION = "14.2.0"

old_warnings = {'-Whsa':
                    {
                        'name': 'Warn when a function cannot be expanded to HSAIL',
                        'description': '<p>Issue a warning when HSAIL cannot be emitted for the compiled function or OpenMP construct.</p>'
                    },
                '-Wmudflap':
                    {
                        'name': 'Warn about constructs not instrumented by -fmudflap',
                        'description': 'Suppress warnings about constructs that cannot be instrumented by <samp>-fmudflap</samp>'
                    },
                '-Wunsafe-loop-optimizations':
                    {
                        'name': 'Warn if the loop cannot be optimized due to nontrivial assumptions',
                        'description': '<p>Warn if the loop cannot be optimized because the compiler cannot assume anything on the bounds of the loop indices. With <samp>-funsafe-loop-optimizations</samp> warn if the compiler makes such assumptions.</p>'
                    }
               }


et = get_cdata_capable_xml_etree()


def add_template_rules(rules):
    """Add template rule(s) to XML."""

    rule_key = '=CustomRuleTemplate'
    rule_name = 'Rule template for GCC custom rules'
    # pylint: disable=line-too-long
    rule_description = textwrap.dedent("""
        <p>
        Follow these steps to make your custom rules available in SonarQube:
        </p>
        <ol>
          <ol>
            <li>Create a new rule in SonarQube by "copying" this rule template and specify the <code>CheckId</code> of your custom rule, a title, a description, and a default severity.</li>
            <li>Enable the newly created rule in your quality profile</li>
          </ol>
          <li>Relaunch an analysis on your projects, et voilà, your custom rules are executed!</li>
        </ol>"""
    )

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = rule_key
    et.SubElement(rule, 'cardinality').text = "MULTIPLE"
    et.SubElement(rule, 'name').text=rule_name
    et.SubElement(rule, 'description').append(CDATA(rule_description))
    rules.append(rule)


def add_unknown_rule(rules):
    """Add unknown rule to XML."""
    rule_key = 'unknown'
    rule_name = 'Unknown GCC rule'
    rule_description = textwrap.dedent("""
        <p>
        By activating this rule, unknown rules in the reports are not discarded but mapped to this rule. This helps to identify unknown rules, e.g. from newer versions of a tool.
        </p>
        """
    )
    rule_severity = 'INFO'

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = rule_key
    et.SubElement(rule, 'name').text=rule_name
    et.SubElement(rule, 'description').append(CDATA(rule_description))
    et.SubElement(rule, 'severity').text = rule_severity
    rules.append(rule)


def add_default_rule(rules):
    """Add default rule to XML."""
    rule_key = 'default'
    rule_name = 'Default compiler warnings'
    rule_description = 'Default compiler warnings.'

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = rule_key
    et.SubElement(rule, 'name').text=rule_name
    et.SubElement(rule, 'description').text = rule_description
    rules.append(rule)


def create_rules(warnings, rules):
    """Add warnings as rules to XML."""
    for key, data in list(warnings.items()):
        rule = et.Element('rule')

        name = data['name']
        if '  ' in name and key != '-Wwrite-strings':
            # After '  ' too much information appears, except for 'Wwrite-strings'
            name = name.split('  ')[0]
        name = name.removesuffix('.').replace('\\"', '"')

        et.SubElement(rule, 'key').text = key
        et.SubElement(rule, 'name').text = name
        if 'description' in data:
            cdata = CDATA(data['description'])
        else:
            cdata = CDATA(name)
        et.SubElement(rule, 'description').append(cdata)

        rules.append(rule)


def read_warning_html_description(url):
    """Read warning description from provided url."""

    with urllib.request.urlopen(url) as fp:
    #with open("Warning-Options.html") as fp:
        soup = BeautifulSoup(fp, 'html.parser')

    warnings = {}

    second_entry_warnings_found = {'-Wimplicit-fallthrough': False, '-Wstrict-aliasing': False}

    for dl in  soup.body.div.find_all("dl", recursive=False):
        found_warning_name = False
        warning_name = ''
        for child in dl.children:
            if not found_warning_name and child.name == "dt":
                warning_name = child.get_text().strip().split(" ", 1)[0]

                if not warning_name.startswith("-W") or warning_name == '-Wall':
                    continue

                if warning_name.startswith("-Wno-"):
                    warning_name = warning_name[:2] + warning_name[5:]
                elif warning_name == "-Wnopacked-bitfield-compat":
                    # This is both misspelled in the docs, and never used in the diagnostics, which use "note" instead of "warning" anyway
                    # (i.e. the message is "<file>:<line>:<column>: note: offset of packed bit-field '<field>’ has changed in GCC 4.4")
                    # But it's already there, let's keep it.
                    warning_name = "-Wpacked-bitfield-compat"
                warning_name = warning_name.split('=', 1)[0]

                # When a warning is listed twice, usually the first entry has better documentation
                # Some (e.g. -Wendif-labels) are simply duplicated
                if warning_name in warnings.keys():
                    continue

                if warning_name in second_entry_warnings_found:
                    if not second_entry_warnings_found[warning_name]:
                        second_entry_warnings_found[warning_name] = True
                        continue

                found_warning_name = True
            elif found_warning_name and child.name == "dd":
                description = str(child)[4:-6] # There is a better way of removing the <dd></dd>?
                # Remove class attributes from tags
                description = re.sub(r' class="[^"]+"', '', description)

                swappable_prefix = '<p>Do not warn '
                if description.startswith(swappable_prefix):
                    description = f'<p>Warn {description.removeprefix(swappable_prefix)}'

                if warning_name == '-Wsuggest-attribute':
                    for attribute in ['pure', 'const', 'noreturn', 'format', 'cold','malloc','returns_nonnull']:
                        warnings[f"-Wsuggest-attribute={attribute}"] = {'description': description}
                    found_warning_name = False
                    continue

                # -Wextra mainly enables other warnings, but it's also a warning on its own.
                # Let's include only the relevant part of the documentation.
                if warning_name == '-Wextra':
                    description = re.sub(r'.*</div>\n', '', description, flags=re.DOTALL)

                warnings[warning_name] = {'description': description}

                found_warning_name = False

    return warnings


def read_warning_html_descriptions():
    """Collect warning HTML descriptions into a single dictionary"""
    urls = [
        f"https://gcc.gnu.org/onlinedocs/gcc-{GCC_VERSION}/gcc/C_002b_002b-Dialect-Options.html",
        f"https://gcc.gnu.org/onlinedocs/gcc-{GCC_VERSION}/gcc/Warning-Options.html",
        f"https://gcc.gnu.org/onlinedocs/gcc-{GCC_VERSION}/gcc/Static-Analyzer-Options.html",
        f"https://gcc.gnu.org/onlinedocs/gcc-{GCC_VERSION}/gcc/Objective-C-and-Objective-C_002b_002b-Dialect-Options.html"
    ]

    warnings = {}
    for url in urls:
        warnings |= read_warning_html_description(url)

    return warnings


def read_warning_options():
    """Read warnings from HTML pages.

    - root pages are defined in 'urls'
    """

    warnings = {}

    field = 0
    warning_name = ''
    urls = [
        f'https://gcc.gnu.org/git/?p=gcc.git;a=blob_plain;f=gcc/common.opt;hb=refs/tags/releases/gcc-{GCC_VERSION}',
        f'https://gcc.gnu.org/git/?p=gcc.git;a=blob_plain;f=gcc/c-family/c.opt;hb=refs/tags/releases/gcc-{GCC_VERSION}',
        f'https://gcc.gnu.org/git/?p=gcc.git;a=blob_plain;f=gcc/analyzer/analyzer.opt;hb=refs/tags/releases/gcc-{GCC_VERSION}',
        f'https://gcc.gnu.org/git/?p=gcc.git;a=blob_plain;f=gcc/config/i386/mingw.opt;hb=refs/tags/releases/gcc-{GCC_VERSION}'
    ]
    for url in urls:
        with urllib.request.urlopen(url) as file:
            found_option = False
            found_warning = False
            for line in file:
                line = line.decode('utf-8')
                # AFAIK the format is not documented. The parsing logic comes from "gcc/opt-gather.awk" in the gcc repo.
                if re.match(r'^[ \t]*(;|$)', line):
                    found_option = False
                    found_warning = False
                    field = 0
                    warning_name = ''
                elif re.match(r'^[^ \t]', line):
                    if not found_option:
                        warning_name = line.strip()
                        if warning_name == 'Waligned-new=':
                            # 'Waligned-new' has a better 'name'
                            continue
                        if warning_name.startswith("Wno-"):
                            warning_name = 'W' + warning_name[4:]
                        if not warning_name.startswith('Wsuggest-attribute'):
                            # The -Wsuggest-attribute=XXX warnings actually appear as such in the compiler output
                            warning_name = warning_name.split('=', 1)[0]
                        found_option = True
                        if warning_name[0] == 'W' and warning_name != 'Wall' and warning_name != 'Wsystem-headers' and f'-{warning_name}' not in warnings:
                            found_warning = True
                    elif found_warning:
                        field = field + 1
                        name = line.strip()
                        if field == 1:
                            tags = name.split(' ')
                            # aligned-new good 'name' is listed under 'Waligned-new', but only in 'Waligned-new=' is tagged as a warning
                            if warning_name != 'Waligned-new' and ('Warning' not in tags or 'Ignore' in tags or any(item.startswith('Alias(') for item in tags)):
                                found_warning = False
                        elif field == 2:
                            if '\t' in name:
                                # Some 'names' first include a "usage" description, followed by a "meaning" description. We only want the meaning.
                                warnings[f'-{warning_name}'] = {'name': name.split('\t', 1)[1]}
                            else:
                                if name == 'Do not warn about using \\"long long\\" when -pedantic.':
                                    name = 'Warn about using "long long"'
                                warnings[f'-{warning_name}'] = {'name': name}
                        else:
                            warnings[f'-{warning_name}']['name'] += ' ' + name
                else:
                    assert(False)

    return warnings

def read_warnings():
    warnings = read_warning_options()
    html_warnings = read_warning_html_descriptions()

    for key in warnings:
        if key in html_warnings:
            warnings[key]['description'] = html_warnings[key]['description']

    return dict(sorted(list(warnings.items())))

def create_xml(warnings):
    """Write rules in XML format to stdout."""
    rules = et.Element('rules')

    add_template_rules(rules)
    add_unknown_rule(rules)
    add_default_rule(rules)
    create_rules(warnings, rules)

    write_rules_xml(rules, sys.stdout)


def main():
    """Main."""
    warnings = read_warnings()
    warnings |= old_warnings
    create_xml(warnings)
    return 0


if __name__ == "__main__":
    sys.exit(main())
