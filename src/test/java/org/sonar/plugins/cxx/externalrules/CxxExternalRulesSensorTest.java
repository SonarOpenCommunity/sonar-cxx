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

package org.sonar.plugins.cxx.externalrules;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.cxx.TestUtils;

public class CxxExternalRulesSensorTest {

  private CxxExternalRulesSensor sensor;
  private SensorContext context;
  private Project project;
  private RuleFinder ruleFinder;
  private RulesProfile profile;
  private Settings settings;

  @Before
  public void setUp() {
    project = TestUtils.mockProject();
    ruleFinder = TestUtils.mockRuleFinder();
    profile = mock(RulesProfile.class);
    context = mock(SensorContext.class);
    settings = new Settings();
    Resource resourceMock = mock(Resource.class);
    when(context.getResource((Resource) anyObject())).thenReturn(resourceMock);
  }

  @Test
  public void shouldReportCorrectViolations() {
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-result-ok.xml");
    sensor = new CxxExternalRulesSensor(ruleFinder, settings, profile);
    sensor.analyse(project, context);
    verify(context, times(2)).saveViolation(any(Violation.class));
  }

  @Test(expected = SonarException.class)
  public void shouldThrowExceptionWhenReportEmpty() {
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-result-empty.xml");
    sensor = new CxxExternalRulesSensor(ruleFinder, settings, profile);
    sensor.analyse(project, context);
    verify(context, times(0)).saveViolation(any(Violation.class));
  }

  @Test
  public void shouldReportNoViolationsNoReportFound() {
    settings = new Settings();
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/noreport.xml");
    sensor = new CxxExternalRulesSensor(ruleFinder, settings, profile);
    sensor.analyse(project, context);
    verify(context, times(0)).saveViolation(any(Violation.class));
  }

  @Test(expected = SonarException.class)
  public void shouldReportNoViolationsInvalidReport() {
    settings = new Settings();
    settings.setProperty(CxxExternalRulesSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-result-invalid.xml");
    sensor = new CxxExternalRulesSensor(ruleFinder, settings, profile);
    sensor.analyse(project, context);
  }
}
