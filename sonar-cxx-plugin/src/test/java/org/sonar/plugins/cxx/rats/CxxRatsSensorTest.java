/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
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
package org.sonar.plugins.cxx.rats;

import org.sonar.api.batch.SensorContext; //@todo deprecated
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.component.ResourcePerspectives; //@todo deprecated
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project; //@todo deprecated
import org.sonar.plugins.cxx.TestUtils;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CxxRatsSensorTest {

  private CxxRatsSensor sensor;
  private SensorContext context; //@todo deprecated
  private Project project; //@todo deprecated
  private DefaultFileSystem fs;
  private Issuable issuable;
  private ResourcePerspectives perspectives; //@todo deprecated

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    project = TestUtils.mockProject();
    issuable = TestUtils.mockIssuable();
    perspectives = TestUtils.mockPerspectives(issuable);
    Settings settings = new Settings();
    settings.setProperty(CxxRatsSensor.REPORT_PATH_KEY, "rats-reports/rats-result-*.xml");
    sensor = new CxxRatsSensor(perspectives, settings, fs, mock(RulesProfile.class));
    context = mock(SensorContext.class); //@todo deprecated
  }

  @Test
  public void shouldReportCorrectViolations() {
    TestUtils.addInputFile(fs, perspectives, issuable, "sources/utils/code_chunks.cpp");
    TestUtils.addInputFile(fs, perspectives, issuable, "report.c");
    sensor.analyse(project, context);
    verify(issuable, times(5)).addIssue(any(Issue.class));
  }
}
