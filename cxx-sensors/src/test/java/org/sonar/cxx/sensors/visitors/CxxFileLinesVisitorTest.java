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
package org.sonar.cxx.sensors.visitors;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.coverage.CxxCoverageSensor;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxFileLinesVisitorTest {

  private CxxLanguage language;
  private FileLinesContextFactory fileLinesContextFactory;
  private FileLinesContext fileLinesContext;
  private File baseDir;
  private File target;
  private Set<Integer> testLines;

  @Before
  public void setUp() {
    language = TestUtils.mockCxxLanguage();
    fileLinesContextFactory = mock(FileLinesContextFactory.class);
    fileLinesContext = mock(FileLinesContext.class);

    when(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY))
      .thenReturn("sonar.cxx." + CxxCoverageSensor.REPORT_PATH_KEY);
    baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors");
    target = new File(baseDir, "ncloc.cc");

    testLines = Stream.of(8, 10, 14, 16, 17, 21, 22, 23, 26, 31, 34, 35, 42, 44, 45, 49, 51, 53, 55, 56,
        58, 59, 63, 65, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 79, 82, 84, 86, 87, 89,
        90, 95, 98, 99, 100, 102, 107, 108, 109, 110, 111, 113, 115, 118, 119, 124, 126)
        .collect(Collectors.toCollection(HashSet::new));
  }

  @Test
  public void TestLinesOfCode() throws UnsupportedEncodingException, IOException {
    String content = new String(Files.readAllBytes(target.toPath()), "UTF-8");
    DefaultInputFile inputFile = TestInputFileBuilder.create("ProjectKey", baseDir, target).setContents(content)
      .setCharset(Charset.forName("UTF-8")).setLanguage(language.getKey())
      .setType(InputFile.Type.MAIN).build();

    SensorContextTester sensorContext = SensorContextTester.create(baseDir);
    sensorContext.fileSystem().add(inputFile);

    when(fileLinesContextFactory.createFor(inputFile)).thenReturn(fileLinesContext);

    CxxFileLinesVisitor visitor = new CxxFileLinesVisitor(language, fileLinesContextFactory, sensorContext);

    CxxAstScanner.scanSingleFile(inputFile, sensorContext, TestUtils.mockCxxLanguage(), visitor);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(visitor.getLinesOfCode()).containsExactlyInAnyOrderElementsOf(testLines);
    softly.assertAll();
  }

  @Test
  public void TestLinesOfComments() throws UnsupportedEncodingException, IOException {
    String content = new String(Files.readAllBytes(target.toPath()), "UTF-8");
    DefaultInputFile inputFile = TestInputFileBuilder.create("ProjectKey", baseDir, target).setContents(content)
      .setCharset(Charset.forName("UTF-8")).setLanguage(language.getKey())
      .setType(InputFile.Type.MAIN).build();

    SensorContextTester sensorContext = SensorContextTester.create(baseDir);
    sensorContext.fileSystem().add(inputFile);

    when(fileLinesContextFactory.createFor(inputFile)).thenReturn(fileLinesContext);

    CxxFileLinesVisitor visitor = new CxxFileLinesVisitor(language, fileLinesContextFactory, sensorContext);

    CxxAstScanner.scanSingleFile(inputFile, sensorContext, TestUtils.mockCxxLanguage(), visitor);

    assertThat(visitor.getLinesOfComments()).containsExactlyInAnyOrder(48, 1, 33, 97, 35, 117, 102, 7, 119, 106, 13);
  }

  @Test
  public void TestExecutableLinesOfCode() throws UnsupportedEncodingException, IOException {

    String content = new String(Files.readAllBytes(target.toPath()), "UTF-8");
    DefaultInputFile inputFile = TestInputFileBuilder.create("ProjectKey", baseDir, target).setContents(content)
      .setCharset(Charset.forName("UTF-8")).setLanguage(language.getKey())
      .setType(InputFile.Type.MAIN).build();

    SensorContextTester sensorContext = SensorContextTester.create(baseDir);
    sensorContext.fileSystem().add(inputFile);

    when(fileLinesContextFactory.createFor(inputFile)).thenReturn(fileLinesContext);

    CxxFileLinesVisitor visitor = new CxxFileLinesVisitor(language, fileLinesContextFactory, sensorContext);

    CxxAstScanner.scanSingleFile(inputFile, sensorContext, TestUtils.mockCxxLanguage(), visitor);

    assertThat(visitor.getExecutableLines()).containsExactlyInAnyOrder(10, 26, 34, 35, 56, 59, 69, 70, 72, 73,
      75, 76, 79, 87, 90, 98, 102, 118, 119, 126);
  }
}
