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

from __future__ import print_function

import os
import sys
import time
import platform
import requests

from glob import glob
from shutil import copyfile
from shutil import move
from subprocess import Popen, PIPE, check_call
from common import analyse_log, get_sonar_log_file, cleanup_logs, print_logs
from tempfile import mkstemp
from requests.auth import HTTPBasicAuth

SONAR_URL = "http://localhost:9000"
INDENT = "    "
BASEDIR = os.path.dirname(os.path.realpath(__file__))
JAR_CXX_PATTERN1 = os.path.join(BASEDIR, "../../sonar-cxx-plugin/target/*[0-9].jar")
JAR_CXX_PATTERN2 = os.path.join(BASEDIR, "../../sonar-cxx-plugin/target/*SNAPSHOT.jar")
JAR_CXX_PATTERN3 = os.path.join(BASEDIR, "../../sonar-cxx-plugin/target/*RC[0-9].jar")
RELPATH_PLUGINS = "extensions/plugins"
didstartsonar = False
featureno = 0
scenariono = 0
sq_process = None

RED = ""
YELLOW = ""
GREEN = ""
BRIGHT = ""
RESET = ""
RESET_ALL = ""
try:
    import colorama
    colorama.init()
    RED = colorama.Fore.RED
    YELLOW = colorama.Fore.YELLOW
    GREEN = colorama.Fore.GREEN
    RESET = colorama.Fore.RESET
    BRIGHT = colorama.Style.BRIGHT
    RESET_ALL = colorama.Style.RESET_ALL   
except ImportError:
    print("Can't init colorama!")


# -----------------------------------------------------------------------------
# HOOKS:
# -----------------------------------------------------------------------------
def before_all(context):
    global didstartsonar

    sys.stdout.write("\n\n" + BRIGHT + 80 * "-" + "\n")
    sys.stdout.write("starting SonarQube ...\n")
    sys.stdout.write(80 * "-" + RESET_ALL + "\n")    
    sys.stdout.flush() 

    print(BRIGHT + "\nSonarQube already running? " + RESET_ALL)    
    if is_webui_up():
        print("\n" + INDENT + "using the SonarQube already running on '%s'\n\n" % SONAR_URL)
        return

    print(BRIGHT + "\nSetting up the test environment" + RESET_ALL)
    
    sonarhome = os.environ.get("SONARHOME", None)
    if sonarhome is None:
        sys.stderr.write(RED
                         + INDENT + "Cannot find a SonarQube instance to integrate against.\n"
                         + INDENT + "Make sure there is a SonarQube running on '%s'\n" % SONAR_URL
                         + INDENT + "or pass a path to SonarQube using environment variable 'SONARHOME'\n"
                         + RESET)
        sys.exit(1)

    if not os.path.exists(sonarhome):
        sys.stderr.write(INDENT + RED + "The folder '%s' doesnt exist, exiting"
                         % sonarhome + RESET)
        sys.exit(1)

    cleanup_logs(sonarhome)
    if not install_plugin(sonarhome):
        sys.exit(1)

    started = start_sonar(sonarhome)
    if not started:
        sys.stderr.write(INDENT + RED + "Cannot start SonarQube from '%s', exiting\n"
                         % sonarhome + RESET)
        print_logs(sonarhome)
        sys.exit(1)

    didstartsonar = True
    check_logs(sonarhome)
    
    sys.stdout.write("\n\n" + BRIGHT + 80 * "-" + "\n")
    sys.stdout.write("starting tests ...\n")
    sys.stdout.write(80 * "-" + RESET_ALL + "\n")    
    sys.stdout.flush()    

def after_all(context):
    if didstartsonar:
        sys.stdout.write(BRIGHT + 80 * "-" + "\n")
        sys.stdout.write("stopping SonarQube ...\n")
        sys.stdout.write(80 * "-" + RESET_ALL + "\n")    
        sys.stdout.flush()    
        sonarhome = os.environ.get("SONARHOME", None)
        stop_sonar(sonarhome)
        sys.stdout.write(BRIGHT + 80 * "-" + "\n")
        sys.stdout.write("Summary:\n")
        sys.stdout.write(80 * "-" + RESET_ALL + "\n")        
        sys.stdout.flush()

def before_feature(context, feature):
     global featureno
     global scenariono
     context.featurename=feature.name
     featureno += 1
     scenariono = 0
     context.featureno=featureno

def before_scenario(context, scenario):
     global scenariono
     context.scenarioname=scenario.name
     scenariono +=1
     context.scenariono=scenariono


# -----------------------------------------------------------------------------
# HELPERS:
# -----------------------------------------------------------------------------

def is_installed(sonarhome):
    return os.path.exists(sonarhome)


def install_plugin(sonarhome):
    sys.stdout.write(INDENT + "installing plugin ... ")
    sys.stdout.flush()
    pluginspath = os.path.join(sonarhome, RELPATH_PLUGINS)
    for path in glob(os.path.join(pluginspath, "sonar-cxx-plugin*.jar")):
        os.remove(path)
    jpath = jar_cxx_path()
    if not jpath:
        sys.stderr.write(RED + "FAILED: the jar file cannot be found. Make sure you build it.\n" + RESET)
        sys.stderr.flush()
        return False

    copyfile(jpath, os.path.join(pluginspath, os.path.basename(jpath)))

    sys.stdout.write(GREEN + "OK\n" + RESET)
    sys.stdout.flush()
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
    sys.stdout.write(INDENT + "starting SonarQube ... ")
    sys.stdout.flush()
    start_script(sonarhome)    
    now = time.time()
    if not wait_for_sonar(300, is_webui_up):
        sys.stdout.write(RED + "FAILED, duration: %03.1f s\n" % (time.time() - now) + RESET)
        sys.stdout.flush()
        return False

    sys.stdout.write(GREEN + "OK, duration: %03.1f s\n" % (time.time() - now) + RESET)
    sys.stdout.flush()
    return True


def stop_sonar(sonarhome):
    rc = check_call(stop_script(sonarhome))
    if rc != 0 or not wait_for_sonar(300, is_webui_down):
        sys.stdout.write(RED + "FAILED\n" + RESET)
        sys.stdout.flush()
        return False

    sys.stdout.write(GREEN + "OK\n\n" + RESET)
    sys.stdout.flush()
    return True


class UnsupportedPlatform(Exception):
    def __init__(self, msg):
        super(UnsupportedPlatform, self).__init__(msg)


def replace(file_path, pattern, subst):
    #Create temp file
    fh, abs_path = mkstemp()
    with open(abs_path,'w') as new_file:
        with open(file_path) as old_file:
            for line in old_file:
                new_file.write(line.replace(pattern, subst))
    os.close(fh)
    #Remove original file
    os.remove(file_path)
    #Move new file
    move(abs_path, file_path)


def start_script(sonarhome):
    global sq_process
    command = None

    replace(os.path.join(sonarhome, "conf", "wrapper.conf"), "wrapper.java.command=java", "wrapper.java.command=" + (os.environ['JAVA_HOME'] + '/bin/java').replace("\\","/"))

    if platform.system() == "Linux":

        script = linux_script(sonarhome)
        if script:
            command = [script, "start"]
        Popen(command, stdout=PIPE, shell=os.name == "nt")

    elif platform.system() == "Windows":

        replace(os.path.join(sonarhome, "conf", "sonar.properties"), "#sonar.path.data=data", "sonar.path.data=" + os.path.join(sonarhome,"data").replace("\\","/"))
        replace(os.path.join(sonarhome, "conf", "sonar.properties"), "#sonar.path.temp=temp", "sonar.path.temp=" + os.path.join(sonarhome,"temp").replace("\\","/"))
        replace(os.path.join(sonarhome, "conf", "wrapper.conf"), "wrapper.java.additional.1=-Djava.awt.headless=true", "wrapper.java.additional.1=-Djava.awt.headless=true -Djava.io.tmpdir=" + os.path.join(sonarhome,"temp").replace("\\","/"))

        command = ["cmd", "/c", os.path.join(sonarhome, "bin/windows-x86-64/StartSonar.bat")]
        sq_process = Popen(command, stdout=PIPE, shell=os.name == "nt")

    elif platform.system() == "Darwin":

        command = [os.path.join(sonarhome, "bin/macosx-universal-64/sonar.sh"), "start"]
        Popen(command, stdout=PIPE, shell=os.name == "nt")

    if command is None:

        msg = "Dont know how to find the start script for the platform %s-%s" % (platform.system(), platform.machine())
        raise UnsupportedPlatform(msg)

    sys.stdout.write("START ")
    sys.stdout.flush()
    return command


def stop_script(sonarhome):
    global sq_process
    command = None
    
    if platform.system() == "Linux":
        script = linux_script(sonarhome)
        if script:
            command = [script, "stop"]
    elif platform.system() == "Darwin":
        command = [os.path.join(sonarhome, "bin/macosx-universal-64/sonar.sh"), "stop"]
    elif platform.system() == "Windows":
        command = ["TASKKILL", "/F", "/PID", "{}".format(sq_process.pid), "/T"]
        sq_process = None
    if command is None:
        msg = "Dont know how to find the stop script for the platform %s-%s" % (platform.system(), platform.machine())
        raise UnsupportedPlatform(msg)
    
    sys.stdout.write("STOP\n")
    sys.stdout.flush()
    
    return command


def linux_script(sonarhome):
    if platform.machine() == "x86_64":
        return os.path.join(sonarhome, "bin/linux-x86-64/sonar.sh")
    elif platform.machine() == "i686":
        return os.path.join(sonarhome, "bin/linux-x86-32/sonar.sh")


def wait_for_sonar(timeout, criteria):
    sys.stdout.write("WAIT ")
    sys.stdout.flush()
    time.sleep(60)
    for _ in range(timeout):
        if criteria():
            return True
        time.sleep(10)
    return False


def is_webui_up():    
    try:
        response = requests.get(SONAR_URL + "/api/system/status", auth=HTTPBasicAuth('admin', 'admin'))
        response.raise_for_status()
        status = response.json()['status']
        sys.stdout.write(RESET + status + ' ')
        sys.stdout.flush()
        return status == "UP"
    except:
        sys.stdout.write("NOSTATUS ")
        sys.stdout.flush()
        return False


def is_webui_down():
    sys.stdout.write("DOWN? ")
    sys.stdout.flush()
    try:
        response = requests.get(SONAR_URL + "/api/system/status", auth=HTTPBasicAuth('admin', 'admin'))
        response.raise_for_status()
        return False
    except:
        return True


def check_logs(sonarhome):
    sys.stdout.write(INDENT + "logs check ... ")
    sys.stdout.flush()
    badlines, errors, warnings = analyse_log(get_sonar_log_file(sonarhome))

    reslabel = GREEN + "OK\n"
    if errors > 0 or (errors == 0 and warnings == 0 and len(badlines) > 0):
        reslabel = RED + "FAILED\n"
    elif warnings > 0:
        reslabel = YELLOW + "WARNINGS\n"

    sys.stdout.write(reslabel + RESET)

    if badlines:
        for line in badlines:
            sys.stdout.write(line)

    summary_msg = "%i errors and %i warnings\n" % (errors, warnings)
    print(len(summary_msg) * "-")
    print(summary_msg)
    
    sys.stdout.flush()
    return errors == 0
