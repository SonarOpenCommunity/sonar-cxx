#!/usr/bin/env python
# -*- mode: python; coding: iso-8859-1 -*-

# Sonar C++ Plugin (Community)
# Copyright (C) Waleri Enns
# dev@sonar.codehaus.org

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

import re

SONAR_ERROR_RE = re.compile(".* ERROR .*")
SONAR_WARN_RE = re.compile(".* WARN .*")
SONAR_WARN_TO_IGNORE_RE = re.compile(".*H2 database should.*|.*Starting search|.*Starting web")


def analyselog(logpath, toignore=None):
    badlines = []
    errors = warnings = 0
    
    toingore_re = None if toignore is None else re.compile(toignore)
    try:
        with open(logpath, "r") as log:
            lines = log.readlines()
            for line in lines:
                if isSonarError(line, toingore_re):
                    badlines.append(line)
                    errors += 1
                elif isSonarWarning(line, toingore_re):
                    badlines.append(line)
                    warnings += 1
    except IOError, e:
        badlines.append(str(e) + "\n")

    return badlines, errors, warnings


def isSonarError(line, toignore_re):
    return (SONAR_ERROR_RE.match(line)
            and (toignore_re and not toignore_re.match(line)))


def isSonarWarning(line, toignore_re):
    return (SONAR_WARN_RE.match(line)
            and not SONAR_WARN_TO_IGNORE_RE.match(line)
            and (toignore_re and not toignore_re.match(line)))


def build_regexp(multiline_str):
    lines = [line for line in multiline_str.split("\n") if line != '']
    return re.compile("|".join(lines))
