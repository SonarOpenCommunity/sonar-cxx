#!/usr/bin/env python3

# -*- coding: utf-8 -*-
# SonarQube C++ Community Plugin (cxx plugin)
# Copyright (C) 2010-2025 SonarOpenCommunity
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

import html
import json
import os
import re
import subprocess
import sys

from utils_createrules import CDATA
from utils_createrules import write_rules_xml
from utils_createrules import get_cdata_capable_xml_etree

SEVERITY_MAP = {

    # last update: llvmorg-16-init-15404-g61be26154924 (git describe)
    # rule keys are in alphabetical order

    "abseil-no-namespace": {"type": "CODE_SMELL", "severity": "INFO"},
    "abseil-redundant-strcat-calls": {"type": "CODE_SMELL", "severity": "MINOR"},
    "abseil-str-cat-append": {"type": "CODE_SMELL", "severity": "MINOR"},
    "abseil-string-find-startswith": {"type": "CODE_SMELL", "severity": "MINOR"},
    "android-cloexec-accept4": {"type": "VULNERABILITY", "severity": "MINOR"},
    "android-cloexec-creat": {"type": "CODE_SMELL", "severity": "INFO"},
    "android-cloexec-dup": {"type": "VULNERABILITY", "severity": "MINOR"},
    "android-cloexec-epoll-create": {"type": "VULNERABILITY", "severity": "MINOR"},
    "android-cloexec-epoll-create1": {"type": "VULNERABILITY", "severity": "MINOR"},
    "android-cloexec-fopen": {"type": "VULNERABILITY", "severity": "MINOR"},
    "android-cloexec-inotify-init": {"type": "VULNERABILITY", "severity": "MINOR"},
    "android-cloexec-inotify-init1": {"type": "VULNERABILITY", "severity": "MINOR"},
    "android-cloexec-memfd-create": {"type": "VULNERABILITY", "severity": "MINOR"},
    "android-cloexec-open": {"type": "VULNERABILITY", "severity": "MINOR"},
    "android-cloexec-socket": {"type": "VULNERABILITY", "severity": "MINOR"},
    "bugprone-argument-comment": {"type": "BUG", "severity": "MINOR"},
    "bugprone-assert-side-effect": {"type": "BUG", "severity": "MINOR"},
    "bugprone-bad-signal-to-kill-thread": {"type": "BUG", "severity": "MINOR"},
    "bugprone-bool-pointer-implicit-conversion": {"type": "BUG", "severity": "MINOR"},
    "bugprone-branch-clone": {"type": "CODE_SMELL", "severity": "INFO"},
    "bugprone-copy-constructor-init": {"type": "BUG", "severity": "MINOR"},
    "bugprone-dangling-handle": {"type": "BUG", "severity": "MINOR"},
    "bugprone-exception-escape": {"type": "BUG", "severity": "MINOR"},
    "bugprone-fold-init-type": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "bugprone-forward-declaration-namespace": {"type": "BUG", "severity": "MINOR"},
    "bugprone-forwarding-reference-overload": {"type": "BUG", "severity": "MINOR"},
    "bugprone-inaccurate-erase": {"type": "BUG", "severity": "MINOR"},
    "bugprone-incorrect-roundings": {"type": "BUG", "severity": "MAJOR"},
    "bugprone-infinite-loop": {"type": "CODE_SMELL", "severity": "MINOR"},
    "bugprone-integer-division": {"type": "BUG", "severity": "MINOR"},
    "bugprone-lambda-function-name": {"type": "BUG", "severity": "MINOR"},
    "bugprone-macro-parentheses": {"type": "BUG", "severity": "MINOR"},
    "bugprone-macro-repeated-side-effects": {"type": "BUG", "severity": "MINOR"},
    "bugprone-misplaced-operator-in-strlen-in-alloc": {"type": "BUG", "severity": "MAJOR"},
    "bugprone-misplaced-pointer-arithmetic-in-alloc": {"type": "BUG", "severity": "MAJOR"},
    "bugprone-misplaced-widening-cast": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "bugprone-move-forwarding-reference": {"type": "BUG", "severity": "MINOR"},
    "bugprone-multiple-statement-macro": {"type": "BUG", "severity": "MINOR"},
    "bugprone-not-null-terminated-result": {"type": "BUG", "severity": "MINOR"},
    "bugprone-parent-virtual-call": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "bugprone-posix-return": {"type": "BUG", "severity": "MINOR"},
    "bugprone-redundant-branch-condition": {"type": "BUG", "severity": "MINOR"},
    "bugprone-reserved-identifier": {"type": "CODE_SMELL", "severity": "MINOR"},
    "bugprone-signal-handler": {"type": "BUG", "severity": "MINOR"},
    "bugprone-signed-char-misuse": {"type": "BUG", "severity": "MINOR"},
    "bugprone-sizeof-container": {"type": "BUG", "severity": "MINOR"},
    "bugprone-sizeof-expression": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "bugprone-spuriously-wake-up-functions": {"type": "BUG", "severity": "MINOR"},
    "bugprone-string-constructor": {"type": "BUG", "severity": "MINOR"},
    "bugprone-string-integer-assignment": {"type": "BUG", "severity": "MINOR"},
    "bugprone-string-literal-with-embedded-nul": {"type": "BUG", "severity": "MAJOR"},
    "bugprone-suspicious-enum-usage": {"type": "CODE_SMELL", "severity": "INFO"},
    "bugprone-suspicious-include": {"type": "BUG", "severity": "MINOR"},
    "bugprone-suspicious-memset-usage": {"type": "BUG", "severity": "MINOR"},
    "bugprone-suspicious-missing-comma": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "bugprone-suspicious-semicolon": {"type": "BUG", "severity": "MAJOR"},
    "bugprone-suspicious-string-compare": {"type": "BUG", "severity": "MINOR"},
    "bugprone-swapped-arguments": {"type": "BUG", "severity": "MINOR"},
    "bugprone-terminating-continue": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "bugprone-throw-keyword-missing": {"type": "BUG", "severity": "MINOR"},
    "bugprone-too-small-loop-variable": {"type": "BUG", "severity": "MINOR"},
    "bugprone-undefined-memory-manipulation": {"type": "BUG", "severity": "MAJOR"},
    "bugprone-undelegated-constructor": {"type": "BUG", "severity": "MINOR"},
    "bugprone-unhandled-self-assignment": {"type": "BUG", "severity": "MINOR"},
    "bugprone-unused-raii": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "bugprone-unused-return-value": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "bugprone-use-after-move": {"type": "BUG", "severity": "MAJOR"},
    "bugprone-virtual-near-miss": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cert-dcl03-c": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cert-dcl21-cpp": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "cert-dcl50-cpp": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cert-dcl54-cpp": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cert-dcl58-cpp": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "cert-dcl59-cpp": {"type": "CODE_SMELL", "severity": "INFO"},
    "cert-env33-c": {"type": "CODE_SMELL", "severity": "INFO"},
    "cert-err09-cpp": {"type": "BUG", "severity": "MINOR"},
    "cert-err34-c": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cert-err52-cpp": {"type": "CODE_SMELL", "severity": "INFO"},
    "cert-err60-cpp": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cert-err61-cpp": {"type": "BUG", "severity": "MINOR"},
    "cert-fio38-c": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "cert-flp30-c": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cert-mem57-cpp": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cert-msc32-c": {"type": "CODE_SMELL", "severity": "INFO"},
    "cert-msc50-cpp": {"type": "CODE_SMELL", "severity": "INFO"},
    "cert-msc51-cpp": {"type": "CODE_SMELL", "severity": "INFO"},
    "cert-oop11-cpp": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cert-oop57-cpp": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cert-oop58-cpp": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cppcoreguidelines-avoid-goto": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "cppcoreguidelines-avoid-magic-numbers": {"type": "CODE_SMELL", "severity": "INFO"},
    "cppcoreguidelines-c-copy-assignment-signature": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cppcoreguidelines-init-variables": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cppcoreguidelines-interfaces-global-init": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "cppcoreguidelines-narrowing-conversions": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "cppcoreguidelines-no-malloc": {"type": "CODE_SMELL", "severity": "INFO"},
    "cppcoreguidelines-owning-memory": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "cppcoreguidelines-prefer-member-initializer": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cppcoreguidelines-pro-bounds-pointer-arithmetic": {"type": "CODE_SMELL", "severity": "INFO"},
    "cppcoreguidelines-pro-type-const-cast": {"type": "CODE_SMELL", "severity": "INFO"},
    "cppcoreguidelines-pro-type-cstyle-cast": {"type": "CODE_SMELL", "severity": "INFO"},
    "cppcoreguidelines-pro-type-member-init": {"type": "CODE_SMELL", "severity": "MINOR"},
    "cppcoreguidelines-pro-type-reinterpret-cast": {"type": "CODE_SMELL", "severity": "INFO"},
    "cppcoreguidelines-pro-type-static-cast-downcast": {"type": "CODE_SMELL", "severity": "INFO"},
    "cppcoreguidelines-pro-type-union-access": {"type": "CODE_SMELL", "severity": "INFO"},
    "cppcoreguidelines-slicing": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "cppcoreguidelines-special-member-functions": {"type": "CODE_SMELL", "severity": "MINOR"},
    "etas-mcdcore-boost-test-classification": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "fuchsia-default-arguments": {"type": "CODE_SMELL", "severity": "INFO"},
    "fuchsia-header-anon-namespaces": {"type": "CODE_SMELL", "severity": "INFO"},
    "fuchsia-multiple-inheritance": {"type": "CODE_SMELL", "severity": "INFO"},
    "fuchsia-overloaded-operator": {"type": "CODE_SMELL", "severity": "INFO"},
    "fuchsia-restrict-system-includes": {"type": "CODE_SMELL", "severity": "INFO"},
    "fuchsia-statically-constructed-objects": {"type": "CODE_SMELL", "severity": "MINOR"},
    "fuchsia-trailing-return": {"type": "CODE_SMELL", "severity": "INFO"},
    "fuchsia-virtual-inheritance": {"type": "CODE_SMELL", "severity": "INFO"},
    "google-objc-avoid-throwing-exception": {"type": "CODE_SMELL", "severity": "INFO"},
    "google-objc-global-variable-declaration": {"type": "CODE_SMELL", "severity": "INFO"},
    "google-readability-braces-around-statements": {"type": "CODE_SMELL", "severity": "INFO"},
    "google-readability-function-size": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-avoid-goto": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "hicpp-braces-around-statements": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-deprecated-headers": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-exception-baseclass": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-explicit-conversions": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "hicpp-function-size": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-invalid-access-moved": {"type": "BUG", "severity": "MAJOR"},
    "hicpp-member-init": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "hicpp-move-const-arg": {"type": "CODE_SMELL", "severity": "MINOR"},
    "hicpp-multiway-paths-covered": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-named-parameter": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-new-delete-operators": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-no-array-decay": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-no-assembler": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-no-malloc": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-noexcept-move": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "hicpp-signed-bitwise": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-special-member-functions": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "hicpp-static-assert": {"type": "CODE_SMELL", "severity": "MINOR"},
    "hicpp-undelegated-constructor": {"type": "BUG", "severity": "MINOR"},
    "hicpp-use-auto": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-use-emplace": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-use-equals-default": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-use-equals-delete": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-use-noexcept": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-use-nullptr": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-use-override": {"type": "CODE_SMELL", "severity": "INFO"},
    "hicpp-vararg": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "misc-definitions-in-headers": {"type": "CODE_SMELL", "severity": "MINOR"},
    "misc-misplaced-const": {"type": "CODE_SMELL", "severity": "INFO"},
    "misc-new-delete-overloads": {"type": "CODE_SMELL", "severity": "MINOR"},
    "misc-non-copyable-objects": {"type": "CODE_SMELL", "severity": "MINOR"},
    "misc-redundant-expression": {"type": "CODE_SMELL", "severity": "INFO"},
    "misc-static-assert": {"type": "CODE_SMELL", "severity": "INFO"},
    "misc-throw-by-value-catch-by-reference": {"type": "CODE_SMELL", "severity": "MINOR"},
    "misc-unconventional-assign-operator": {"type": "CODE_SMELL", "severity": "MINOR"},
    "misc-uniqueptr-reset-release": {"type": "CODE_SMELL", "severity": "MINOR"},
    "misc-unused-alias-decls": {"type": "CODE_SMELL", "severity": "INFO"},
    "misc-unused-parameters": {"type": "CODE_SMELL", "severity": "INFO"},
    "misc-unused-using-decls": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-avoid-bind": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-avoid-c-arrays": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-concat-nested-namespaces": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-deprecated-headers": {"type": "CODE_SMELL", "severity": "MINOR"},
    "modernize-deprecated-ios-base-aliases": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-loop-convert": {"type": "CODE_SMELL", "severity": "MINOR"},
    "modernize-make-shared": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-make-unique": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-pass-by-value": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-raw-string-literal": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-redundant-void-arg": {"type": "CODE_SMELL", "severity": "MINOR"},
    "modernize-replace-auto-ptr": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-replace-disallow-copy-and-assign-macro": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-replace-random-shuffle": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-return-braced-init-list": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-shrink-to-fit": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-unary-static-assert": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-use-auto": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-use-bool-literals": {"type": "CODE_SMELL", "severity": "MINOR"},
    "modernize-use-default": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-use-default-member-init": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-use-emplace": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-use-equals-default": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-use-equals-delete": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-use-noexcept": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-use-nullptr": {"type": "CODE_SMELL", "severity": "MINOR"},
    "modernize-use-transparent-functors": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-use-uncaught-exceptions": {"type": "CODE_SMELL", "severity": "INFO"},
    "modernize-use-using": {"type": "CODE_SMELL", "severity": "INFO"},
    "mpi-buffer-deref": {"type": "CODE_SMELL", "severity": "MAJOR"},
    "mpi-type-mismatch": {"type": "BUG", "severity": "MINOR"},
    "objc-avoid-nserror-init": {"type": "BUG", "severity": "MINOR"},
    "objc-avoid-spinlock": {"type": "BUG", "severity": "MAJOR"},
    "objc-forbidden-subclassing": {"type": "BUG", "severity": "MINOR"},
    "objc-property-declaration": {"type": "CODE_SMELL", "severity": "INFO"},
    "performance-faster-string-find": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-for-range-copy": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-implicit-cast-in-loop": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-implicit-conversion-in-loop": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-inefficient-algorithm": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-inefficient-string-concatenation": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-inefficient-vector-operation": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-move-const-arg": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-move-constructor-init": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-no-automatic-move": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-no-int-to-ptr": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-noexcept-move-constructor": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-trivially-destructible": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-type-promotion-in-math-fn": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-unnecessary-copy-initialization": {"type": "CODE_SMELL", "severity": "MINOR"},
    "performance-unnecessary-value-param": {"type": "CODE_SMELL", "severity": "MINOR"},
    "portability-restrict-system-includes": {"type": "CODE_SMELL", "severity": "MINOR"},
    "portability-simd-intrinsics": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-avoid-const-params-in-decls": {"type": "CODE_SMELL", "severity": "MINOR"},
    "readability-braces-around-statements": {"type": "CODE_SMELL", "severity": "MINOR"},
    "readability-const-return-type": {"type": "CODE_SMELL", "severity": "MINOR"},
    "readability-container-size-empty": {"type": "CODE_SMELL", "severity": "MINOR"},
    "readability-convert-member-functions-to-static": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-delete-null-pointer": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-deleted-default": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-function-cognitive-complexity": {"type": "CODE_SMELL", "severity": "MINOR"},
    "readability-function-size": {"type": "CODE_SMELL", "severity": "MINOR"},
    "readability-identifier-naming": {"type": "CODE_SMELL", "severity": "MINOR"},
    "readability-implicit-bool-cast": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-implicit-bool-conversion": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-inconsistent-declaration-parameter-name": {"type": "CODE_SMELL", "severity": "MINOR"},
    "readability-isolate-declaration": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-magic-numbers": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-make-member-function-const": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-misleading-indentation": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-misplaced-array-index": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-non-const-parameter": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-redundant-access-specifiers": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-redundant-control-flow": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-redundant-declaration": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-redundant-function-ptr-dereference": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-redundant-member-init": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-redundant-preprocessor": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-redundant-smartptr-get": {"type": "CODE_SMELL", "severity": "MINOR"},
    "readability-redundant-string-cstr": {"type": "CODE_SMELL", "severity": "MINOR"},
    "readability-redundant-string-init": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-simplify-subscript-expr": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-static-accessed-through-instance": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-static-definition-in-anonymous-namespace": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-string-compare": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-uniqueptr-delete-release": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-uppercase-literal-suffix": {"type": "CODE_SMELL", "severity": "INFO"},
    "readability-use-anyofallof": {"type": "CODE_SMELL", "severity": "INFO"},
    "zircon-temporary-objects": {"type": "BUG", "severity": "MAJOR"},

}

deprecated_diag_keys = {
    'c++11-compat': ['c++0x-compat'],
    'c++11-extensions': ['c++0x-extensions'],
    'c++11-narrowing': ['c++0x-narrowing'],
    'c++14-compat': ['c++1y-compat'],
    'c++14-extensions': ['c++1y-extensions'],
    'c++17-compat': ['c++1z-compat'],
    'c++17-compat-mangling': ['c++1z-compat-mangling'],
    'c++17-extensions': ['c++1z-extensions'],
    'c++20-compat': ['c++2a-compat'],
    'c++20-compat-pedantic': ['c++2a-compat-pedantic'],
    'c++20-extensions': ['c++2a-extensions'],
    'c++23-compat': ['c++2b-compat'],
    'c++23-extensions': ['c++2b-extensions'],
    'c++26-compat' :['c++2c-compat'],
    'c++26-extensions': ['c++2c-extensions'],
    'pre-c++14-compat': ['pre-c++1y-compat'],
    'pre-c++14-compat-pedantic': ['pre-c++1y-compat-pedantic'],
    'pre-c++17-compat': ['pre-c++1z-compat'],
    'pre-c++17-compat-pedantic': ['pre-c++1z-compat-pedantic'],
    'pre-c++20-compat': ['pre-c++2a-compat'],
    'pre-c++20-compat-pedantic': ['pre-c++2a-compat-pedantic'],
    'pre-c++23-compat': ['pre-c++2b-compat'],
    'pre-c++23-compat-pedantic': ['pre-c++2b-compat-pedantic'],
    'pre-c++26-compat': ['pre-c++2c-compat'],
    'pre-c++26-compat-pedantic': ['pre-c++2c-compat-pedantic']
}

et = get_cdata_capable_xml_etree()

CLANG_TIDY_DOC_URL_BASE = "http://clang.llvm.org/extra/clang-tidy/checks/"


def fix_local_urls(html, filename):
    # replace local ancors
    html = re.sub("href=\"(?!http)(.*\\.html)??#", "href=\"" +
                  CLANG_TIDY_DOC_URL_BASE + filename + ".html#", html)
    # replace local urls
    html = re.sub("href=\"(?!http)", "href=\"" + CLANG_TIDY_DOC_URL_BASE, html)
    return html


def rstfile_to_description(path, filename, fix_urls):
    html = subprocess.check_output(
        ['pandoc', path, '--wrap=none', '--no-highlight', '-f', 'rst', '-t', 'html5']).decode('utf-8')
    footer = """<h2>References</h2>
<p><a href="%s%s.html" target="_blank">clang.llvm.org</a></p>""" % (CLANG_TIDY_DOC_URL_BASE, filename)
    if fix_urls:
        html = fix_local_urls(html, filename)

    html = html.replace('\r\n', '\n')
    return html + footer


def rstfile_to_rule(path, fix_urls):
    sys.stderr.write("[INFO] convert: '{}'\n".format(path))
    rule = et.Element('rule')

    filename_with_extension = os.path.basename(path)
    filename = os.path.splitext(filename_with_extension)[0]
    containing_folder = os.path.basename(os.path.dirname(path))

    key = containing_folder + '-' + filename
    name = key
    description = rstfile_to_description(path, containing_folder + '/' + filename, fix_urls)

    default_issue_type = "CODE_SMELL"
    default_issue_severity = "INFO"

    et.SubElement(rule, 'key').text = key
    et.SubElement(rule, 'name').text = name

    cdata = CDATA(description)
    et.SubElement(rule, 'description').append(cdata)

    custom_severity = SEVERITY_MAP.get(key, None)
    if custom_severity:
        default_issue_severity = custom_severity["severity"]
        default_issue_type = custom_severity["type"]

    if default_issue_severity != 'MAJOR': # MAJOR is the default
        et.SubElement(rule, 'severity').text = default_issue_severity
    if default_issue_type != 'CODE_SMELL': # CODE_SMELL is the default
        et.SubElement(rule, 'type').text = default_issue_type

    return rule


def add_old_clangtidy_rules(rules):
    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-analyzer-core.DynamicTypePropagation'
    et.SubElement(rule, 'name').text = 'clang-analyzer-core.DynamicTypePropagation'
    et.SubElement(rule, 'description').append(CDATA("""<div class="title">
<p>clang-tidy - clang-analyzer-core.DynamicTypePropagation</p>
</div>
<h1 id="clang-analyzer-core.dynamictypepropagation">clang-analyzer-core.DynamicTypePropagation</h1>
<p>Generate dynamic type information</p>
<h2>References</h2>
<p><a href="https://releases.llvm.org/17.0.1/tools/clang/tools/extra/docs/clang-tidy/checks/clang-analyzer/core.DynamicTypePropagation.html" target="_blank">clang.llvm.org</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'cert-dcl21-cpp'
    et.SubElement(rule, 'name').text = 'cert-dcl21-cpp'
    et.SubElement(rule, 'description').append(CDATA("""<div class="title">
<p>clang-tidy - cert-dcl21-cpp</p>
</div>
<h1 id="cert-dcl21-cpp">cert-dcl21-cpp</h1>
<div class="note">
<div class="title">
<p>Note</p>
</div>
<p>This check is deprecated since it's no longer part of the CERT standard. It will be removed in <code class="interpreted-text" role="program">clang-tidy</code> version 19.</p>
</div>
<p>This check flags postfix <code>operator++</code> and <code>operator--</code> declarations if the return type is not a const object. This also warns if the return type is a reference type.</p>
<p>The object returned by a postfix increment or decrement operator is supposed to be a snapshot of the object's value prior to modification. With such an implementation, any modifications made to the resulting object from calling operator++(int) would be modifying a temporary object. Thus, such an implementation of a postfix increment or decrement operator should instead return a const object, prohibiting accidental mutation of a temporary object. Similarly, it is unexpected for the postfix operator to return a reference to its previous state, and any subsequent modifications would be operating on a stale object.</p>
<p>This check corresponds to the CERT C++ Coding Standard recommendation DCL21-CPP. Overloaded postfix increment and decrement operators should return a const object. However, all of the CERT recommendations have been removed from public view, and so their justification for the behavior of this check requires an account on their wiki to view.</p>
<h2>References</h2>
<p><a href="https://releases.llvm.org/18.1.0/tools/clang/tools/extra/docs/clang-tidy/checks/cert/dcl21-cpp.html" target="_blank">clang.llvm.org</a></p>"""))
    rules.append(rule)


def rstfiles_to_rules_xml(directory, fix_urls):
    sys.stderr.write("[INFO] read .rst files '{}'\n".format(directory))
    rules = et.Element('rules')
    for subdir, _, files in os.walk(directory):
        for f in files:
            ext = os.path.splitext(f)[-1].lower()
            if ext == ".rst" and f != "list.rst":
                rst_file_path = os.path.join(subdir, f)
                rules.append(rstfile_to_rule(rst_file_path, fix_urls))

    add_old_clangtidy_rules(rules)

    rules[:] = sorted(rules, key=lambda rule: rule.find('key').text.casefold())

    sys.stderr.write("[INFO] write .xml file ...\n")
    write_rules_xml(rules, sys.stdout)


def contains_required_fields(entry_value):
    FIELDS = ["!name", "!anonymous", "!superclasses",
              "Class", "DefaultSeverity", "Summary"]
    for field in FIELDS:
        if field not in entry_value:
            return False
    return True

def create_template_rules(rules):
    rule_key = "CustomRuleTemplate"
    rule_name = "Rule template for Clang-Tidy custom rules"
    rule_severity = SEVERITY["SEV_Warning"]["sonarqube_severity"]
    rule_description = """
      <p>Follow these steps to make your custom rules available in SonarQube:</p>
<ol>
  <ol>
    <li>Create a new rule in SonarQube by "copying" this rule template and specify the <code>CheckId</code> of your custom rule, a title, a description, and a default severity.</li>
    <li>Enable the newly created rule in your quality profile</li>
  </ol>
  <li>Relaunch an analysis on your projects, et voilà, your custom rules are executed!</li>
</ol>
      """

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = rule_key
    et.SubElement(rule, 'cardinality').text = "MULTIPLE"
    name = et.SubElement(rule, 'name').text=rule_name
    et.SubElement(rule, 'description').append(CDATA(rule_description))
    rules.append(rule)

def create_clang_default_rules(rules):
    # defaults clang error (not associated with any activation switch): error, fatal error
    rule_key = "clang-diagnostic-error"
    rule_name = "clang-diagnostic-error"
    rule_type = DIAG_CLASS["CLASS_ERROR"]["sonarqube_type"]
    rule_severity = SEVERITY["SEV_Remark"]["sonarqube_severity"]
    rule_description = """
      <p>Default compiler diagnostic for errors without an explicit check name. Compiler error, e.g header file not found.</p>
      """

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = rule_key
    et.SubElement(rule, 'name').text = rule_name
    et.SubElement(rule, 'description').append(CDATA(rule_description))
    et.SubElement(rule, 'severity').text = rule_severity
    et.SubElement(rule, 'type').text = rule_type
    rules.append(rule)

    # defaults clang warning (not associated with any activation switch): warning
    rule_key = "clang-diagnostic-warning"
    rule_name = "clang-diagnostic-warning"
    rule_type = DIAG_CLASS["CLASS_WARNING"]["sonarqube_type"]
    rule_severity = SEVERITY["SEV_Warning"]["sonarqube_severity"]
    rule_description = """
      <p>Default compiler diagnostic for warnings without an explicit check name.</p>
      """

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = rule_key
    et.SubElement(rule, 'name').text = rule_name
    et.SubElement(rule, 'description').append(CDATA(rule_description))
    rules.append(rule)

    # defaults clang issue (not associated with any activation switch): all other levels
    rule_key = "clang-diagnostic-unknown"
    rule_name = "clang-diagnostic-unknown"
    rule_type = DIAG_CLASS["CLASS_REMARK"]["sonarqube_type"]
    rule_severity = SEVERITY["SEV_Remark"]["sonarqube_severity"]
    rule_description = """
      <p>(Unknown) compiler diagnostic without an explicit check name.</p>
      """

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = rule_key
    et.SubElement(rule, 'name').text = rule_name
    et.SubElement(rule, 'description').append(CDATA(rule_description))
    et.SubElement(rule, 'severity').text = rule_severity
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
              "CLASS_ERROR": {"weight": 1, "sonarqube_type": "BUG", "printable": "error"},
              "CLASS_FATAL_ERROR": {"weight": 1, "sonarqube_type": "BUG", "printable": "fatal error"}
              }

# see Severity in JSON
# SEV_Ignored means, that the corresponding diagnostics is disabled in clang by default
# this fact doesn't make any statement about the real severity, so we use INFO
SEVERITY = {"SEV_Remark": {"weight": 0, "sonarqube_severity": "INFO"},
            "SEV_Ignored": {"weight": 1, "sonarqube_severity": "INFO"},
            "SEV_Warning": {"weight": 1, "sonarqube_severity": "INFO"},
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
    for diagnostic in sorted(diagnostics, key=lambda k: k['Summary']):
        diag_class = diagnostic["Class"]["def"]
        all_diagnostics_are_remarks = all_diagnostics_are_remarks and (
            diag_class == "CLASS_REMARK")
        diag_text = diagnostic["Summary"]
        diag_class_printable = DIAG_CLASS[diag_class]["printable"]
        diag_text_escaped = html.escape(diag_text, quote=False)
        html_lines.append("<li>%s: %s</li>" %
                          (diag_class_printable, diag_text_escaped))
    html_lines.append("</ul>")
    html_lines.append("<h2>References</h2>")
    anchor_prefix = "r" if all_diagnostics_are_remarks else "w"
    html_lines.append('<p><a href="http://clang.llvm.org/docs/DiagnosticsReference.html#%s%s" target="_blank">Diagnostic flags in Clang</a></p>' %
                      (anchor_prefix, diag_group_name))
    return "\n".join(html_lines)


def add_old_diagnostics_rules(rules):
    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-auto-import'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-auto-import'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: treating #%select{include|import|include_next|__include_macros}0 as an import of module '%1'</li>
</ul>
<h2>References</h2>
<p><a href="http://clang.llvm.org/docs/DiagnosticsReference.html#wauto-import" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-deprecated-experimental-coroutine'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-deprecated-experimental-coroutine'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: support for std::experimental::%0 will be removed in LLVM 15; use std::%0 instead</li>
</ul>
<h2>References</h2>
<p><a href="https://releases.llvm.org/16.0.0/tools/clang/docs/DiagnosticsReference.html#wdeprecated-experimental-coroutine" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-export-unnamed'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-export-unnamed'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: ISO C++20 does not permit %select{an empty|a static_assert}0 declaration to appear in an export block</li>
<li>warning: ISO C++20 does not permit a declaration that does not introduce any names to be exported</li>
</ul>
<h2>References</h2>
<p><a href="http://clang.llvm.org/docs/DiagnosticsReference.html#wexport-unnamed" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-gnu-empty-initializer'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-gnu-empty-initializer'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: use of GNU empty initializer extension</li>
</ul>
<h2>References</h2>
<p><a href="http://clang.llvm.org/docs/DiagnosticsReference.html#wgnu-empty-initializer" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-ignored-pragma-optimize'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-ignored-pragma-optimize'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: '#pragma optimize' is not supported</li>
</ul>
<h2>References</h2>
<p><a href="http://clang.llvm.org/docs/DiagnosticsReference.html#wignored-pragma-optimize" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-return-std-move'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-return-std-move'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: local variable %0 will be copied despite being %select{returned|thrown}1 by name</li>
</ul>
<h2>References</h2>
<p><a href="http://clang.llvm.org/docs/DiagnosticsReference.html#wreturn-std-move" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-requires-expression'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-requires-expression'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: this requires expression will only be checked for syntactic validity; did you intend to place it in a nested requirement? (add another 'requires' before the expression)</li>
</ul>
<h2>References</h2>
<p><a href="https://releases.llvm.org/13.0.0/tools/clang/docs/DiagnosticsReference.html#wrequires-expression" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-concepts-ts-compat'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-concepts-ts-compat'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: ISO C++20 does not permit the 'bool' keyword after 'concept'</li>
</ul>
<h2>References</h2>
<p><a href="https://releases.llvm.org/15.0.0/tools/clang/docs/DiagnosticsReference.html#wconcepts-ts-compat" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-interrupt-service-routine'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-interrupt-service-routine'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: interrupt service routine should only call a function with attribute 'no_caller_saved_registers'</li>
</ul>
<h2>References</h2>
<p><a href="https://releases.llvm.org/17.0.1/tools/clang/docs/DiagnosticsReference.html#winterrupt-service-routine" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-enum-constexpr-conversion'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-enum-constexpr-conversion'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: integer value %0 is outside the valid range of values [%1, %2] for the enumeration type %3</li>
</ul>
<h2>References</h2>
<p><a href="https://releases.llvm.org/19.1.0/tools/clang/docs/DiagnosticsReference.html#wenum-constexpr-conversion" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "CRITICAL"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-export-using-directive'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-export-using-directive'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: ISO C++20 does not permit using directive to be exported</li>
</ul>
<h2>References</h2>
<p><a href="https://releases.llvm.org/16.0.0/tools/clang/docs/DiagnosticsReference.html#wexport-using-directive" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-fixed-enum-extension'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-fixed-enum-extension'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: enumeration types with a fixed underlying type are a Clang extension</li>
</ul>
<h2>References</h2>
<p><a href="https://releases.llvm.org/19.1.0/tools/clang/docs/DiagnosticsReference.html#wfixed-enum-extension" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-higher-precision-fp'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-higher-precision-fp'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: higher precision floating-point type size has the same size than floating-point type size</li>
</ul>
<h2>References</h2>
<p><a href="https://releases.llvm.org/19.1.0/tools/clang/docs/DiagnosticsReference.html#whigher-precision-fp" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-overriding-t-option'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-overriding-t-option'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: overriding '%0' option with '%1'</li>
</ul>
<h2>References</h2>
<p><a href="https://releases.llvm.org/17.0.1/tools/clang/docs/DiagnosticsReference.html#woverriding-t-option" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-c++23-default-comp-relaxed-constexpr'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-c++23-default-comp-relaxed-constexpr'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: defaulted definition of %select{%sub{select_defaulted_comparison_kind}1|three-way comparison operator}0 that is declared %select{constexpr|consteval}2 but%select{|for which the corresponding implicit 'operator==' }0 invokes a non-constexpr comparison function is a C++23 extension</li>
</ul>
<h2>References</h2>
<p><a href="https://releases.llvm.org/18.1.0/tools/clang/docs/DiagnosticsReference.html#wc-23-default-comp-relaxed-constexpr" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-deprecated-static-analyzer-flag'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-deprecated-static-analyzer-flag'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: analyzer option '%0' is deprecated. This flag will be removed in %1, and passing this option will be an error.</li>
<li>warning: analyzer option '%0' is deprecated. This flag will be removed in %1, and passing this option will be an error. Use '%2' instead.</li>
</ul>
<h2>References</h2>
<p><a href="https://releases.llvm.org/18.1.0/tools/clang/docs/DiagnosticsReference.html#wdeprecated-static-analyzer-flag" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-generic-type-extension'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-generic-type-extension'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: passing a type argument as the first operand to '_Generic' is a Clang extension</li>
</ul>
<h2>References</h2>
<p><a href="https://releases.llvm.org/18.1.0/tools/clang/docs/DiagnosticsReference.html#wgeneric-type-extension" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-gnu-binary-literal'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-gnu-binary-literal'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: binary integer literals are a GNU extension</li>
</ul>
<h2>References</h2>
<p><a href="http://clang.llvm.org/docs/DiagnosticsReference.html#wgnu-binary-literal" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-gnu-offsetof-extensions'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-gnu-offsetof-extensions'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: defining a type within '%select{__builtin_offsetof|offsetof}0' is a Clang extension</li>
</ul>
<h2>References</h2>
<p><a href="http://clang.llvm.org/docs/DiagnosticsReference.html#wgnu-offsetof-extensions" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-knl-knm-isa-support-removed'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-knl-knm-isa-support-removed'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: KNL, KNM related Intel Xeon Phi CPU's specific ISA's supports will be removed in LLVM 19.</li>
</ul>
<h2>References</h2>
<p><a href="https://releases.llvm.org/18.1.0/tools/clang/docs/DiagnosticsReference.html#wknl-knm-isa-support-removed" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)

    rule = et.Element('rule')
    et.SubElement(rule, 'key').text = 'clang-diagnostic-undefined-arm-streaming'
    et.SubElement(rule, 'name').text = 'clang-diagnostic-undefined-arm-streaming'
    et.SubElement(rule, 'description').append(CDATA("""<p>Diagnostic text:</p>
<ul>
<li>warning: builtin call has undefined behaviour when called from a %0 function</li>
</ul>
<h2>References</h2>
<p><a href="https://releases.llvm.org/18.1.0/tools/clang/docs/DiagnosticsReference.html#wundefined-arm-streaming" target="_blank">Diagnostic flags in Clang</a></p>"""))
    et.SubElement(rule, 'severity').text = "INFO"
    rules.append(rule)


def diagnostics_to_rules_xml(json_file):
    rules = et.Element('rules')

    # add a template rule
    create_template_rules(rules)
    # add clang default warnings
    create_clang_default_rules(rules)

    all_deprecated_keys = []
    for _, v in deprecated_diag_keys.items():
        for deprecated_diag_key in v:
            all_deprecated_keys.append(deprecated_diag_key)

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

            if data[diag_group_id]["GroupName"] in all_deprecated_keys:
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
            if data[diag_group_id]["GroupName"] in deprecated_diag_keys:
                for deprecated_diag_key in deprecated_diag_keys[data[diag_group_id]["GroupName"]]:
                    et.SubElement(rule, 'deprecatedKey').text = "clang-diagnostic-" + deprecated_diag_key
            et.SubElement(rule, 'description').append(CDATA(rule_description))
            if rule_severity != 'MAJOR': # MAJOR is the default
                et.SubElement(rule, 'severity').text = rule_severity
            if rule_type != 'CODE_SMELL': # CODE_SMELL is the default
                et.SubElement(rule, 'type').text = rule_type

            rules.append(rule)

    add_old_diagnostics_rules(rules)

    rules[:] = sorted(rules, key=lambda rule: rule.find('key').text.lower())

    write_rules_xml(rules, sys.stdout)

#
# GENERATION OF RULES FROM CLANG-TIDY DOCUMENTATION (RST FILES)
#
# 0. install pandoc
#    see https://pandoc.org/
#
# 1. Check out LLVM (https://github.com/llvm/llvm-project.git)
#
# 2. generate the new version of the rules file
#    python clangtidy_createrules.py rules <src_dir>clang-tools-extra/docs/clang-tidy/checks > clangtidy_new.xml
#
# 3. compare the new version with the old one, extend the old XML
#    python utils_createrules.py comparerules clangtidy.xml clangtidy_new.xml
#    meld clangtidy.xml clangtidy_new.xml.comparable
#
# 4. optional: try to fix local urls in the documentation
#    python clangtidy_createrules.py rules_fixurls <src_dir>clang-tools-extra/docs/clang-tidy/checks > clangtidy_new.xml
#    python utils_createrules.py comparerules clangtidy.xml clangtidy_new.xml
#    meld clangtidy.xml clangtidy_new.xml.comparable
#
# GENERATION OF RULES FROM CLANG DIAGNOSTICS
#
# 0. check out clang source code
#    https://clang.llvm.org/get_started.html
#    perform 1. Get the required tools
#            2. Check out LLVM (https://github.com/llvm/llvm-project.git)
#               - Clang is included
#            3. Build LLVM and Clang
#
# 1. use the TableGen (https://llvm.org/docs/TableGen/) for generation of the list of diagnostics
#
#    cd <src_dir>/clang/include/clang/Basic/
#    <src_dir>/build/bin/llvm-tblgen -dump-json <src_dir>/clang/include/clang/Basic/Diagnostic.td > output.json
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
    print("""Usage: %s rules <path to clang-tidy source directory with RST files>
       %s rules_fixurls <path to clang-tidy source directory with RST files>
       %s diagnostics <path to the JSON diagnostics descriptions>
       see the source code for inline documentation""" % (script_name, script_name, script_name))
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
