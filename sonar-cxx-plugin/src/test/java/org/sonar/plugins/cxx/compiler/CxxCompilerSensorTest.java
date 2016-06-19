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
package org.sonar.plugins.cxx.compiler;

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
import org.hamcrest.CoreMatchers;
import org.junit.Assert;

import org.junit.Assert;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

public class CxxCompilerSensorTest {

  private SensorContext context;
  private Project project;
  private DefaultFileSystem fs;
  private RulesProfile profile;
  private Issuable issuable;
  private ResourcePerspectives perspectives;

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    project = TestUtils.mockProject();
    issuable = TestUtils.mockIssuable();
    perspectives = TestUtils.mockPerspectives(issuable);
    profile = mock(RulesProfile.class);
    context = mock(SensorContext.class);
  }

  @Test
  public void shouldReportACorrectVcViolations() {
    Settings settings = new Settings();
    settings.setProperty("sonar.cxx.compiler.parser", CxxCompilerVcParser.KEY);
    settings.setProperty(CxxCompilerSensor.REPORT_PATH_KEY, "compiler-reports/BuildLog.htm");
    settings.setProperty(CxxCompilerSensor.REPORT_CHARSET_DEF, "UTF-16");
    TestUtils.addInputFile(fs, perspectives, issuable, "zipmanager.cpp");
    CxxCompilerSensor sensor = new CxxCompilerSensor(perspectives, settings, fs, profile);
    sensor.analyse(project, context);
    verify(issuable, times(9)).addIssue(any(Issue.class));
  }

  //@Test @todo
  public void shouldReportCorrectGccViolations() {
    Settings settings = new Settings();
    settings.setProperty("sonar.cxx.compiler.parser", CxxCompilerGccParser.KEY);
    settings.setProperty(CxxCompilerSensor.REPORT_PATH_KEY, "compiler-reports/build.log");
    settings.setProperty(CxxCompilerSensor.REPORT_CHARSET_DEF, "UTF-8");
    TestUtils.addInputFile(fs, perspectives, issuable, "/home/test/src/zip/src/zipmanager.cpp");
    CxxCompilerSensor sensor = new CxxCompilerSensor(perspectives, settings, fs, profile);
    sensor.analyse(project, context);
    verify(issuable, times(4)).addIssue(any(Issue.class));
  }

  @Test
  public void shouldReportBCorrectVcViolations() {
    Settings settings = new Settings();
    settings.setProperty("sonar.cxx.compiler.parser", CxxCompilerVcParser.KEY);
    settings.setProperty(CxxCompilerSensor.REPORT_PATH_KEY, "compiler-reports/VC-report.log");
    settings.setProperty(CxxCompilerSensor.REPORT_CHARSET_DEF, "UTF-8");
    settings.setProperty(CxxCompilerSensor.REPORT_REGEX_DEF, "^.*>(?<filename>.*)\\((?<line>\\d+)\\):\\x20warning\\x20(?<id>C\\d+):(?<message>.*)$");
    TestUtils.addInputFile(fs, perspectives, issuable, "Server/source/zip/zipmanager.cpp");
    CxxCompilerSensor sensor = new CxxCompilerSensor(perspectives, settings, fs, profile);
    sensor.analyse(project, context);
    verify(issuable, times(9)).addIssue(any(Issue.class));
  }
  
  
  @Test
  public void shouldReportWarningsWithoutFileAndLineInformation() {
    Settings settings = new Settings();
    List<CompilerParser.Warning> warnings = Arrays.asList(
        new CompilerParser.Warning("filename1", "line1", "id1", "msg2"),
        new CompilerParser.Warning("filename1", null, "id2", "msg1"),
        new CompilerParser.Warning(null, null, "id3", "msg1"),
        new CompilerParser.Warning(null, null, "id4", null)
        );
    
    MockCxxCompilerSensor sensor = new MockCxxCompilerSensor(perspectives, settings, fs, profile, warnings);
    try {
      sensor.processReport(project, context, null);
    } catch (XMLStreamException e) {
      Assert.fail(e.getMessage());
    }
    
    Assert.assertTrue(warnings.containsAll(sensor.savedWarnings));
    Assert.assertTrue(sensor.savedWarnings.containsAll(warnings));
  }
}
