/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.config.Settings;

/**
 * CxxSettings supports variables in configuration files
 *
 * - CxxSettings clones Settings in ctor
 * - format for placeholder is ${xxx} - supported are environment variables,
 *   Java system properties and SonarQube properties
 * - backslashes in values from environment variables and Java system properties
 *   are replaced with slashes to support Windows paths
 */
public class CxxSettings extends Settings {

  private final HashMap<String, String> vars = new HashMap<>();
  private final Pattern regex = Pattern.compile("\\$\\{(.+?)\\}");

  /**
   * Clone settings.
   */
  public CxxSettings(Settings other) {
    super(other);

    Map<String, String> envMap = System.getenv();
    for (Entry<String, String> entry : envMap.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      value = value.replace("\\", "/");
      vars.put(key, value);
    }

    Properties props = System.getProperties();
    for (String key : props.stringPropertyNames()) {
      String value = props.getProperty(key);
      value = value.replace("\\", "/");
      vars.put(key, value);
    }

    vars.putAll(other.getProperties());
  }

  @Override
  protected String getClearString(String key) {
    String value = super.getClearString(key);
    if (value != null) {
      value = expandProperties(value);
    }
    return value;
  }

  /**
   * expand properties in string
   */
  private String expandProperties(String text) {
    Matcher m = regex.matcher(text);
    String result = text;
    while (m.find()) {
      String key = m.group(1);
      String value = vars.get(key);
      if (value != null) {
        result = result.replace(m.group(), value);
      }
    }
    return result;
  }

  @Override
  protected void doOnSetProperty(String key, String value) {
    if (value != null) {
      vars.put(key, value);
    }
  }

  @Override
  protected void doOnRemoveProperty(String key) {
    vars.remove(key);
  }

  @Override
  protected void doOnClearProperties() {
    vars.clear();
  }

}
