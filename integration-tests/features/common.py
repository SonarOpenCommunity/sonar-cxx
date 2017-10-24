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
import os
import sys
import requests
from requests.auth import HTTPBasicAuth
import json
import time

SONAR_ERROR_RE = re.compile(".* ERROR .*")
SONAR_WARN_RE = re.compile(".* WARN .*")
SONAR_WARN_TO_IGNORE_RE = re.compile(".*H2 database should.*|.*Starting search|.*Starting web")
RELPATH_LOG = "logs/sonar.log"

RED = ""
YELLOW = ""
GREEN = ""
RESET = ""
RESET_ALL = ""
BRIGHT = ""
INDENT = "    "

SONAR_URL = "http://localhost:9000"

def get_sonar_log_path(sonarhome):
    return os.path.join(sonarhome, RELPATH_LOG)

def sonar_analysis_finished(logpath):
    urlForChecking = ""

    print(BRIGHT + "    Read Log : " + logpath + RESET_ALL)

    try:
        with open(logpath, "r") as log:
            lines = log.readlines()
            urlForChecking = get_url_from_log(lines)
    except IOError, e:
        badlines.append(str(e) + "\n")


    print(BRIGHT + "     Get Analysis In Background : " + urlForChecking + RESET_ALL)

    if urlForChecking == "":
        return ""

    status = ""
    while True:
        time.sleep(1)
        response = requests.get(urlForChecking)
        task = json.loads(response.text).get("task", None)
        print(BRIGHT + "     CURRENT STATUS : " + task["status"] + RESET_ALL)
        if task["status"] == "IN_PROGRESS" or task["status"] == "PENDING":
            continue

        if task["status"] == "SUCCESS":
            break

        if task["status"] == "FAILED":
            status = "BACKGROUND TASK AS FAILED. CHECK SERVER : " + logpath + ".server"
            break

    serverlogurl = urlForChecking.replace("task?id", "logs?taskId")
    r = requests.get(serverlogurl, auth=HTTPBasicAuth('admin', 'admin'),timeout=10)

    writepath = logpath + ".server"
    f = open(writepath, 'w')
    f.write(r.text)
    f.close()

#    print(BRIGHT + " LOG: " + r.text + RESET_ALL)
        
    return status

def analyse_log(logpath, toignore=None):
    badlines = []
    errors = warnings = 0

    try:
        with open(logpath, "r") as log:
            lines = log.readlines()
            badlines, errors, warnings = analyse_log_lines(lines, toignore)
    except IOError, e:
        badlines.append(str(e) + "\n")

    return badlines, errors, warnings

def get_url_from_log(lines):
    urlForChecking = ""
    for line in lines:
        if "INFO: More about the report processing at" in line:
            urlForChecking = line.split("INFO: More about the report processing at")[1].strip()

        if "INFO  - More about the report processing at" in line:
            urlForChecking = line.split("INFO  - More about the report processing at")[1].strip()

    return urlForChecking

def analyse_log_lines(lines, toignore=None):
    badlines = []
    errors = warnings = 0
    toingore_re = None if toignore is None else re.compile(toignore)
    for line in lines:
        if is_sonar_error(line, toingore_re):
            badlines.append(line)
            errors += 1
        elif is_sonar_warning(line, toingore_re):
            if "JOURNAL_FLUSHER" not in line and "high disk watermark" not in line and "shards will be relocated away from this node" not in line:
                sys.stdout.write("found warning '%s'" % line)
                badlines.append(line)
                warnings += 1

    return badlines, errors, warnings

def is_sonar_error(line, toignore_re):
    return (SONAR_ERROR_RE.match(line)
            and (toignore_re is None or not toignore_re.match(line)))

def is_sonar_warning(line, toignore_re):
    return (SONAR_WARN_RE.match(line)
            and not SONAR_WARN_TO_IGNORE_RE.match(line)
            and (toignore_re is None or not toignore_re.match(line)))

def build_regexp(multiline_str):
    lines = [line for line in multiline_str.split("\n") if line != '']
    return re.compile("|".join(lines))
