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
package org.sonar.cxx.sensors.utils;

import org.sonar.cxx.sensors.utils.CxxReportSensor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.sonar.api.config.internal.MapSettings;

public class CxxReportSensor_getReports_Test {

  private static final String REPORT_PATH_KEY = "sonar.cxx.cppcheck.reportPath";
  private MapSettings settings = new MapSettings();
  
  @Rule
  public TemporaryFolder base = new TemporaryFolder();

  @Test
  public void getReports_patternMatching() throws java.io.IOException, java.lang.InterruptedException {
    List<String[]> examples = new LinkedList<>();

    //                        "pattern",      "matches",   "matches not"
    // relative
    examples.add(new String[]{"A.ext",        "A.ext",     "dir/B.ext"});
    examples.add(new String[]{"dir/A.ext",    "dir/A.ext", "A.ext,dir/B.ext"});
    examples.add(new String[]{"dir/../A.ext", "A.ext",     "B.ext,dir/A.ext"});
    examples.add(new String[]{"./A.ext",      "A.ext",     "B.ext"});
    examples.add(new String[]{"./A.ext",      "A.ext",     "B.ext"});
    // empty
    examples.add(new String[]{"", "", ""});
    // question mark glob
    examples.add(new String[]{"A?.ext",       "AA.ext,AB.ext", "B.ext"});
    // multi-char glob
    examples.add(new String[]{"A*.ext",       "A.ext,AAA.ext", "B.ext"});
    // multi-dir glob
    examples.add(new String[]{"**/A.ext",     "A.ext,dir/A.ext,dir/subdir/A.ext", "B.ext,dir/B.ext"});
    examples.add(new String[]{"dir/**/A.ext", "dir/A.ext,dir/subdir/A.ext",       "A.ext,dir/B.ext,dir/subdir/B.ext"});

    String pattern, match, allpaths;
    List<File> reports;
    for (String[] example : examples) {
      pattern = example[0];
      match = example[1];
      allpaths = String.join(",", Arrays.copyOfRange(example, 1, 3));
      setupExample(allpaths);

      settings.setProperty(REPORT_PATH_KEY, pattern);
      reports = CxxReportSensor.getReports(settings.asConfig(), base.getRoot(), REPORT_PATH_KEY);
      assertMatch(reports, match, example[0]);
      deleteExample(base.getRoot());
    }

    // TODO: some special windows cases?
  }

  private void setupExample(String pathes) throws java.io.IOException {
    String[] parsedPaths = pathes.split(",");
    for (String path : parsedPaths) {
      path = path.trim();
      if (!path.isEmpty()) {
        FileUtils.touch(new File(base.getRoot(), path));
      }
    }
  }

  private void deleteExample(File dir) throws java.io.IOException {
    FileUtils.cleanDirectory(dir);
  }

  private void assertMatch(List<File> real, String expected, String pattern) {
    String[] parsedPaths = expected.split(",");
    List<File> expectedFiles = new LinkedList<>();
    for (String path : parsedPaths) {
      path = path.trim();
      if (!path.isEmpty()) {
        expectedFiles.add(new File(base.getRoot(), path));
      }
    }

    Set<File> realSet = new TreeSet<>(real);
    Set<File> expectedSet = new TreeSet<>(expectedFiles);

    assertEquals("Failed for pattern: " + pattern, expectedSet, realSet);
  }

  @Test
  public void testAbsoluteInsideBasedir() throws IOException {
    File absReportFile = new File(base.getRoot(), "path/to/report.xml").getAbsoluteFile();
    FileUtils.touch(absReportFile);

    settings.setProperty(REPORT_PATH_KEY, absReportFile.toString());

    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), base.getRoot(), REPORT_PATH_KEY);
    assertEquals(1, reports.size());
  }

  @Test
  public void testAbsoluteOutsideBasedir() {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    File absReportFile = new File(absReportsProject, "cppcheck-reports/cppcheck-result-SAMPLE-V2.xml");

    settings.setProperty(REPORT_PATH_KEY, absReportFile.toString());

    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), base.getRoot(), REPORT_PATH_KEY);
    assertEquals(1, reports.size());
  }

  @Test
  public void testAbsoluteOutsideBasedirWithGlobbing() {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    File absReportFile = new File(absReportsProject, "cppcheck-reports/cppcheck-result-SAMPLE-*.xml");

    settings.setProperty(REPORT_PATH_KEY, absReportFile.toString());

    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), base.getRoot(), REPORT_PATH_KEY);
    assertEquals(2, reports.size());
  }

  @Test
  public void testAbsoluteOutsideBasedirAndRelative() throws IOException {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    File absReportFile = new File(absReportsProject, "cppcheck-reports/cppcheck-result-SAMPLE-V2.xml");

    String relativeReport = "path/to/report.xml";
    FileUtils.touch(new File(base.getRoot(), relativeReport));

    settings.setProperty(REPORT_PATH_KEY, absReportFile.toString() + "," + relativeReport);

    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), base.getRoot(), REPORT_PATH_KEY);
    assertEquals(2, reports.size());
  }

  @Test
  public void testAbsoluteOutsideBasedirWithGlobbingAndRelativeWithGlobbing() throws IOException {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    File absReportFile = new File(absReportsProject, "cppcheck-reports/cppcheck-result-SAMPLE-*.xml");

    FileUtils.touch(new File(base.getRoot(), "report.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/supercoolreport.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/a/report.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/some/reports/1.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/some/reports/2.xml"));
    FileUtils.touch(new File(base.getRoot(), "some/reports/a"));
    FileUtils.touch(new File(base.getRoot(), "some/reports/b"));

    settings.setProperty(REPORT_PATH_KEY, absReportFile.toString() + ",**/*.xml");

    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), base.getRoot(), REPORT_PATH_KEY);
    assertEquals(7, reports.size());
  }

  @Test
  public void testAbsoluteOutsideBasedirWithGlobbingAndNestedRelativeWithGlobbing() throws IOException {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    File absReportFile = new File(absReportsProject, "cppcheck-reports/cppcheck-result-SAMPLE-*.xml");

    FileUtils.touch(new File(base.getRoot(), "path/to/supercoolreport.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/a/report.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/some/reports/1.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/some/reports/2.xml"));
    FileUtils.touch(new File(base.getRoot(), "some/reports/a.xml"));
    FileUtils.touch(new File(base.getRoot(), "some/reports/b.xml"));

    settings.setProperty(REPORT_PATH_KEY, absReportFile.toString() + ",path/**/*.xml");

    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), base.getRoot(), REPORT_PATH_KEY);
    assertEquals(6, reports.size());
  }

  @Test
  public void testRelativeBackticksOutsideBasedirThenBackInside() throws IOException {
    FileUtils.touch(new File(base.getRoot(), "path/to/supercoolreport.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/a/report.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/some/reports/1.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/some/reports/2.xml"));

    settings.setProperty(REPORT_PATH_KEY, "../" + base.getRoot().getName() + "/path/**/*.xml");

    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), base.getRoot(), REPORT_PATH_KEY);
    assertEquals(4, reports.size());
  }

  @Test
  public void testRelativeExcessiveBackticks() throws IOException {
    FileUtils.touch(new File(base.getRoot(), "path/to/supercoolreport.xml"));

    // Might be valid if java.io.tmpdir is nested excessively deep -- not likely    
    settings.setProperty(REPORT_PATH_KEY, "../../../../../../../../../../../../../../../../../../../../../../../../" +
        "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../" +
        "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../" +
        "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../*.xml");
    
    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), base.getRoot(), REPORT_PATH_KEY);
    assertEquals(0, reports.size());
  }
}
