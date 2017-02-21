/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.cxx;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.bootstrap.ProjectBuilder;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class CxxProjectBuilder extends ProjectBuilder {

  private static final Logger LOG = Loggers.get(CxxProjectBuilder.class);
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

    for (ProjectDefinition definition : context.projectReactor().getProjects()) {
      Map<String,String> propsDef = definition.properties();
      for (Map.Entry<String,String> entry : propsDef.entrySet()) {
        vars.put(entry.getKey(), entry.getValue());
    // replace placeholders
        String newValue = expandVariables(entry.getValue());
        if (!entry.getValue().equals(newValue)) {
          definition.setProperty(entry.getKey(), newValue);
          if (LOG.isDebugEnabled()) {
            LOG.debug("property expansion: project '{}'; key '{}'; value '{}' => '{}'",
                    new Object[]{definition.getKey(), entry.getKey(), entry.getValue(), newValue});
          }
        }
      }
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
