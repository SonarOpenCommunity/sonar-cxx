#!/usr/bin/env python
# -*- mode: python; coding: utf-8 -*-

# SonarQube Python Plugin
# Copyright (C) Waleri Enns, Günter Wirth
# Copyright (C) 2010-2025 SonarOpenCommunity
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
import os
import re
import io
import platform
import subprocess
from behave import given, when, then, model # pylint: disable=no-name-in-module
from common import analyse_log, build_regexp, get_sonar_log_file, analyse_log_lines, sonar_analysis_finished
from webapi import web_api_get, web_api_set

TESTDATADIR = os.path.normpath(os.path.join(os.path.realpath(__file__),
                                            '..', '..', '..', 'testdata'))
TEST_METRICS_ORDER = ['tests',
    'test_failures',
    'test_errors',
    'skipped_tests',
    'test_success_density',
    'test_execution time']


@given('the project "{project}"')
def step_impl(context, project):
    assert os.path.isdir(os.path.join(TESTDATADIR, project))
    context.project = project
    context.profile_key = None

    url = ('/api/qualityprofiles/search')
    response = web_api_get(url)
    profiles = _get_json(response)['profiles']
    data = _got_key_from_quality_profile(profiles)
    default_profile_key = '?'
    for key, name in data.items():
        if name == 'Sonar way - cxx':
            default_profile_key = key

    url = ('/api/qualityprofiles/set_default')
    payload = {'language': 'cxx', 'qualityProfile': 'Sonar way'}
    web_api_set(url, payload)

    copy_profile_key = None
    for key, name in data.items():
        if name == 'Sonar way copy':
            copy_profile_key = key

    if copy_profile_key:
        url = ('/api/qualityprofiles/delete')
        payload = {'language': 'cxx', 'qualityProfile': 'Sonar way copy'}
        web_api_set(url, payload)

    url = ('/api/qualityprofiles/copy')
    payload = {'fromKey': default_profile_key, 'toName': 'Sonar way copy'}
    web_api_set(url, payload)

    url = ('/api/qualityprofiles/search')
    response = web_api_get(url)
    profiles = _get_json(response)['profiles']
    data = _got_key_from_quality_profile(profiles)
    for key, name in data.items():
        if name == 'Sonar way copy - cxx':
            context.profile_key = key

    url = ('/api/qualityprofiles/set_default')
    payload = {'language': 'cxx', 'qualityProfile': 'Sonar way copy'}
    web_api_set(url, payload)


@given('platform is not "{plat}"')
def step_impl(context, plat):
    if platform.system() == plat:
        context.scenario.skip(reason='scenario meant to run only in specified platform')


@given('platform is "{plat}"')
def step_impl(context, plat):
    if platform.system() != plat:
        context.scenario.skip(reason='scenario meant to run only in specified platform')


@given('declared suffixes for cxx files to analyze are "{extensions}"')
def step_impl(context, extensions):
    assert context.profile_key != '', f"PROFILE KEY NOT FOUND: {str(context.profile_key)}"
    url = ('/api/settings/reset')
    web_api_set(url, {'keys': 'sonar.cxx.file.suffixes'})
    url = ('/api/settings/set')
    extensionlist = extensions.split(',')
    payload = dict()
    payload['key'] = 'sonar.cxx.file.suffixes'
    for extension in extensionlist:
        if 'values' in payload:
            payload['values'].append(extension)
        else:
            payload['values'] = [extension]
    web_api_set(url, payload)


@given('rule "{rule}" with params "{params}" is activated')
def step_impl(context, rule, params):
    assert context.profile_key != '', f"PROFILE KEY NOT FOUND: {str(context.profile_key)}"
    # deactivate first to be able to set params
    url = ('/api/qualityprofiles/deactivate_rule')
    payload = {'key': context.profile_key, 'rule': rule}
    web_api_set(url, payload)
    url = ('/api/qualityprofiles/activate_rule')
    payload = {'key': context.profile_key, 'rule': rule, 'params': params}
    web_api_set(url, payload)


@given('rule "{rule}" is activated')
def step_impl(context, rule):
    assert context.profile_key != '', f"PROFILE KEY NOT FOUND: {str(context.profile_key)}"
    url = ('/api/qualityprofiles/activate_rule')
    payload = {'key': context.profile_key, 'rule': rule}
    web_api_set(url, payload)


@given('rule "{rule}" is deactivated')
def step_impl(context, rule):
    assert context.profile_key != '', f"PROFILE KEY NOT FOUND: {str(context.profile_key)}"
    url = ('/api/qualityprofiles/deactivate_rule')
    payload = {'key': context.profile_key, 'rule': rule}
    web_api_set(url, payload)


@given('custom rule "{rule}" from rule template "{templaterule}" in repository "{repository}" is activated')
def step_impl(context, rule, templaterule, repository):
    assert context.profile_key != '', f"PROFILE KEY NOT FOUND: {str(context.profile_key)}"
    url = ('/api/rules/create')
    payload = {'customKey': rule, 'html_description': 'nodesc', 'name': rule, 'templateKey': templaterule, 'markdownDescription': 'nodesc'}
    web_api_set(url, payload)
    url = ('/api/qualityprofiles/activate_rule')
    payload = {'key': context.profile_key, 'rule': repository + ':' + rule}
    web_api_set(url, payload)


@given('custom rule "{rule}" with params "{params}" is updated')
def step_impl(context, rule, params):
    assert context.profile_key != '', f"PROFILE KEY NOT FOUND: {str(context.profile_key)}"
    url = ('/api/rules/update')
    payload = {'key': rule, 'params': params}
    web_api_set(url, payload)


@then('custom rule "{rule}" is deleted')
def step_impl(context, rule):
    url = ('/api/rules/delete')
    payload = {'key': rule}
    web_api_set(url, payload)


@then('the analysis finishes successfully')
def step_impl(context):
    assert context.rc == 0, f"Exit code is {context.rc}, but should be zero"


@then('the analysis in server has completed')
def step_impl(context):
    assert sonar_analysis_finished(context.log) == '', ('Analysis in Background Task Failed')


@then('the analysis log contains no error/warning messages except those matching')
def step_impl(context):
    ignore_re = build_regexp(context.text)
    badlines, _errors, _warnings = analyse_log(context.log, ignore_re)

    assert len(badlines) == 0,\
        ('Found additonal errors and/or warnings lines in the logfile:\n' + ''.join(badlines) + '\nFor details see ' + context.log)


@then('the analysis log contains no error/warning messages')
def step_impl(context):
    badlines, _errors, _warnings = analyse_log(context.log)

    assert len(badlines) == 0,\
        ('Found following errors and/or warnings lines in the logfile:\n' + ''.join(badlines) + '\nFor details see ' + context.log)


@then('the server log (if locatable) contains no error/warning messages')
def step_impl(context):
    if context.serverlogfd is not None:
        lines = context.serverlogfd.readlines()
        badlines, _errors, _warnings = analyse_log_lines(lines)

        assert len(badlines) == 0,\
            ('Found following errors and/or warnings lines in the logfile:\n' + ''.join(badlines) + '\nFor details see ' + context.serverlog)


@then('the number of violations fed is {number}')
def step_impl(context, number):
    exp_measures = {'violations': float(number)}
    _assert_measures(context.project, exp_measures)


@then('the following metrics have following values')
def step_impl(context):
    exp_measures = _exp_measures_to_dict(context.table)
    _assert_measures(context.project, exp_measures)


@then('the test related metrics have following values: {values}')
def step_impl(context, values):
    parsed_values = [value.strip() for value in values.split(',')]
    exp_measures = _exp_measures_to_dict(parsed_values)
    _assert_measures(context.project, exp_measures)


@then('the analysis breaks')
def step_impl(context):
    assert context.rc != 0, f"Exit code is {context.rc}, but should be non zero"


@then('the analysis log contains a line matching')
def step_impl(context):
    assert _contains_line_matching(context.log, context.text), f"The analysis log does not contain a line matching '{context.text}'"


@when('I run "{command}"')
def step_impl(context, command):
    _run_command(context, command)


@when('I run sonar-scanner with "{params}"')
def step_impl(context, params):
    _run_command(context, 'sonar-scanner -Dsonar.host.url=http://localhost:9000 ' + params) # use token from SONAR_TOKEN


@when('I run sonar-scanner with following options')
def step_impl(context):
    arguments = [line for line in context.text.split('\n') if line != '']
    command = 'sonar-scanner -Dsonar.host.url=http://localhost:9000 ' + ' '.join(arguments) # use token from SONAR_TOKEN
    _run_command(context, command)



def _get_json(response):
    try:
        return response.json()
    except ValueError as error:
        assert False, f"error _get_json: {str(error)}, {response.text}"

def _exp_measures_to_dict(measures):
    def convertvalue(value):
        return None if value == 'None' else float(value)
    res = {}
    if isinstance(measures, model.Table):
        res = {row['metric']: convertvalue(row['value']) for row in measures}
    elif isinstance(measures, list):
        assert len(measures) == len(TEST_METRICS_ORDER)
        res = {}
        for i in range(len(measures) - 1):
            res[TEST_METRICS_ORDER[i]] = convertvalue(measures[i])
    return res

def _got_key_from_quality_profile(measures):
    return {measure['key']: measure['name'] + ' - ' + measure['language'] for measure in measures}

def _got_measures_to_dict(measures):
    return {measure['metric']: measure['value'] for measure in measures}

def _diff_measures(expected, measured):
    difflist = []
    for metric, value_expected in expected.items():
        value_measured = measured.get(metric, None)
        append = False
        try:
            if float(value_expected) != float(value_measured):
                append = True
        except:
            if value_expected != value_measured:
                append = True
        if append:
            difflist.append(f"\t{metric} is actually {str(value_measured)} [expected: {str(value_expected)}]")

    return '\n'.join(difflist)

def _contains_line_matching(filepath, pattern):
    pat = re.compile(pattern.strip())
    with io.open(filepath, mode='rt', encoding='utf8') as logfo:
        for line in logfo:
            if pat.search(line.rstrip('\r\n')):
                return True

    return False

def _assert_measures(project, measures):
    metrics_to_query = list(measures.keys())
    url = ('/api/measures/component?component=' + project + '&metricKeys=' + ','.join(metrics_to_query))

    print('\nGet measures with query : ' + url, flush=True)
    response = web_api_get(url)

    got_measures = {}
    json_measures = _get_json(response)['component']['measures']
    got_measures = _got_measures_to_dict(json_measures)

    diff = _diff_measures(measures, got_measures)
    assert diff == '', '\n' + diff

def _run_command(context, command):
    context.log = f"_{context.project}_{context.scenariono}.log"

    sonarhome = os.environ.get('SONARHOME', None)
    if sonarhome:
        context.serverlog = get_sonar_log_file(sonarhome)
        if getattr(context, 'serverlogfd', None) is not None:
            context.serverlogfd.close()
        context.serverlogfd = open(context.serverlog, 'r', encoding='utf8')
        context.serverlogfd.seek(0, 2)
    else:
        context.serverlogfd = None

    projecthome = os.path.join(TESTDATADIR, context.project)

    with open(context.log, 'w', encoding='utf8') as logfile:
        proc = subprocess.Popen(command,
                                shell=True,
                                cwd=projecthome,
                                stdout=logfile,
                                stderr=subprocess.STDOUT)
        proc.communicate()

    # print errors and warnings from log file
    if proc.returncode != 0:
        print('cmd: ' + command, flush=True)
        with open(context.log, 'r', encoding='utf8') as log:
            for line in log:
                if  'WARN' in line or 'ERROR' in line:
                    print(line, flush=True)

    context.rc = proc.returncode
