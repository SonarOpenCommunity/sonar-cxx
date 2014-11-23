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

import os
import subprocess
import sys
import time
import urllib
import platform

from glob import glob
from shutil import copyfile
from common import analyselog

SONAR_URL = "http://localhost:9000"
INDENT = "    "
BASEDIR = os.path.dirname(os.path.realpath(__file__))
JARPATTERN = os.path.join(BASEDIR, "../../sonar-cxx-plugin/target/*SNAPSHOT.jar")
RELPATH_LOG = "logs/sonar.log"
RELPATH_PLUGINS = "extensions/plugins"
didstartsonar = False
PIDSonar = 0


RED = ""
YELLOW = ""
GREEN = ""
RESET = ""
RESET_ALL = ""
BRIGHT = ""
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
    pass


# -----------------------------------------------------------------------------
# HOOKS:
# -----------------------------------------------------------------------------
def before_all(context):
    global didstartsonar
    print BRIGHT + "\nSetting up the test environment" + RESET_ALL

    if not is_webui_up():
        sonarhome = os.environ.get("SONARHOME", None)
        if sonarhome is not None:
            if os.path.exists(sonarhome):
                cleanup(sonarhome)
                if install_plugin(sonarhome):
                    started = start_sonar(sonarhome)
                    if not started:
                        sys.stderr.write(INDENT + RED + "Cannot start SonarQube from '%s', exiting"
                                     % sonarhome + RESET)
                        sys.exit(-1)
                    didstartsonar = True
                    checklogs(sonarhome)
                else:
                    sys.exit(-1)
            else:
                sys.stderr.write(INDENT + RED + "The folder '%s' doesnt exist, exiting"
                                 % sonarhome + RESET)
                sys.exit(-1)
        else:
            sys.stderr.write(RED
                             + INDENT + "Cannot find a SonarQube instance to integrate against.\n"
                             + INDENT + "Make sure there is a SonarQube running on '%s'\n" % SONAR_URL
                             + INDENT + "or pass a path to SonarQube using environment variable 'SONARHOME'\n"
                             + RESET)
            sys.exit(-1)
    else:
        print INDENT + "using the SonarQube already running on '%s'\n\n" % SONAR_URL


def after_all(context):
    if didstartsonar:
        sonarhome = os.environ.get("SONARHOME", None)
        stop_sonar(sonarhome)


# -----------------------------------------------------------------------------
# HELPERS:
# -----------------------------------------------------------------------------
def cleanup(sonarhome):
    sys.stdout.write(INDENT + "cleaning logs ... ")
    sys.stdout.flush()
    try:
        os.remove(sonarlog(sonarhome))
    except OSError:
        pass
    sys.stdout.write(GREEN + "OK\n" + RESET)


def is_installed(sonarhome):
    return os.path.exists(sonarhome)


def install_plugin(sonarhome):
    sys.stdout.write(INDENT + "installing plugin ... ")
    sys.stdout.flush()
    pluginspath = os.path.join(sonarhome, RELPATH_PLUGINS)
    for path in glob(os.path.join(pluginspath, "sonar-cxx-plugin*.jar")):
        os.remove(path)
    jpath = jarpath()
    if not jpath:
        sys.stderr.write(RED + "FAILED: the jar file cannot be found. Make sure you build it.\n")
        sys.stderr.flush()
        return False

    copyfile(jpath, os.path.join(pluginspath, os.path.basename(jpath)))
    sys.stdout.write(GREEN + "OK\n" + RESET)
    return True



def jarpath():
    jars = glob(JARPATTERN)
    if not jars:
        return None
    return os.path.normpath(jars[0])


def start_sonar(sonarhome):
    sys.stdout.write(INDENT + "starting SonarQube ... ")
    sys.stdout.flush()
    now = time.time()
    
    if platform.system() == "Windows" and platform.machine() == "AMD64":
        p = subprocess.Popen("start cmd.exe /c "  + start_script(sonarhome), stdout=subprocess.PIPE, shell=(os.name == "nt"))
        rc = 0

    else:
        rc = subprocess.call(start_script(sonarhome), stdout=subprocess.PIPE, shell=(os.name == "nt"))
                         
    if rc != 0 or not wait_for_sonar(50, is_webui_up):
        sys.stdout.write(RED + "FAILED\n" + RESET)
        return False

    sys.stdout.write(GREEN + "OK, duration: %03.1f s\n" % (time.time() - now)
                     + RESET)
    return True
        
def stop_sonar(sonarhome):

    if platform.system() == "Windows" and platform.machine() == "AMD64":
        return True
    else:    
        sys.stdout.write(INDENT + "stopping SonarQube ... ")
        sys.stdout.flush()    
        rc = subprocess.call(stop_script(sonarhome), stdout=subprocess.PIPE,
                            shell=(os.name == "nt"))
                            
    if rc != 0 or not wait_for_sonar(30, is_webui_down):
        sys.stdout.write(RED + "FAILED\n" + RESET)
        return False

    sys.stdout.write(GREEN + "OK\n\n" + RESET)
    return True
        
def start_script(sonarhome):
    if platform.system() == "Windows" and platform.machine() == "AMD64":
        return  os.path.join(sonarhome, _script_relpath())
    
    return [os.path.join(sonarhome, _script_relpath()), "start"]


def stop_script(sonarhome):
    return [os.path.join(sonarhome, _script_relpath()), "stop"]


def _script_relpath():
    if platform.system() == "Linux" and platform.machine() == "x86_64":
        return "bin/linux-x86-64/sonar.sh"
        
    if platform.system() == "Windows" and platform.machine() == "AMD64":
        return "bin\windows-x86-64\StartSonar.bat"        
        
    return "bin/linux-x86-32/sonar.sh"


def sonarlog(sonarhome):
    return os.path.join(sonarhome, RELPATH_LOG)


def wait_for_sonar(timeout, criteria):
    for _ in range(timeout):
        if criteria():
            return True
        time.sleep(1)
    return False


def is_webui_up():
    try:
        return urllib.urlopen(SONAR_URL).getcode() == 200
    except IOError:
        return False


def is_webui_down():
    try:
        urllib.urlopen(SONAR_URL)
        return False
    except IOError:
        return True


def checklogs(sonarhome):
    sys.stdout.write(INDENT + "logs check ... ")
    sys.stdout.flush()
    badlines, errors, warnings = analyselog(sonarlog(sonarhome))

    reslabel = GREEN + "OK\n"
    if errors > 0 or (errors == 0 and warnings == 0 and len(badlines) > 0):
        reslabel = RED + "FAILED\n"
    elif warnings > 0:
        reslabel = YELLOW + "WARNINGS\n"

    sys.stdout.write(reslabel + RESET)

    if badlines:
        for line in badlines:
            sys.stdout.write(2*INDENT + line)

    summary_msg = "%i errors and %i warnings\n" % (errors, warnings)

    print 2*INDENT + len(summary_msg) * "-"
    print 2*INDENT + summary_msg
    return errors == 0
