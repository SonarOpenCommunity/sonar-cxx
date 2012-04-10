/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.Extension;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.plugins.cxx.cppcheck.CxxCppCheckProfile;
import org.sonar.plugins.cxx.cppcheck.CxxCppCheckRuleRepository;
import org.sonar.plugins.cxx.cppcheck.CxxCppCheckSensor;
import org.sonar.plugins.cxx.cppncss.CxxCppNcssSensor;
import org.sonar.plugins.cxx.gcovr.CxxGcovrSensor;
import org.sonar.plugins.cxx.rats.CxxRatsProfile;
import org.sonar.plugins.cxx.rats.CxxRatsRuleRepository;
import org.sonar.plugins.cxx.rats.CxxRatsSensor;
import org.sonar.plugins.cxx.valgrind.CxxValgrindProfile;
import org.sonar.plugins.cxx.valgrind.CxxValgrindRuleRepository;
import org.sonar.plugins.cxx.valgrind.CxxValgrindSensor;
import org.sonar.plugins.cxx.veraxx.CxxVeraxxProfile;
import org.sonar.plugins.cxx.veraxx.CxxVeraxxRuleRepository;
import org.sonar.plugins.cxx.veraxx.CxxVeraxxSensor;
import org.sonar.plugins.cxx.xunit.CxxXunitSensor;
import org.sonar.plugins.cxx.squid.CxxSquidSensor;

@Properties({
    @Property(
      key = CxxPlugin.FILE_SUFFIXES_KEY,
      defaultValue = CxxLanguage.DEFAULT_FILE_SUFFIXES,
      name = "File suffixes",
      description = "Comma-separated list of suffixes for files to analyze. Leave empty to use the default.",
      global = true,
      project = true),
    @Property(
      key = CxxCppCheckSensor.REPORT_PATH_KEY,
      defaultValue = "",
      name = "Path to cppcheck report(s)",
      description = "Relative to projects' root. Ant patterns are accepted",
      global = false,
      project = true),
    @Property(
      key = CxxCppNcssSensor.REPORT_PATH_KEY,
      defaultValue = "",
      name = "Path to cppncss report(s)",
      description = "Relative to projects' root. Ant patterns are accepted",
      global = false,
      project = true),
    @Property(
      key = CxxGcovrSensor.REPORT_PATH_KEY,
      defaultValue = "",
      name = "Path to gcovr report(s)",
      description = "Relative to projects' root. Ant patterns are accepted",
      global = false,
      project = true),
    @Property(
      key = CxxRatsSensor.REPORT_PATH_KEY,
      defaultValue = "",
      name = "Path to rats report(s)",
      description = "Relative to projects' root. Ant patterns are accepted",
      global = false,
      project = true),
    @Property(
      key = CxxValgrindSensor.REPORT_PATH_KEY,
      defaultValue = "",
      name = "Path to valgrind report(s)",
      description = "Relative to projects' root. Ant patterns are accepted",
      global = false,
      project = true),
    @Property(
      key = CxxVeraxxSensor.REPORT_PATH_KEY,
      defaultValue = "",
      name = "Path to vera++ report(s)",
      description = "Relative to projects' root. Ant patterns are accepted",
      global = false,
      project = true),
    @Property(
      key = CxxXunitSensor.REPORT_PATH_KEY,
      defaultValue = "",
      name = "Path to unit test execution report(s)",
      description = "Relative to projects' root. Ant patterns are accepted",
      global = false,
      project = true),
    @Property(
      key = CxxXunitSensor.XSLT_URL_KEY,
      defaultValue = "",
      name = "URL of the xslt transformer",
      description = "TODO",
      global = false,
      project = true)
      })
public final class CxxPlugin implements Plugin {
  static final String FILE_SUFFIXES_KEY = "sonar.cxx.suffixes";

  /**
   * @deprecated this is not used anymore
   */
  @Deprecated
  public String getKey() {
    return "C++ plugin";
  }

  /**
   * @deprecated this is not used anymore
   */
  @Deprecated
  public String getName() {
    return "C++ plugin";
  }

  /**
   * @deprecated this is not used anymore
   */
  @Deprecated
  public String getDescription() {
    return "Add support for C++ language.";
  }

  /**
   * {@inheritDoc}
   */
  public List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> l = new ArrayList<Class<? extends Extension>>();
    l.add(CxxLanguage.class);
    l.add(CxxSourceImporter.class);
    l.add(CxxColorizer.class);
    l.add(CxxSquidSensor.class);
    l.add(CxxCpdMapping.class);
    l.add(CxxRatsRuleRepository.class);
    l.add(CxxRatsSensor.class);
    l.add(CxxRatsProfile.class);
    l.add(CxxXunitSensor.class);
    l.add(CxxGcovrSensor.class);
    l.add(CxxCppCheckRuleRepository.class);
    l.add(CxxCppCheckSensor.class);
    l.add(CxxCppCheckProfile.class);
    l.add(CxxCppNcssSensor.class);
    l.add(CxxVeraxxRuleRepository.class);
    l.add(CxxVeraxxSensor.class);
    l.add(CxxVeraxxProfile.class);
    l.add(CxxValgrindRuleRepository.class);
    l.add(CxxValgrindSensor.class);
    l.add(CxxValgrindProfile.class);
    return l;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
