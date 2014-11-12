#!/usr/bin/env python
# -*- mode: python; coding: iso-8859-1 -*-

# SonarQube Python Plugin
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


import os
import re
import json
import requests
import subprocess
from behave import given, when, then, model
from common import analyselog, build_regexp


TESTDATADIR = os.path.normpath(os.path.join(os.path.realpath(__file__),
                                            "..", "..", "..", "testdata"))
SONAR_URL = "http://localhost:9000"


@given(u'the project "{project}"')
def step_impl(context, project):
    assert os.path.isdir(os.path.join(TESTDATADIR, project))
    context.project = project


@when(u'I run "{command}"')
def step_impl(context, command):
    context.log = "_%s_.log" % context.project
    projecthome = os.path.join(TESTDATADIR, context.project)
    with open(context.log, "w") as logfile:
        rc = subprocess.call(command,
                             cwd=projecthome,
                             stdout=logfile, stderr=logfile,
                             shell=True)
    context.rc = rc


@then(u'the analysis finishes successfully')
def step_impl(context):
    assert context.rc == 0, "Exit code is %i, but should be zero" % context.rc


def _build_regexp(multiline_str):
    lines = multiline_str.split(r"\n")
    print lines
    

@then(u'the analysis log contains no error/warning messages except those matching')
def step_impl(context):
    ignore_re = build_regexp(context.text)
    badlines, _errors, _warnings = analyselog(context.log, ignore_re)
    
    assert len(badlines) == 0,\
        ("Found following errors and/or warnings lines in the logfile:\n"
         + "".join(badlines)
         + "For details see %s" % context.log)


@then(u'the following metrics have following values')
def step_impl(context):
    def _toSimpleDict(measures):
        if isinstance(measures, model.Table):
            return {row["metric"]: None if row["value"] == "None" else float(row["value"])
                    for row in measures}
        else:
            return {measure["key"]: measure["val"] for measure in measures}

    def diffMeasures(expected, measured):
        difflist = []
        for metric, value_expected in expected.iteritems():
            value_measured = measured.get(metric, None)
            if value_expected != value_measured:
                difflist.append("\t%s is actually %s" % (metric, str(value_measured)))
        return "\n".join(difflist)

    exp_measures = _toSimpleDict(context.table)
    metrics_to_query = exp_measures.keys()

    try:
        url = (SONAR_URL + "/api/resources?resource=" + context.project + "&metrics="
               + ",".join(metrics_to_query))
        response = requests.get(url)
        got_measures = {}
        json_measures = json.loads(response.text)[0].get("msr", None)
        if json_measures is not None:
            got_measures = _toSimpleDict(json_measures)
        diff = diffMeasures(exp_measures, got_measures)
    except requests.exceptions.ConnectionError, e:
        assert False, "cannot query the metrics, details: %s" % str(e)

    assert diff == "", "\n" + diff


@then(u'the analysis breaks')
def step_impl(context):
    assert context.rc != 0, "Exit code is %i, but should be non zero" % context.rc


@then(u'the analysis log should contain a line matching')
def step_impl(context):
    pattern = re.compile(context.text)
    with open(context.log) as logfo:
        for line in logfo:
            if pattern.match(line):
                return True
    return False
