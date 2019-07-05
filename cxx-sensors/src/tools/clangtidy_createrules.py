# Sonar C++ Plugin (Community)
# Copyright (C) 2010-2019 SonarOpenCommunity
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

import cgi
import json
import os
import re
import subprocess
import sys

from utils_createrules import CDATA
from utils_createrules import write_rules_xml
from utils_createrules import get_cdata_capable_xml_etree

SEVERITY_MAP = {

    # CODE_SMELL INFO

    "abseil-no-namespace": {"type": "CODE_SMELL", "severity": "INFO"},
    "android-cloexec-creat": {"type": "CODE_SMELL", "severity": "INFO"},
    "bugprone-suspicious-enum-usage": {"type": "CODE_SMELL", "severity": "INFO"},
    "cert-dcl59-cpp": {"type": "CODE_SMELL", "severity": "INFO"},
    "cppcoreguidelines-avoid-magic-numbers": {"type": "CODE_SMELL", "severity": "INFO"},
    "cppcoreguidelines-no-malloc": {"type": "CODE_SMELL", "severity": "INFO"},
    "fuchsia-default-arguments": {"type": "CODE_SMELL", "severity": "INFO"},
    "fuchsia-header-anon-namespaces": {"type": "CODE_SMELL", "severity": "INFO"},
    "fuchsia-multiple-inheritance": {"type": "CODE_SMELL", "severity": "INFO"},
    "fuchsia-overloaded-operator": {"type": "CODE_SMELL", "severity": "INFO"},
    "fuchsia-restrict-system-includes": {"type": "CODE_SMELL", "severity": "INFO"},
    "fuchsia-trailing-return": {"type": "CODE_SMELL", "severity": "INFO"},
    "fuchsia-virtual-inheritance": {"type": "CODE_SMELL", "severity": "INFO"},
    "google-objc-avoid-throwing-exception": {"type": "CODE_SMELL", "severity": "INFO"},
    "google-objc-global-variable-declaration": {"type": "CODE_SMELL", "severity": "INFO"},
    "google-readability-braces-around-statements": {"type": "CODE_SMELL", "severity": "INFO"},
    "google-readability-function-size": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-braces-around-statements": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-deprecated-headers": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-exception-baseclass": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-function-size": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-named-parameter": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-new-delete-operators": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-no-array-decay": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-no-assembler": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-no-malloc": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-signed-bitwise": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-use-auto": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-use-emplace": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-use-equals-default": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-use-equals-delete": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-use-noexcept": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-use-nullptr": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-use-override": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-replace-random-shuffle": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-return-braced-init-list": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-unary-static-assert": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-use-default-member-init": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-use-equals-default": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-use-equals-delete": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-use-noexcept": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-use-transparent-functors": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-use-uncaught-exceptions": {"type": "CODE_SMELL", "severity": "INFO"},
    "objc-property-declaration": {"type": "CODE_SMELL", "severity": "INFO"},
    "portability-simd-intrinsics": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-delete-null-pointer": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-implicit-bool-conversion": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-magic-numbers": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-misleading-indentation": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-misplaced-array-index": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-non-const-parameter": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-redundant-declaration": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-redundant-function-ptr-dereference": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-redundant-member-init": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-simplify-subscript-expr": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-static-accessed-through-instance": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-string-compare": {"type": "CODE_SMELL", "severity": "INFO"},

    # CODE_SMELL MINOR

    "abseil-redundant-strcat-calls": {"type": "CODE_SMELL", "severity": "MINOR"},
    "abseil-str-cat-append": {"type": "CODE_SMELL", "severity": "MINOR"},
    "abseil-string-find-startswith": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cert-dcl03-c": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cert-dcl54-cpp": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cert-oop11-cpp": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cppcoreguidelines-c-copy-assignment-signature": {"type": "CODE_SMELL", "severity": "MINOR"},
    "fuchsia-statically-constructed-objects": {"type": "CODE_SMELL", "severity": "MINOR"},
    "hicpp-move-const-arg": {"type": "CODE_SMELL", "severity": "MINOR"},
    "hicpp-static-assert": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-implicit-conversion-in-loop": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-inefficient-algorithm": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-inefficient-string-concatenation": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-inefficient-vector-operation": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-move-const-arg": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-move-constructor-init": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-noexcept-move-constructor": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-type-promotion-in-math-fn": {"type": "CODE_SMELL", "severity": "MINOR"},

    # CODE_SMELL MAJOR

    "bugprone-fold-init-type": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "bugprone-misplaced-widening-cast": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "bugprone-parent-virtual-call": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "bugprone-sizeof-expression": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "bugprone-suspicious-missing-comma": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "bugprone-terminating-continue": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "bugprone-unused-raii": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "bugprone-unused-return-value": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "bugprone-virtual-near-miss": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "cert-dcl21-cpp": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "cert-dcl58-cpp": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "cert-fio38-c": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "cppcoreguidelines-avoid-goto": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "cppcoreguidelines-interfaces-global-init": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "cppcoreguidelines-narrowing-conversions": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "cppcoreguidelines-owning-memory": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "cppcoreguidelines-slicing": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "cppcoreguidelines-special-member-functions": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "hicpp-avoid-goto": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "hicpp-explicit-conversions": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "hicpp-member-init": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "hicpp-multiway-paths-covered": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "hicpp-noexcept-move": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "hicpp-special-member-functions": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "hicpp-vararg": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "mpi-buffer-deref": {"type": "CODE_SMELL", "severity": "MAJOR"},

    # BUG MINOR

    "bugprone-argument-comment": {"type": "BUG", "severity": "MINOR"},
    "bugprone-assert-side-effect": {"type": "BUG", "severity": "MINOR"},
    "bugprone-bool-pointer-implicit-conversion": {"type": "BUG", "severity": "MINOR"},
    "bugprone-copy-constructor-init": {"type": "BUG", "severity": "MINOR"},
    "bugprone-exception-escape": {"type": "BUG", "severity": "MINOR"},
    "bugprone-forward-declaration-namespace": {"type": "BUG", "severity": "MINOR"},
    "bugprone-forwarding-reference-overload": {"type": "BUG", "severity": "MINOR"},
    "bugprone-inaccurate-erase": {"type": "BUG", "severity": "MINOR"},
    "bugprone-integer-division": {"type": "BUG", "severity": "MINOR"},
    "bugprone-macro-parentheses": {"type": "BUG", "severity": "MINOR"},
    "bugprone-macro-repeated-side-effects": {"type": "BUG", "severity": "MINOR"},
    "bugprone-move-forwarding-reference": {"type": "BUG", "severity": "MINOR"},
    "bugprone-multiple-statement-macro": {"type": "BUG", "severity": "MINOR"},
    "bugprone-sizeof-container": {"type": "BUG", "severity": "MINOR"},
    "bugprone-string-constructor": {"type": "BUG", "severity": "MINOR"},
    "bugprone-string-integer-assignment": {"type": "BUG", "severity": "MINOR"},
    "bugprone-suspicious-memset-usage": {"type": "BUG", "severity": "MINOR"},
    "bugprone-suspicious-string-compare": {"type": "BUG", "severity": "MINOR"},
    "bugprone-swapped-arguments": {"type": "BUG", "severity": "MINOR"},
    "bugprone-throw-keyword-missing": {"type": "BUG", "severity": "MINOR"},
    "bugprone-undelegated-constructor": {"type": "BUG", "severity": "MINOR"},
    "cert-err09-cpp": {"type": "BUG", "severity": "MINOR"},
    "cert-err61-cpp": {"type": "BUG", "severity": "MINOR"},
    "hicpp-undelegated-constructor": {"type": "BUG", "severity": "MINOR"},
    "mpi-type-mismatch": {"type": "BUG", "severity": "MINOR"},
    "objc-avoid-nserror-init": {"type": "BUG", "severity": "MINOR"},
    "objc-forbidden-subclassing": {"type": "BUG", "severity": "MINOR"},

    # BUG MAJOR

    "bugprone-incorrect-roundings": {"type": "BUG", "severity": "MAJOR"},
    "bugprone-lambda-function-name": {"type": "BUG", "severity": "MAJOR"},
    "bugprone-misplaced-operator-in-strlen-in-alloc": {"type": "BUG", "severity": "MAJOR"},
    "bugprone-string-literal-with-embedded-nul": {"type": "BUG", "severity": "MAJOR"},
    "bugprone-suspicious-semicolon": {"type": "BUG", "severity": "MAJOR"},
    "bugprone-undefined-memory-manipulation": {"type": "BUG", "severity": "MAJOR"},
    "bugprone-use-after-move": {"type": "BUG", "severity": "MAJOR"},
    "hicpp-invalid-access-moved": {"type": "BUG", "severity": "MAJOR"},
    "objc-avoid-spinlock": {"type": "BUG", "severity": "MAJOR"},
    "zircon-temporary-objects": {"type": "BUG", "severity": "MAJOR"},

    # VULNERABILITY INFO

    "cert-msc32-c": {"type": "VULNERABILITY", "severity": "INFO"},
    "cert-msc50-cpp": {"type": "VULNERABILITY", "severity": "INFO"},
    "cert-msc51-cpp": {"type": "VULNERABILITY", "severity": "INFO"},

    # VULNERABILITY MINOR

    "android-cloexec-accept4": {"type": "VULNERABILITY", "severity": "MINOR"},
    "android-cloexec-dup": {"type": "VULNERABILITY", "severity": "MINOR"},
    "android-cloexec-epoll-create": {"type": "VULNERABILITY", "severity": "MINOR"},
    "android-cloexec-epoll-create1": {"type": "VULNERABILITY", "severity": "MINOR"},
    "android-cloexec-fopen": {"type": "VULNERABILITY", "severity": "MINOR"},
    "android-cloexec-inotify-init": {"type": "VULNERABILITY", "severity": "MINOR"},
    "android-cloexec-inotify-init1": {"type": "VULNERABILITY", "severity": "MINOR"},
    "android-cloexec-memfd-create": {"type": "VULNERABILITY", "severity": "MINOR"},
    "android-cloexec-open": {"type": "VULNERABILITY", "severity": "MINOR"},
    "android-cloexec-socket": {"type": "VULNERABILITY", "severity": "MINOR"}

}

et = get_cdata_capable_xml_etree()

CLANG_TIDY_DOC_URL_BASE = "http://clang.llvm.org/extra/clang-tidy/checks/"


def fix_local_urls(html, filename):
    # replace local ancors
    html = re.sub("href=\"(?!http)#", "href=\"" +
                  CLANG_TIDY_DOC_URL_BASE + filename + ".html#", html)
    # replace local urls
    html = re.sub("href=\"(?!http)", "href=\"" + CLANG_TIDY_DOC_URL_BASE, html)
    return html


def rstfile_to_description(path, filename, fix_urls):
    html = subprocess.check_output(
        ['pandoc', path, '--no-highlight', '-f', 'rst', '-t', 'html5'])
    footer = """<h2>References</h2>
<p><a href="%s%s.html" target="_blank">clang.llvm.org</a></p>""" % (CLANG_TIDY_DOC_URL_BASE, filename)
    if fix_urls:
        html = fix_local_urls(html, filename)

    return html + footer


def rstfile_to_rule(path, fix_urls):
    rule = et.Element('rule')

    filename_with_extension = os.path.basename(path)
    filename = os.path.splitext(filename_with_extension)[0]

    key = filename
    name = filename
    description = rstfile_to_description(path, filename, fix_urls)

    default_issue_type = "CODE_SMELL"
    default_issue_severity = "MAJOR"

    et.SubElement(rule, 'key').text = key
    et.SubElement(rule, 'name').text = name

    cdata = CDATA(description)
    et.SubElement(rule, 'description').append(cdata)

    custom_severity = SEVERITY_MAP.get(key, None)
    if custom_severity is None:
        et.SubElement(rule, 'severity').text = default_issue_severity
        et.SubElement(rule, 'type').text = default_issue_type
    else:
        et.SubElement(rule, 'severity').text = custom_severity["severity"]
        et.SubElement(rule, 'type').text = custom_severity["type"]

    return rule


def rstfiles_to_rules_xml(directory, fix_urls):
    rules = et.Element('rules')
    for subdir, _, files in os.walk(directory):
        for f in files:
            ext = os.path.splitext(f)[-1].lower()
            if ext == ".rst" and f != "list.rst":
                rst_file_path = os.path.join(subdir, f)
                rules.append(rstfile_to_rule(rst_file_path, fix_urls))
    write_rules_xml(rules, sys.stdout)


def contains_required_fields(entry_value):
    FIELDS = ["!name", "!anonymous", "!superclasses",
              "Class", "DefaultSeverity", "Text"]
    for field in FIELDS:
        if field not in entry_value:
            return False
    return True


def create_clang_default_rules(rules):
    # defaults clang warning (not associated with any activation switch)
    rule_key = "clang-diagnostic-warning"
    rule_name = "clang-diagnostic-warning"
    rule_type = DIAG_CLASS["CLASS_WARNING"]["sonarqube_type"]
    rule_severity = SEVERITY["SEV_Warning"]["sonarqube_severity"] 
    rule_description = "<p>Diagnostic text: default compiler warnings</p>"

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = rule_key
    et.SubElement(rule, 'name').text = rule_name
    et.SubElement(rule, 'description').append(CDATA(rule_description))
    et.SubElement(rule, 'severity').text = rule_severity
    et.SubElement(rule, 'type').text = rule_type
    rules.append(rule)

def collect_warnings(data, diag_group_id, warnings_in_group):
    diag_group = data[diag_group_id]

    for entry_key in data:
        entry_value = data[entry_key]
        if entry_key == "!tablegen_json_version":
            continue

        if not contains_required_fields(entry_value):
            continue

        if "Diagnostic" not in entry_value["!superclasses"]:
            continue

        if "InGroup" not in entry_value["!superclasses"]:
            continue

        group_key = entry_value["Group"]["def"]
        if group_key == diag_group_id:
            warnings_in_group.append(entry_value)

    sub_groups = diag_group["SubGroups"]
    for sub_group in sub_groups:
        sub_group_id = sub_group["def"]
        collect_warnings(data, sub_group_id, warnings_in_group)


# see DiagClass in JSON
# see http://clang.llvm.org/docs/DiagnosticsReference.html for the
# printable mappings
DIAG_CLASS = {"CLASS_EXTENSION": {"weight": 0, "sonarqube_type": "CODE_SMELL", "printable": "warning"},
              "CLASS_NOTE": {"weight": 0, "sonarqube_type": "CODE_SMELL", "printable": "note"},
              "CLASS_REMARK": {"weight": 0, "sonarqube_type": "CODE_SMELL", "printable": "remark"},
              "CLASS_WARNING": {"weight": 0, "sonarqube_type": "CODE_SMELL", "printable": "warning"},
              "CLASS_ERROR": {"weight": 1, "sonarqube_type": "BUG", "printable": "error"}
              }

# see Severity in JSON
# SEV_Ignored means, that the corresponding diagnostics is disabled in clang by default
# this fact doesn't make any statement about the real severity, so we use MAJOR
SEVERITY = {"SEV_Remark": {"weight": 0, "sonarqube_severity": "INFO"},
            "SEV_Ignored": {"weight": 1, "sonarqube_severity": "MAJOR"},
            "SEV_Warning": {"weight": 1, "sonarqube_severity": "MAJOR"},
            "SEV_Error": {"weight": 2, "sonarqube_severity": "CRITICAL"},
            "SEV_Fatal": {"weight": 3, "sonarqube_severity": "BLOCKER"}
            }


def calculate_rule_type_and_severity(diagnostics):
    max_class = "CLASS_EXTENSION"
    max_severity = "SEV_Ignored"

    for diagnostic in diagnostics:
        diag_class = diagnostic["Class"]["def"]
        diag_severity = diagnostic["DefaultSeverity"]["def"]

        if DIAG_CLASS[diag_class]["weight"] >= DIAG_CLASS[max_class]["weight"]:
            max_class = diag_class
        if SEVERITY[diag_severity]["weight"] >= SEVERITY[max_severity]["weight"]:
            max_severity = diag_severity

    return DIAG_CLASS[max_class]["sonarqube_type"], SEVERITY[max_severity]["sonarqube_severity"]


def generate_description(diag_group_name, diagnostics):
    html_lines = ["<p>Diagnostic text:</p>", "<ul>"]
    all_diagnostics_are_remarks = True
    for diagnostic in sorted(diagnostics, key=lambda k: k['Text']):
        diag_class = diagnostic["Class"]["def"]
        all_diagnostics_are_remarks = all_diagnostics_are_remarks and (
            diag_class == "CLASS_REMARK")
        diag_text = diagnostic["Text"]
        diag_class_printable = DIAG_CLASS[diag_class]["printable"]
        diag_text_escaped = cgi.escape(diag_text)
        html_lines.append("<li>%s: %s</li>" %
                          (diag_class_printable, diag_text_escaped))
    html_lines.append("</ul>")
    html_lines.append("<h2>References</h2>")
    anchor_prefix = "r" if all_diagnostics_are_remarks else "w"
    html_lines.append('<p><a href="http://clang.llvm.org/docs/DiagnosticsReference.html#%s%s" target="_blank">Diagnostic flags in Clang</a></p>' %
                      (anchor_prefix, diag_group_name))
    return "\n".join(html_lines)


def diagnostics_to_rules_xml(json_file):
    rules = et.Element('rules')
    
    # add clang default warnings 
    create_clang_default_rules(rules)
    
    with open(json_file) as f:
        data = json.load(f)
        diag_groups = data["!instanceof"]["DiagGroup"]
        for diag_group_id in sorted(diag_groups):
            if not data[diag_group_id]["GroupName"]:
                continue

            # colleact all Diagnostics included into this DiagGroup
            warnings_in_group = []
            collect_warnings(data, diag_group_id, warnings_in_group)

            if not warnings_in_group:
                continue

            # for each DiagGroup calculate the rule type and severity
            rule_type, rule_severity = calculate_rule_type_and_severity(
                warnings_in_group)

            group_name_escaped = data[diag_group_id]["GroupName"].replace(
                "++", "-").replace("#", "-").replace("--", "-")
            rule_name = "clang-diagnostic-" + data[diag_group_id]["GroupName"]
            rule_key = "clang-diagnostic-" + data[diag_group_id]["GroupName"]
            rule_description = generate_description(
                group_name_escaped, warnings_in_group)

            rule = et.Element('rule')
            et.SubElement(rule, 'key').text = rule_key
            et.SubElement(rule, 'name').text = rule_name
            et.SubElement(rule, 'description').append(CDATA(rule_description))
            et.SubElement(rule, 'severity').text = rule_severity
            et.SubElement(rule, 'type').text = rule_type
            rules.append(rule)

    write_rules_xml(rules, sys.stdout)

#
# GENERATION OF RULES FROM CLANG-TIDY DOCUMENTATION (RST FILES)
#
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
# 3. compare the new version with the old one, extend the old XML
# python utils_createrules.py comparerules clangtidy.xml clangtidy_new.xml
# meld clangtidy.xml clangtidy_new.xml.comparable
#
# 4. optional: try to fix local urls in the documentation
# python clangtidy_createrules.py rules_fixurls /tmp/trunk/docs/clang-tidy/checks > clangtidy_new.xml
# python utils_createrules.py comparerules clangtidy.xml clangtidy_new.xml
# meld clangtidy.xml clangtidy_new.xml.comparable

#
# GENERATION OF RULES FROM CLANG DIAGNOSTICS
#
# 0. check out clang source code
#    https://clang.llvm.org/get_started.html
#    perform 1. Get the required tools
#            2. Check out LLVM
#            3. Check out Clang
#            7. Build LLVM and Clang
#
# 1. use the TableGen (https://llvm.org/docs/TableGen/index.html) for generation of the list of diagnostics
#
#    cd <src_dir>/tools/clang/include/clang/Basic/
#    <src_dir>/build/bin/llvm-tblgen -dump-json <src_dir>/tools/clang/include/clang/Basic/Diagnostic.td > output.json
#
# 2. generate the new version of the rules file
#
#    python clangtidy_createrules.py diagnostics output.json > clangtidy_new.xml
#
# 3. compare the new version with the old one, extend the old XML
#
#    python utils_createrules.py comparerules clangtidy.xml clangtidy_new.xml
#    meld clangtidy.xml clangtidy_new.xml.comparable


def print_usage_and_exit():
    script_name = os.path.basename(sys.argv[0])
    print """Usage: %s rules <path to clang-tidy source directory with RST files>
       %s rules_fixurls <path to clang-tidy source directory with RST files>
       %s diagnostics <path to the JSON diagnostics descriptions>
       see the source code for inline documentation""" % (script_name, script_name, script_name)
    sys.exit(1)


if __name__ == "__main__":

    if len(sys.argv) < 2:
        print_usage_and_exit()

    # transform to an other elementtree
    if sys.argv[1] == "rules":
        rstfiles_to_rules_xml(sys.argv[2], False)
    elif sys.argv[1] == "rules_fixurls":
        rstfiles_to_rules_xml(sys.argv[2], True)
    elif sys.argv[1] == "diagnostics":
        diagnostics_to_rules_xml(sys.argv[2])
    else:
        print_usage_and_exit()
