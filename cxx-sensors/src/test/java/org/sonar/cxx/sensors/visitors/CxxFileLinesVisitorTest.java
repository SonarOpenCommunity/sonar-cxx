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
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.utils.Version;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxFileLinesVisitorTest {
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

    assertThat(visitor.getLinesOfCode()).hasSize(48);
    assertThat(visitor.getLinesOfCode()).containsOnly(8, 10, 14, 16, 17, 21, 22, 23, 26, 31, 34, 35, 42, 44, 45, 51, 53, 55, 56, 58,
                                                      59, 63, 65, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 79, 82, 84, 86, 87, 89, 90,
                                                      95, 98, 99, 100, 102, 115, 118, 119);

    assertThat(visitor.getLinesOfComments()).hasSize(11);
    assertThat(visitor.getLinesOfComments()).containsOnly(48, 1, 33, 97, 35, 117, 102, 7, 119, 106, 13);

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

    assertThat(visitor.getExecutableLines()).hasSize(26);
    assertThat(visitor.getExecutableLines()).containsOnly(10, 16, 26, 34, 35, 44, 45, 53, 55, 56, 59, 67, 69, 70, 72, 73, 75,
                                                          76, 79, 86, 87, 90, 98, 102, 118, 119);
  }

}

