#!/usr/bin/env python
# -*- mode: python; coding: iso-8859-1 -*-
#
# Copyright (C) 1990 - 2014 CONTACT Software GmbH
# All rights reserved.
# http://www.contact.de/
#

import os

SONAR_HOME = "/home/joe/sonar"
SONAR_VERSIONS = ["3.7.4", "4.0", "4.1.2", "4.2"]

# for Windows, 32 bit:
#START_SCRIPT = os.path.join(SONAR_HOME, "sonar-%s", "bin", "windows-x86-32", "StartNTService.bat")
#STOP_SCRIPT = os.path.join(SONAR_HOME, "sonar-%s", "bin", "windows-x86-32", "StopNTService.bat")

# for Linux, 32 bit:
#START_SCRIPT = os.path.join(SONAR_HOME, "sonar-%s", "bin", "linux-x86-32", "sonar.sh")
#STOP_SCRIPT = START_SCRIPT

# for Linux, 64 bit:
START_SCRIPT = os.path.join(SONAR_HOME, "sonar-%s", "bin", "linux-x86-64", "sonar.sh")
STOP_SCRIPT = START_SCRIPT
