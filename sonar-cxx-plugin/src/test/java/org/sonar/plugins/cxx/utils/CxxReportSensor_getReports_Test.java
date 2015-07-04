/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
package org.sonar.plugins.cxx.utils;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.plugins.cxx.TestUtils;

public class CxxReportSensor_getReports_Test {

  private class CxxReportSensorImpl extends CxxReportSensor {
    public CxxReportSensorImpl(Settings settings, FileSystem fs) {
      super(settings, fs, TestUtils.mockReactor());
    }
  };

  @Rule
  public TemporaryFolder base = new TemporaryFolder();

  private CxxReportSensor sensor;
  private Settings settings;
  private FileSystem fs;

  @Before
  public void init() {
    settings = new Settings();
    fs = TestUtils.mockFileSystem();
    sensor = new CxxReportSensorImpl(settings, fs);
  }

  @Test
  public void getReports_patternMatching() throws java.io.IOException, java.lang.InterruptedException {
    Settings settings = new Settings();
    final String property = "sonar.cxx.cppcheck.reportPath";
    List<String[]> examples = new LinkedList<String[]>();
  
    //                          "pattern",      "matches",         "matches not"
    examples.add(new String[] { "A.ext",        "A.ext",           "dir/B.ext" });        // relative
    examples.add(new String[] { "dir/A.ext",    "dir/A.ext",       "A.ext, dir/B.ext" }); // relative with subdir

    examples.add(new String[] { "dir/../A.ext", "A.ext",           "B.ext, dir/A.ext" }); // relative with subdir
    examples.add(new String[] { "./A.ext",      "A.ext",           "B.ext" });            // relative with leading dot

    examples.add(new String[] { "A?.ext",       "AA.ext,AB.ext",   "B.ext" });            // containing question mark
    examples.add(new String[] { "A*.ext",       "A.ext,AAA.ext",   "B.ext" });            // containing question mark
    examples.add(new String[] { "**/A.ext",     "A.ext,dir/A.ext", "B.ext" });            // containing question mark
    examples.add(new String[] { "",             "",                "" });                 // empty

    //TODO: decide whether to support absolute paths
    //String abspattern = new File(base.getRoot(), "A.ext").getPath();
    //examples.add(new String[] { abspattern,     "",                "A.ext" });            // absolute

    String pattern, match, allpaths;
    List<File> reports;
    for (String[] example : examples) {
      pattern = example[0];
      match = example[1];
      allpaths = StringUtils.join(Arrays.copyOfRange(example, 1, 3), ",");
      setupExample(allpaths);
      settings.setProperty(property, pattern);

      reports = sensor.getReports(settings, base.getRoot().getPath(), "", property);

      assertMatch(reports, match);
      deleteExample(base.getRoot());
    }

    // TODO: some special windows cases?
  }

  private void setupExample(String pathes) throws java.io.IOException {
    String[] parsedPaths = StringUtils.split(pathes, ",");
    for (String path : parsedPaths) {
      FileUtils.touch(new File(base.getRoot(), path.trim()));
    }
  }

  private void deleteExample(File dir) throws java.io.IOException {
    FileUtils.cleanDirectory(dir);
  }

  private void assertMatch(List<File> real, String expected) {
    String[] parsedPaths = StringUtils.split(expected, ",");
    List<File> expectedFiles = new LinkedList<File>();
    for (String path : parsedPaths) {
      expectedFiles.add(new File(base.getRoot(), path));
    }

    Set<File> realSet = new TreeSet<File>(real);
    Set<File> expectedSet = new TreeSet<File>(expectedFiles);

    assertEquals(realSet, expectedSet);
  }
}
