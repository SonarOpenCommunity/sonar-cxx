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
import requests
from requests.auth import HTTPBasicAuth


SONAR_URL = ('http://localhost:9000')
SONAR_TOKEN = os.getenv('SONAR_TOKEN', '')

if SONAR_TOKEN:
    SONAR_LOGIN = SONAR_TOKEN
    SONAR_PASSWORD = ''
else:
    SONAR_LOGIN = os.getenv('sonar.login', 'admin')
    SONAR_PASSWORD = os.getenv('sonar.password', 'admin')


def web_api_get(url, log=False):
    try:
        if not url.startswith('http'):
            url = SONAR_URL + url
        response = None
        if log:
            print(f"\n'{url}' response:", flush=True)
        response = requests.get(url, timeout=60, auth=HTTPBasicAuth(SONAR_LOGIN, SONAR_PASSWORD))
        response.raise_for_status()
        if not response.text:
            assert False, f"error web_api_get: no response {url}"
        if log:
            print(response.text, flush=True)
        return response
    except requests.exceptions.RequestException as error:
        if response and response.text:
            assert False, f"error web_api_get: {url} -> {str(error)}, {response.text}"
        else:
            assert False, f"error web_api_get: {url} -> {str(error)}"

def web_api_set(url, payload):
    try:
        url = SONAR_URL + url
        response = None
        response = requests.post(url, payload, timeout=60, auth=HTTPBasicAuth(SONAR_LOGIN, SONAR_PASSWORD))
        response.raise_for_status()
        return response
    except requests.exceptions.RequestException as error:
        if response.text:
            assert False, f"error web_api_set: {url} {str(payload)} -> {str(error)}, {response.text}"
        else:
            assert False, f"error web_api_set: {url} {str(payload)} -> {str(error)}"
