/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.internal.DefaultNoSonarFilter;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;

public class CxxFileLinesContextTest {

  private FileLinesContextForTesting fileLinesContext;

  @Before
  public void setUp() throws IOException {
    ActiveRules rules = mock(ActiveRules.class);
    var checkFactory = new CheckFactory(rules);
    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    fileLinesContext = new FileLinesContextForTesting();
    when(fileLinesContextFactory.createFor(Mockito.any(InputFile.class))).thenReturn(fileLinesContext);

    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx");
    var context = SensorContextTester.create(baseDir);
    var inputFile = TestUtils.buildInputFile(baseDir, "ncloc.cc");
    context.fileSystem().add(inputFile);

    var sensor = new CxxSquidSensor(fileLinesContextFactory, checkFactory, new DefaultNoSonarFilter(), null);
    sensor.execute(context);
  }

  @Test
  public void TestLinesOfCode() throws UnsupportedEncodingException, IOException {
    Set<Integer> linesOfCode = Stream.of(
      8, 10, 14, 16, 17, 21, 22, 23, 26, 31, 34, 35, 42, 44, 45, 49, 51, 53, 55, 56,
      58, 59, 63, 65, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 79, 82, 84, 86, 87, 89,
      90, 95, 98, 99, 100, 102, 107, 108, 109, 110, 111, 113, 115, 118, 119, 124, 126)
      .collect(Collectors.toCollection(HashSet::new));

    assertThat(fileLinesContext.linesOfCode).containsExactlyInAnyOrderElementsOf(linesOfCode);
  }

  @Test
  public void TestExecutableLinesOfCode() throws UnsupportedEncodingException, IOException {
    assertThat(fileLinesContext.executableLines).containsExactlyInAnyOrder(
      10, 26, 34, 35, 56, 59, 69, 70, 72, 73,
      75, 76, 79, 87, 90, 98, 102, 118, 119, 126);
  }

  private class FileLinesContextForTesting implements FileLinesContext {

    public final Set<Integer> executableLines = new HashSet<>();
    public final Set<Integer> linesOfCode = new HashSet<>();

    @Override
    public void setIntValue(String metricKey, int line, int value) {
      Assert.assertEquals(1, value);

      switch (metricKey) {
        case CoreMetrics.NCLOC_DATA_KEY:
          linesOfCode.add(line);
          break;
        case CoreMetrics.EXECUTABLE_LINES_DATA_KEY:
          executableLines.add(line);
          break;
        default:
          Assert.fail("Unsupported metric key " + metricKey);
      }
    }

    @Override
    public void setStringValue(String metricKey, int line, String value) {
      Assert.fail("unexpected method called: setStringValue()");
    }

    @Override
    public void save() {
    }
  }

}
