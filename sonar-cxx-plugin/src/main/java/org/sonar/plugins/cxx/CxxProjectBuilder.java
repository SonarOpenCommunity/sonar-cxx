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
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.bootstrap.ProjectBuilder;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.slf4j.LoggerFactory;

public class CxxProjectBuilder extends ProjectBuilder {

  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger("CxxProjectBuilder");
  private final HashMap<String, String> vars = new HashMap<>();
  private final Pattern regex = Pattern.compile("\\$\\{(.+?)\\}");

  public CxxProjectBuilder() {
    super();
    LOG.debug("using CxxProjectBuilder");
  }

  /**
   * additional support of variables in configuration files
   *
   * - format for placeholder is ${xxx}
   * - supported are environment variables, Java system properties and SonarQube properties
   */
  @Override
  public void build(Context context) {
    super.build(context);

    ProjectDefinition definition = context.projectReactor().getRoot();

    // collect all properties
    Map<String, String> envMap = System.getenv();
    for (Map.Entry<String, String> entry : envMap.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      vars.put(key, value);
    }

    Properties props = System.getProperties();
    for (String key : props.stringPropertyNames()) {
      String value = props.getProperty(key);
      vars.put(key, value);
    }

    props = definition.getProperties();
    for (String key : props.stringPropertyNames()) {
      String value = props.getProperty(key);
      vars.put(key, value);
    }

    // replace placeholders
    for (String key : props.stringPropertyNames()) {
      String oldValue = props.getProperty(key);
      String newValue = expandVariables(oldValue);
      if (!oldValue.equals(newValue)) {
        definition.setProperty(key, newValue);
        LOG.debug("property expansion: key '{}'; value '{}' => '{}'", new Object[]{key, oldValue, newValue});
      }
    }
    
    // add list of available property keys
    if (LOG.isDebugEnabled()) {
      StringBuilder sb = new StringBuilder("analysis parameters:\n");
      for (String key : props.stringPropertyNames()) {
        sb.append("   ");
        sb.append(key);
        sb.append("=");
        sb.append(props.getProperty(key));
        sb.append("\n");
      }
      LOG.debug(sb.toString());
    }
  }

  /**
   * expand variables in string
   */
  private String expandVariables(String text) {
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

}
