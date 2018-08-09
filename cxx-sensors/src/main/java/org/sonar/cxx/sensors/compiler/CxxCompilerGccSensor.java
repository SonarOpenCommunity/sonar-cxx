/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.cxx.sensors.compiler;

import java.util.Optional;
import java.util.function.Predicate;

import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.cxx.CxxLanguage;

public class CxxCompilerGccSensor extends CxxCompilerSensor {

  private class IsGccParserConfigured implements Predicate<Configuration> {
    @Override
    public boolean test(Configuration config) {
      if (!config.hasKey(getReportPathKey())) {
        return false;
      }
      final Optional<String> parserValue = config.get(getLanguage().getPluginProperty(PARSER_KEY_DEF));
      return parserValue.isPresent() && CxxCompilerGccParser.KEY_GCC.equals(parserValue.get());
    }
  };

  public CxxCompilerGccSensor(CxxLanguage language) {
    super(language, REPORT_PATH_KEY, CxxCompilerGccRuleRepository.getRepositoryKey(language),
        new CxxCompilerGccParser());
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name(getLanguage().getName() + " CxxCompilerGccSensor")
      .onlyOnLanguage(getLanguage().getKey())
      .createIssuesForRuleRepository(getRuleRepositoryKey())
      .onlyWhenConfiguration(new IsGccParserConfigured());
  }
}
