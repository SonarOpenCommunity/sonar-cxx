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
package org.sonar.cxx.sensors.visitors;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Set;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxFileLinesVisitorTest {
    private static final Logger LOG = Loggers.get(CxxFileLinesVisitorTest.class);
    private static final Version SQ_6_2 = Version.create(6, 2);
    
  @Test
  public void TestLinesOfCode() throws UnsupportedEncodingException, IOException {

    SensorContextTester sensorContext = SensorContextTester.create(new File("."));

    String fileName = "src/test/resources/org/sonar/cxx/sensors/ncloc.cc";
    String content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), fileName).toPath()), "UTF-8");
    DefaultInputFile inputFile = new DefaultInputFile("projectKey", fileName);
    sensorContext.fileSystem().add(inputFile.initMetadata(content));

    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    FileLinesContext fileLinesContext = mock(FileLinesContext.class);

    when(fileLinesContextFactory.createFor(inputFile)).thenReturn(fileLinesContext);

    HashMap<InputFile, Set<Integer>> linesOfCode = new HashMap<>();
    CxxFileLinesVisitor visitor = new CxxFileLinesVisitor(fileLinesContextFactory, sensorContext, linesOfCode);

    CxxAstScanner.scanSingleFile(inputFile, sensorContext, TestUtils.mockCxxLanguage(), visitor);

    assertThat(visitor.getLinesOfCode()).hasSize(32);
    assertThat(visitor.getLinesOfCode()).containsOnly(64, 65, 66,  8,  9, 10, 11, 14, 15, 16, 17, 19, 21, 22, 23, 25, 26, 27, 31, 32,
                                                      34, 35, 36, 37, 42, 43, 44, 45, 46, 60, 61, 63);

    assertThat(visitor.getLinesOfComments()).hasSize(9);
    assertThat(visitor.getLinesOfComments()).containsOnly(48, 64, 1, 33, 35, 51, 7, 13, 62);

  }

  @Test
  public void TestExecutableLinesOfCode() throws UnsupportedEncodingException, IOException {

    SensorContextTester sensorContext = SensorContextTester.create(new File("."));
    sensorContext.setSonarQubeVersion(SQ_6_2);

    String fileName = "src/test/resources/org/sonar/cxx/sensors/ncloc.cc";
    String content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), fileName).toPath()), "UTF-8");
    DefaultInputFile inputFile = new DefaultInputFile("projectKey", fileName);
    sensorContext.fileSystem().add(inputFile.initMetadata(content));

    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    FileLinesContext fileLinesContext = mock(FileLinesContext.class);

    when(fileLinesContextFactory.createFor(inputFile)).thenReturn(fileLinesContext);

    HashMap<InputFile, Set<Integer>> linesOfCode = new HashMap<>();
    CxxFileLinesVisitor visitor = new CxxFileLinesVisitor(fileLinesContextFactory, sensorContext, linesOfCode);

    CxxAstScanner.scanSingleFile(inputFile, sensorContext, TestUtils.mockCxxLanguage(), visitor);

    assertThat(visitor.getExecutableLines()).hasSize(23);
    assertThat(visitor.getExecutableLines()).containsOnly(32, 64, 65, 34, 66, 35, 36, 37, 9, 10, 11, 43, 44, 45, 46, 15, 16, 19, 25, 26, 27, 61, 63);
  }

}
