#!/usr/bin/env python
# -*- mode: python; coding: utf-8 -*-

# C++ Community Plugin (cxx plugin)
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
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02
import re
import os
import json
import time
from webapi import web_api_get


SONAR_ERROR_RE = re.compile('.* ERROR .*')
SONAR_WARN_RE = re.compile('.* WARN .*')
SONAR_WARN_TO_IGNORE_RE = re.compile('.*H2 database should.*|.*Starting search|.*Starting web')
SONAR_LOG_FOLDER = 'logs'


def get_sonar_log_folder(sonarhome):
    return os.path.join(sonarhome, SONAR_LOG_FOLDER)

def get_sonar_log_file(sonarhome):
    matches = re.search(r'sonarqube-(\d+[.]\d+)', sonarhome)
    if float(matches.group(1)) < 9.6:
        sonar_log_file = 'sonar.' + time.strftime('%Y%m%d') + '.log'
    else:
        sonar_log_file = 'sonar.log'
    return os.path.join(get_sonar_log_folder(sonarhome), sonar_log_file)

def sonar_analysis_finished(logpath):
    url = ''

    print('    Read Log : ' + logpath, flush=True)

    try:
        with open(logpath, 'r', encoding='utf8') as log:
            lines = log.readlines()
            url = get_url_from_log(lines)
    except IOError:
        pass

    print('     Get Analysis In Background : ' + url, flush=True)

    if url == '':
        return ''

    start = time.time()
    status = ''
    while True:
        end = time.time()
        if end - start > 30:
            print('     CURRENT STATUS : timeout, abort', flush=True)
            break

        time.sleep(1)
        response = web_api_get(url, log=False) # debug log=True
        if not response.text:
            print('     CURRENT STATUS : no response', flush=True)
            continue
        task = json.loads(response.text).get('task', None)
        if not task:
            print('     CURRENT STATUS : ?', flush=True)
            continue
        print('     CURRENT STATUS : ' + task['status'], flush=True)
        if task['status'] == 'IN_PROGRESS' or task['status'] == 'PENDING':
            continue

        if task['status'] == 'SUCCESS':
            break
        if task['status'] == 'FAILED':
            status = 'BACKGROUND TASK HAS FAILED. CHECK SERVER : ' + logpath + '.server'
            break

    return status

def cleanup_logs(sonarhome):
    print('\tcleaning logs ... ', end='', flush=True)
    try:
        logpath = get_sonar_log_folder(sonarhome)
        filelist = [ f for f in os.listdir(logpath) if f.endswith('.log') ]
        for filename in filelist:
            os.remove(os.path.join(logpath, filename))
    except OSError:
        pass
    print('OK', flush=True)

def print_logs(sonarhome):
    print('\tprint logs ...', flush=True)
    try:
        logpath = get_sonar_log_folder(sonarhome)
        filelist = [ f for f in os.listdir(logpath) if f.endswith('.log') ]
        for filename in filelist:
            print('\n--- ' + filename + ' ---', flush=True)
            with open(os.path.join(logpath, filename), 'r', encoding='utf8') as file:
                print(file.read(), flush=True)
    except OSError:
        pass

def analyse_log(logpath, toignore=None):
    badlines = []
    errors = warnings = 0

    try:
        with open(logpath, 'r', encoding='utf8') as log:
            lines = log.readlines()
            badlines, errors, warnings = analyse_log_lines(lines, toignore)
    except IOError as error:
        badlines.append(str(error) + '\n')

    return badlines, errors, warnings

def get_url_from_log(lines):
    for line in lines:
        if 'More about the report processing at' in line:
            return line.split('at ')[1].strip()

    return ''

def analyse_log_lines(lines, toignore=None):
    badlines = []
    errors = warnings = 0
    toingore_re = None if toignore is None else re.compile(toignore)
    for line in lines:
        line = line.strip()
        if is_sonar_error(line, toingore_re):
            badlines.append(line + '\n')
            errors += 1
        elif is_sonar_warning(line, toingore_re):
            badlines.append(line + '\n')
            warnings += 1

    return badlines, errors, warnings

def is_sonar_error(line, toignore_re):
    return (SONAR_ERROR_RE.match(line) and (toignore_re is None or not toignore_re.match(line)))

def is_sonar_warning(line, toignore_re):
    return (SONAR_WARN_RE.match(line) and not SONAR_WARN_TO_IGNORE_RE.match(line) and (toignore_re is None or not toignore_re.match(line)))

def build_regexp(multiline_str):
    lines = [line.strip() for line in multiline_str.split('\n') if line != '']
    return re.compile('|'.join(lines))
