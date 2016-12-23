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
package org.sonar.plugins.cxx.metrics;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Set;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.cxx.CxxAstScanner;

public class FileLinesVisitorTest {

  @Test
  public void test() throws UnsupportedEncodingException, IOException {
    SensorContextTester sensorContext = SensorContextTester.create(new File("."));

    String fileName = "src/test/resources/org/sonar/plugins/cxx/ncloc.cc";
    String content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), fileName).toPath()), "UTF-8");
    DefaultInputFile inputFile = new DefaultInputFile("myProjectKey", fileName);
    sensorContext.fileSystem().add(inputFile.initMetadata(content));

    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    FileLinesContext fileLinesContext = mock(FileLinesContext.class);

    when(fileLinesContextFactory.createFor(inputFile)).thenReturn(fileLinesContext);

    HashMap<InputFile, Set<Integer>> linesOfCode = new HashMap<>();
    FileLinesVisitor visitor = new FileLinesVisitor(fileLinesContextFactory, sensorContext.fileSystem(), linesOfCode);

    CxxAstScanner.scanSingleFile(inputFile, sensorContext, visitor);

    assertThat(linesOfCode).hasSize(1);
    assertThat(linesOfCode.get(inputFile)).containsOnly(
      8, 10,
      14, 16, 17, 18,
      21, 22, 23, 26,
      31, 34, 35,
      42, 44, 45,
      60, 63, 64
    );
  }

}
