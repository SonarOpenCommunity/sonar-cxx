#!/usr/bin/env python
# -*- mode: python; coding: utf-8 -*-

# C++ Community Plugin (cxx plugin)
# Copyright (C) Waleri Enns
# Copyright (C) 2010-2024 SonarOpenCommunity
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
import sys
import time
import platform
import subprocess

from glob import glob
from shutil import copyfile
from shutil import move
from tempfile import mkstemp
from common import analyse_log, get_sonar_log_file, cleanup_logs, print_logs
from webapi import web_api_get, web_api_set


BASEDIR = os.path.dirname(os.path.realpath(__file__))
JAR_CXX_PATTERN1 = os.path.join(BASEDIR, '../../sonar-cxx-plugin/target/*[0-9].jar')
JAR_CXX_PATTERN2 = os.path.join(BASEDIR, '../../sonar-cxx-plugin/target/*SNAPSHOT.jar')
JAR_CXX_PATTERN3 = os.path.join(BASEDIR, '../../sonar-cxx-plugin/target/*RC[0-9].jar')
RELPATH_PLUGINS = 'extensions/plugins'
SONAR_STARTED = False
FEATURE_NO = 0
SCENARIO_NO = 0
SONAR_PROCCESS = None


# -----------------------------------------------------------------------------
# HOOKS:
# -----------------------------------------------------------------------------
def before_all(context):
    global SONAR_STARTED

    print('\n\n' + 80 * '-', flush=True)
    print('setup SonarQube ...', flush=True)
    print(80 * '-', flush=True)

    print('\nSonarQube already running? ', flush=True)
    if is_webui_up():
        print('\n\tusing already running SonarQube\n\n', flush=True)
        return

    print('\nSetting up the test environment ...', flush=True)

    sonarhome = os.environ.get('SONARHOME', None)
    if sonarhome is None:
        msg = (f"\tCannot find a SonarQube instance to integrate against.\n"
            f"\tMake sure there is a SonarQube running or pass a path to\n"
            f"\tSonarQube using environment variable 'SONARHOME'\n")
        print(msg, flush=True)
        sys.exit(1)

    if not os.path.exists(sonarhome):
        print(f"\tThe folder '{sonarhome}' doesnt exist, exiting.", flush=True)
        sys.exit(1)

    cleanup_logs(sonarhome)
    if not install_plugin(sonarhome):
        sys.exit(1)

    started = start_sonar(sonarhome)
    if not started:
        print(f"\tCannot start SonarQube from '{sonarhome}', exiting\n", flush=True)
        print_logs(sonarhome)
        sys.exit(1)

    SONAR_STARTED = True
    check_logs(sonarhome)

    try:
        print(f"\nCreate 'SONAR_TOKEN' for SonarScanner ...\n", flush=True)
        url = ('/api/user_tokens/generate')
        payload = {'login': 'admin', 'name': 'SonarScanner', 'type': 'GLOBAL_ANALYSIS_TOKEN'}
        response = web_api_set(url, payload)
        token = response.json()['token']
        os.environ['SONAR_TOKEN'] = token
    except:
        print(f"\tCannot create 'SONAR_TOKEN' for SonarScanner.\n", flush=True)
        sys.exit(1)

    print('\n\n' + 80 * '-', flush=True)
    print('starting tests ...', flush=True)
    print(80 * '-', flush=True)

def after_all(context):
    if SONAR_STARTED:
        print(80 * '-', flush=True)
        print('stopping SonarQube ...', flush=True)
        print(80 * '-', flush=True)

        sonarhome = os.environ.get('SONARHOME', None)
        stop_sonar(sonarhome)
        print(80 * '-', flush=True)
        print('Summary:', flush=True)
        print(80 * '-', flush=True)

def before_feature(context, feature):
    global FEATURE_NO
    global SCENARIO_NO
    context.featurename = feature.name
    FEATURE_NO += 1
    SCENARIO_NO = 0
    context.featureno = FEATURE_NO

def before_scenario(context, scenario):
    global SCENARIO_NO
    context.scenarioname = scenario.name
    SCENARIO_NO +=1
    context.scenariono = SCENARIO_NO


# -----------------------------------------------------------------------------
# HELPERS:
# -----------------------------------------------------------------------------
def is_installed(sonarhome):
    return os.path.exists(sonarhome)


def install_plugin(sonarhome):
    print('\tinstalling plugin ... ', end='', flush=True)
    pluginspath = os.path.join(sonarhome, RELPATH_PLUGINS)
    for path in glob(os.path.join(pluginspath, 'sonar-cxx-plugin*.jar')):
        os.remove(path)
    jpath = jar_cxx_path()
    if not jpath:
        print("FAILED: the jar file cannot be found. Make sure you build it '" + jpath + "'.\n", flush=True)
        return False

    copyfile(jpath, os.path.join(pluginspath, os.path.basename(jpath)))

    print('OK', flush=True)
    return True


def jar_cxx_path():
    jars = glob(JAR_CXX_PATTERN1)
    if jars:
        return os.path.normpath(jars[0])
    jars = glob(JAR_CXX_PATTERN2)
    if jars:
        return os.path.normpath(jars[0])
    jars = glob(JAR_CXX_PATTERN3)
    if jars:
        return os.path.normpath(jars[0])
    return None


def start_sonar(sonarhome):
    print('\tstarting SonarQube ... ', end='', flush=True)
    start_script(sonarhome)
    now = time.time()
    if not wait_for_sonar(300, is_webui_up):
        print('FAILED, duration: %03.1f s\n' % (time.time() - now), flush=True)
        return False

    print('OK, duration: %03.1f s' % (time.time() - now), flush=True)

    # debug only
    #web_api_get('/api/system/health', log=True)
    #web_api_get('/api/system/info', log=True)

    return True


def stop_sonar(sonarhome):
    try:
        subprocess.check_call(stop_script(sonarhome))
    except subprocess.CalledProcessError as error:
        print(f"FAILED, {error}", flush=True)

    if not wait_for_sonar(300, is_webui_down):
        print('FAILED', flush=True)
        return False

    print('OK\n', flush=True)
    return True


class UnsupportedPlatform(Exception):
    def __init__(self, msg):
        super(UnsupportedPlatform, self).__init__(msg)


def replace(file_path, pattern, subst):
    #Create temp file
    file_handle, abs_path = mkstemp()
    with open(abs_path, 'w', encoding='utf8') as new_file:
        with open(file_path, encoding='utf8') as old_file:
            for line in old_file:
                new_file.write(line.replace(pattern, subst))
    os.close(file_handle)
    #Remove original file
    os.remove(file_path)
    #Move new file
    move(abs_path, file_path)


def start_script(sonarhome):
    global SONAR_PROCCESS
    command = None

    # newer SQ versions do not have this 'wrapper.conf' file anymore
    wrapper_config = os.path.join(sonarhome, 'conf', 'wrapper.conf')
    if os.path.exists(wrapper_config):
        replace(wrapper_config,
            'wrapper.java.command=java',
            'wrapper.java.command=' + (os.environ['JAVA_HOME'] + '/bin/java').replace('\\','/'))

    #sonar_properties = os.path.join(sonarhome, 'conf', 'sonar.properties')
    #replace(
    #    sonar_properties,
    #    '#sonar.log.level=INFO',
    #    'sonar.log.level=DEBUG'
    #    )

    if platform.system() == 'Linux':
        command = [os.path.join(sonarhome, 'bin/linux-x86-64/sonar.sh'), 'start']

    elif platform.system() == 'Windows':
        command = ['cmd.exe', '/c', os.path.join(sonarhome, 'bin/windows-x86-64/StartSonar.bat')]

    elif platform.system() == 'Darwin':
        command = [os.path.join(sonarhome, 'bin/macosx-universal-64/sonar.sh'), 'start']

    if command is None:
        msg = f"Dont know how to find the start script for the platform {platform.system()}-{platform.machine()}"
        raise UnsupportedPlatform(msg)

    SONAR_PROCCESS = subprocess.Popen(command)
    print('START ', end='', flush=True)
    return command


def stop_script(sonarhome):
    global SONAR_PROCCESS
    command = None

    if platform.system() == 'Linux':
        command = [os.path.join(sonarhome,'bin/linux-x86-64/sonar.sh'), 'stop']
    elif platform.system() == 'Darwin':
        command = [os.path.join(sonarhome, 'bin/macosx-universal-64/sonar.sh'), 'stop']
    elif platform.system() == 'Windows':
        command = ['TASKKILL', '/F', '/PID', f"{SONAR_PROCCESS.pid}", '/T']
        SONAR_PROCCESS = None

    if command is None:
        msg = f"Dont know how to find the stop script for the platform {platform.system()}-{platform.machine()}"
        raise UnsupportedPlatform(msg)

    print('STOP', flush=True)
    return command


def wait_for_sonar(timeout, criteria):
    print('WAIT ', end='', flush=True)
    time.sleep(60)
    for _ in range(timeout):
        if criteria():
            return True
        time.sleep(10)
    return False


def is_webui_up():
    try:
        response = web_api_get('/api/system/status', log=False) # debug log=True
        status = response.json()['status']
        print(status + ' ', end='', flush=True)
        return status == 'UP'
    except:
        print('NOSTATUS ', flush=True)
        return False


def is_webui_down():
    print('DOWN? ', end='', flush=True)
    try:
        response = web_api_get('/api/system/status')
        return False
    except:
        return True


def check_logs(sonarhome):
    print('\tlogs check ... ', end='', flush=True)
    badlines, errors, warnings = analyse_log(get_sonar_log_file(sonarhome))

    reslabel = 'OK'
    if errors > 0 or (errors == 0 and warnings == 0 and len(badlines) > 0):
        reslabel = 'FAILED'
    elif warnings > 0:
        reslabel = 'WARNINGS'

    print(reslabel, flush=True)

    if badlines:
        for line in badlines:
            print(line, end='', flush=True)

    summary_msg = f"{errors} errors and {warnings} warnings\n"
    print(len(summary_msg) * '-', flush=True)
    print(summary_msg, flush=True)

    return errors == 0
