# C++ Community Plugin (cxx plugin)
# Copyright (C) 2020 SonarOpenCommunity
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

"""
Python script to read the Microsoft warnings from the internet.

Unfortunately the warnings are not uniformly structured,
which is why some manual rework is often necessary.
"""

# pip install beautifulsoup4
# pip install selenium-requests

# pages contains JavaScript: script is using Firefox to create HTML pages
# you have to download and install geckodriver
# from https://github.com/mozilla/geckodriver/releases
# to C:\Program Files\geckodriver

import re
import sys
from bs4 import BeautifulSoup
from bs4.dammit import EntitySubstitution
from selenium import webdriver
from selenium.webdriver.firefox.service import Service

from utils_createrules import CDATA
from utils_createrules import write_rules_xml
from utils_createrules import get_cdata_capable_xml_etree


# URL of overview pages and default values
# - use a URL as start page, where all warnings can be listed and read in the menu.
# - at least 'type' must be defined
# - add more than one 'tag' as a comma separated string: 'tag': 'a,b,c'
URLS = {
    # pylint: disable=line-too-long
    'https://learn.microsoft.com/en-us/cpp/error-messages/compiler-warnings/compiler-warnings-c4000-through-c4199' : {
        'severity': 'INFO',
        'type': 'CODE_SMELL',
        },
    'https://learn.microsoft.com/en-us/cpp/error-messages/compiler-warnings/compiler-warnings-c4200-through-c4399' : {
        'severity': 'INFO',
        'type': 'CODE_SMELL',
        },
    'https://learn.microsoft.com/en-us/cpp/error-messages/compiler-warnings/compiler-warnings-c4400-through-c4599' : {
        'severity': 'INFO',
        'type': 'CODE_SMELL',
        },
    'https://learn.microsoft.com/en-us/cpp/error-messages/compiler-warnings/compiler-warnings-c4600-through-c4799' : {
        'severity': 'INFO',
        'type': 'CODE_SMELL',
        },
    'https://learn.microsoft.com/en-us/cpp/error-messages/compiler-warnings/compiler-warnings-c4800-through-c4999' : {
        'severity': 'INFO',
        'type': 'CODE_SMELL',
        },
    'https://learn.microsoft.com/en-us/cpp/code-quality/code-analysis-for-c-cpp-warnings' : {
        'severity': 'CRITICAL',
        'type': 'CODE_SMELL',
        },
    'https://learn.microsoft.com/en-us/cpp/code-quality/code-analysis-for-cpp-corecheck' : {
        'severity': 'INFO',
        'type': 'CODE_SMELL',
        'tag': 'core-guideline',
        },
}

# special values for rules (overwriting defaults)
# - here the default settings for individual warnings can be overwritten
# - add more than one 'tag' as a comma separated string: 'tag': 'a,b,c'
RULE_MAP = {
    # Compiler warnings C4000 - C5999
    'C4020': {'severity':'MAJOR','type':'BUG'},
    'C4034': {'severity':'MAJOR','type':'BUG'},
    'C4056': {'severity':'MAJOR','type':'BUG'},
    'C4062': {'severity':'MAJOR','type':'BUG'},
    'C4130': {'severity':'MAJOR','type':'BUG'},
    'C4133': {'severity':'MAJOR','type':'BUG'},
    'C4138': {'severity':'MAJOR','type':'BUG'},
    'C4172': {'severity':'MAJOR','type':'BUG'},
    'C4243': {'severity':'MAJOR','type':'BUG'},
    'C4245': {'severity':'MAJOR','type':'BUG'},
    'C4291': {'severity':'MAJOR','type':'BUG'},
    'C4293': {'severity':'MAJOR','type':'BUG'},
    'C4295': {'severity':'MAJOR','type':'BUG'},
    'C4296': {'severity':'MAJOR','type':'BUG'},
    'C4309': {'severity':'MAJOR','type':'BUG'},
    'C4313': {'severity':'MAJOR','type':'BUG'},
    'C4317': {'severity':'MAJOR','type':'BUG'},
    'C4333': {'severity':'MAJOR','type':'BUG'},
    'C4339': {'severity':'MAJOR','type':'BUG'},
    'C4340': {'severity':'MAJOR','type':'BUG'},
    'C4341': {'severity':'MAJOR','type':'BUG'},
    'C4355': {'severity':'MAJOR','type':'BUG'},
    'C4356': {'severity':'MAJOR','type':'BUG'},
    'C4358': {'severity':'MAJOR','type':'BUG'},
    'C4359': {'severity':'MAJOR','type':'BUG'},
    'C4368': {'severity':'MAJOR','type':'BUG'},
    'C4405': {'severity':'MAJOR','type':'BUG'},
    'C4407': {'severity':'MAJOR','type':'BUG'},
    'C4422': {'severity':'MAJOR','type':'BUG'},
    'C4426': {'severity':'MAJOR','type':'BUG'},
    'C4473': {'severity':'MAJOR','type':'BUG'},
    'C4474': {'severity':'MAJOR','type':'BUG'},
    'C4477': {'severity':'MAJOR','type':'BUG'},
    'C4478': {'severity':'MAJOR','type':'BUG'},
    'C4526': {'severity':'MAJOR','type':'BUG'},
    'C4539': {'severity':'MAJOR','type':'BUG'},
    'C4541': {'severity':'MAJOR','type':'BUG'},
    'C4715': {'severity':'MAJOR','type':'BUG'},
    'C4716': {'severity':'MAJOR','type':'BUG'},
    'C4717': {'severity':'MAJOR','type':'BUG'},
    'C4756': {'severity':'MAJOR','type':'BUG'},
    'C4774': {'severity':'MINOR','type':'CODE_SMELL'},
    'C4777': {'severity':'MINOR','type':'CODE_SMELL'},
    # Code analysis for C/C++ warnings
    'C6001': {'severity':'CRITICAL','type':'BUG'},
    'C6011': {'severity':'CRITICAL','type':'BUG'},
    'C6014': {'severity':'BLOCKER','type':'BUG'},
    'C6057': {'severity':'CRITICAL','type':'BUG'},
    'C6063': {'severity':'CRITICAL','type':'BUG'},
    'C6064': {'severity':'CRITICAL','type':'BUG'},
    'C6066': {'severity':'CRITICAL','type':'BUG'},
    'C6101': {'severity':'CRITICAL','type':'BUG'},
    'C6102': {'severity':'CRITICAL','type':'BUG'},
    'C6103': {'severity':'CRITICAL','type':'BUG'},
    'C6200': {'severity':'CRITICAL','type':'BUG'},
    'C6201': {'severity':'CRITICAL','type':'BUG'},
    'C6202': {'severity':'CRITICAL','type':'BUG'},
    'C6203': {'severity':'CRITICAL','type':'BUG'},
    'C6235': {'severity':'CRITICAL','type':'BUG'},
    'C6236': {'severity':'CRITICAL','type':'BUG'},
    'C6237': {'severity':'CRITICAL','type':'BUG'},
    'C6239': {'severity':'CRITICAL','type':'BUG'},
    'C6240': {'severity':'CRITICAL','type':'BUG'},
    'C6260': {'severity':'CRITICAL','type':'BUG'},
    'C6272': {'severity':'CRITICAL','type':'BUG'},
    'C6277': {'severity':'CRITICAL','type':'VULNERABILITY'},
    'C6278': {'severity':'CRITICAL','type':'BUG'},
    'C6279': {'severity':'CRITICAL','type':'BUG'},
    'C6283': {'severity':'CRITICAL','type':'BUG'},
    'C6287': {'severity':'CRITICAL','type':'BUG'},
    'C6294': {'severity':'CRITICAL','type':'BUG'},
    'C6295': {'severity':'CRITICAL','type':'BUG'},
    'C6296': {'severity':'CRITICAL','type':'BUG'},
    'C6299': {'severity':'CRITICAL','type':'BUG'},
    'C6308': {'severity':'CRITICAL','type':'BUG'},
    'C6318': {'severity':'CRITICAL','type':'BUG'},
    'C6322': {'severity':'CRITICAL','type':'BUG'},
    'C6334': {'severity':'CRITICAL','type':'BUG'},
    'C6335': {'severity':'CRITICAL','type':'BUG'},
    'C6383': {'severity':'CRITICAL','type':'BUG'},
    'C6535': {'severity':'CRITICAL','type':'BUG'},
    # C++ Core Guidelines checker warnings
    'C26100': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26101': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26105': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26110': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26111': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26112': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26115': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26116': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26117': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26130': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26135': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26140': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26160': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26165': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26166': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26167': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26400': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26401': {'severity':'BLOCKER','type':'BUG'},
    'C26402': {'severity':'BLOCKER','type':'BUG'},
    'C26403': {'severity':'BLOCKER','type':'BUG'},
    'C26404': {'severity':'BLOCKER','type':'BUG'},
    'C26405': {'severity':'BLOCKER','type':'BUG'},
    'C26406': {'severity':'BLOCKER','type':'CODE_SMELL'},
    'C26407': {'severity':'BLOCKER','type':'CODE_SMELL'},
    'C26408': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26409': {'severity':'BLOCKER','type':'BUG'},
    'C26410': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26411': {'severity':'BLOCKER','type':'BUG'},
    'C26412': {'severity':'MAJOR','type':'CODE_SMELL'},
    'C26423': {'severity':'MAJOR','type':'CODE_SMELL'},
    'C26424': {'severity':'MAJOR','type':'CODE_SMELL'},
    'C26453': {'severity':'BLOCKER','type':'BUG'},
    'C26454': {'severity':'BLOCKER','type':'BUG'},
    'C26460': {'severity':'MAJOR','type':'CODE_SMELL'},
    'C26461': {'severity':'MAJOR','type':'CODE_SMELL'},
    'C26462': {'severity':'MAJOR','type':'CODE_SMELL'},
    'C26463': {'severity':'MAJOR','type':'CODE_SMELL'},
    'C26464': {'severity':'MAJOR','type':'CODE_SMELL'},
    'C26465': {'severity':'MAJOR','type':'CODE_SMELL'},
    'C26466': {'severity':'MAJOR','type':'CODE_SMELL'},
    'C26470': {'severity':'CRITICAL','type':'BUG'},
    'C26471': {'severity':'CRITICAL','type':'BUG'},
    'C26481': {'severity':'CRITICAL','type':'BUG'},
    'C26482': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26483': {'severity':'CRITICAL','type':'BUG'},
    'C26485': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26486': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26487': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26489': {'severity':'BLOCKER','type':'BUG'},
    'C26490': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26491': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26492': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26493': {'severity':'CRITICAL','type':'CODE_SMELL'},
    'C26494': {'severity':'MAJOR','type':'BUG'},
    'C26495': {'severity':'MAJOR','type':'BUG'},
    'C26496': {'severity':'MAJOR','type':'CODE_SMELL'},
    'C26497': {'severity':'MAJOR','type':'CODE_SMELL'},
    'C26498': {'severity':'MAJOR','type':'CODE_SMELL'},
    'C28103': {'severity':'INFO','type':'BUG'},
    'C28105': {'severity':'INFO','type':'BUG'},
    'C28114': {'severity':'BLOCKER','type':'BUG'},
}


et = get_cdata_capable_xml_etree()

def read_page_source(browser, url):
    """
    Read source code of a HTML page.
    """
    browser.get(url)
    return browser.page_source


def warning_key(menu_item):
    """
    Parse for warning key in menu item.
    """
    if not menu_item:
        return None
    if 'warnings' in menu_item:
        return None
    match = re.search('(C[0-9]{4,5})', menu_item)
    if not match:
        return None
    return match.group(1)


def parse_warning_hrefs(page_source, warnings):
    """
    Parse source code of HTML menu and extract hrefs of items with warnings.
    """
    # parse HTML page
    soup = BeautifulSoup(page_source, 'html.parser')

    # read all warnings from menu: Cnnnnn
    for menu_item in  soup.find_all('a', href=True, string=re.compile('(Warning)?[ ]?C[0-9]{4,5}$')):
        key = warning_key(menu_item.string)
        href = menu_item['href']
        if key and href:
            warnings[key] = {'key': key, 'href': href}

    return warnings


def name(elem, key, default_name):
    """
    Create 'name' item for a rule.
    """
    text = ''
    if elem:
        for string in elem.strings:
            text += string
        prefix = 'Warning '
        if text.startswith(prefix):
            text = text.replace(prefix, "", 1)
        prefix = 'warning '
        if text.startswith(prefix):
            text = text.replace(prefix, "", 1)
        match = re.match('^(C[0-9]+)[ :-](.*)', text)
        if match:
            text = match.group(1) + ': ' + match.group(2).strip()
        else:
            text = key + ': ' + text.strip()
        if '\n' in text:
            lines = text.split('\n', 1)
            text = lines[0]
        if text.endswith('.') or text.endswith(':'):
            text = text[:-1]
    if not text:
        text = default_name
    return text


def parse_warning_page(page_source, warning):
    """
    Pare source code of a warning HTML page and extract key, name and description.
    """
    # parse HTML page
    soup = BeautifulSoup(page_source, 'html.parser')
    content = soup.find("div", class_="content")

    # use header, sometimes only message ID
    key = warning['key']
    warning['name'] = name(content.find('h1'), key, key)
    # sometimes better description inside blockquote
    warning['name'] = name(content.select_one('blockquote > p'), key, warning['name'])

    desc = ''
    for paragraph in  content.select('div > p'):
        txt = str(paragraph)
        if 'Compiler Warning ' in warning['name']:
            # compiler messages: first p element is header
            if len(txt) < 200:
                warning['name'] = name(paragraph, key, warning['name'])
            else:
                desc += txt
                break
        else:
            # use only first p block: XML otherwise becomes too large
            desc += txt
            break
    if not desc:
        # repeat header in description to have something
        desc = '<p>'  + EntitySubstitution().substitute_html(warning['name']) + '</p>'
    warning['description'] = desc
    return warning


def read_warning_pages(browser, warnings):
    """
    Iterate over all HTML warning pages and parse content.
    """
    # read HTML pages of warnings
    for _key, data in list(warnings.items()):
        page_source = read_page_source(browser, data['href'])
        data = parse_warning_page(page_source, data)


def description(data):
    """
    Create a description tag with link to Microsoft documentation.
    """
    html = '\n' + data['description']
    html += '\n<h2>Microsoft Documentation</h2>'
    html += '\n<p><a href="{}" target="_blank">{}</a></p>'.format(data['href'], data['key'])
    return html


def add_template_rules(rules):
    """
    Add template rule(s) to XML.
    """
    rule_key = 'CustomRuleTemplate'
    rule_name = 'Rule template for Visual Studio custom rules'
    rule_severity = 'MAJOR'
    # pylint: disable=line-too-long
    rule_description = """<p>Follow these steps to make your custom rules available in SonarQube:</p>
<ol>
  <ol>
    <li>Create a new rule in SonarQube by "copying" this rule template and specify the <code>CheckId</code> of your custom rule, a title, a description, and a default severity.</li>
    <li>Enable the newly created rule in your quality profile</li>
  </ol>
  <li>Relaunch an analysis on your projects, et voilà, your custom rules are executed!</li>
</ol>"""

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = rule_key
    et.SubElement(rule, 'cardinality').text = "MULTIPLE"
    et.SubElement(rule, 'name').text=rule_name
    et.SubElement(rule, 'description').append(CDATA(rule_description))
    et.SubElement(rule, 'severity').text = rule_severity
    rules.append(rule)


def create_rules(warnings, rules):
    """
    Add warnings as rules to XML.
    - default 'type' is 'CODE_SMELL'
    - default 'severity' is 'INFO'
    - if not 'INFO' / 'CODE_SMELL' set:
      - 'remediationFunction' to 'LINEAR'
    - - 'remediationFunctionGapMultiplier' to '5min'
    """
    for _key, data in list(warnings.items()):
        rule = et.Element('rule')

        # mandatory
        et.SubElement(rule, 'key').text = data['key']
        et.SubElement(rule, 'name').text = data['name']
        cdata = CDATA(description(data))
        et.SubElement(rule, 'description').append(cdata)

        # optional
        if 'tag' in data:
            for tag in data['tag'].split(','):
                et.SubElement(rule, 'tag').text = tag

        if 'internalKey' in data:
            et.SubElement(rule, 'internalKey').text = data['internalKey']

        if 'severity' in data:
            et.SubElement(rule, 'severity').text = data['severity']
        else:
            et.SubElement(rule, 'severity').text = 'INFO'

        if 'type' in data:
            et.SubElement(rule, 'type').text = data['type']
        else:
            et.SubElement(rule, 'type').text = 'CODE_SMELL'

        if ('remediationFunction' in data) and ('remediationFunctionGapMultiplier' in data):
            et.SubElement(rule, 'remediationFunction').text = data['remediationFunction']
            et.SubElement(rule, 'remediationFunctionGapMultiplier').text = data['remediationFunctionGapMultiplier']
        else:
            if ('severity' in data) and ('type' in data) and not (
                    (data['severity'] == 'INFO') and (data['type'] == 'CODE_SMELL')):
                et.SubElement(rule, 'remediationFunction').text = 'LINEAR'
                et.SubElement(rule, 'remediationFunctionGapMultiplier').text = '5min'

        rules.append(rule)


def sorter(item):
    """
    Helper to sort warning keys in a numeric order (as ints).
    """
    return  int(item[0][1:])


def assign_warning_properties(warning, defaults, override):
    """
    Assign default properties to warnings.
    - overide=False: init only once
    - overide=True: overwrite already existing values
    """
    assign = False
    if override:
        assign = True
    elif 'type' not in warning:
        # set default values only once
        assign = True
    if assign:
        for key, value in list(defaults.items()):
            warning[key] = value


def read_warnings():
    """
    Read warnings from HTML pages.
    - root pages are defined in URLS
    - special property values are defined in RULE_MAP
    """
    # page contains JavaScript. Use Firefox to create HTML page
    # you have to download and install https://github.com/mozilla/geckodriver/releases
    service = Service(executable_path=r'C:\Program Files\geckodriver\geckodriver.exe')
    browser = webdriver.Firefox(service=service)

    # read links to warning pages from menu of overview pages
    warnings = {}
    for url, properties in list(URLS.items()):
        page_source = read_page_source(browser, url)
        parse_warning_hrefs(page_source, warnings)
        for key, warning in list(warnings.items()):
            assign_warning_properties(warning, properties, False)

    # warnings = dict(list(warnings.items())[:1]) # for testing only

    # sort warnings ascending by message number
    warnings = dict(sorted(list(warnings.items()), key=sorter))

    # read content of warning pages
    read_warning_pages(browser, warnings)

    # override defaults
    for key, defaults in list(RULE_MAP.items()):
        if key in warnings:
            warning = warnings[key]
            assign_warning_properties(warning, defaults, True)

    # close browser
    browser.quit()
    return warnings


def create_xml(warnings):
    """
    Write rules in XML format to stdout.
    """
    rules = et.Element('rules')

    add_template_rules(rules)
    create_rules(warnings, rules)

    write_rules_xml(rules, sys.stdout)


def main():
    """
    Main.
    """
    warnings = read_warnings()
    create_xml(warnings)
    return 0


if __name__ == "__main__":
    sys.exit(main())
