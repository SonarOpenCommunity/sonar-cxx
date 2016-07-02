/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011-2016 SonarOpenCommunity
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
package org.sonar.cxx.checks;

import org.sonar.squidbridge.checks.CheckMessagesVerifier;
import org.junit.Test;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.cxx.CxxAstScanner;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

public class ClassComplexityCheckTest {
  
  @Test
  public void test() throws UnsupportedEncodingException, IOException {
    ClassComplexityCheck check = new ClassComplexityCheck();
    check.setMaximumClassComplexityThreshold(5);

    String fileName = "src/test/resources/checks/ClassComplexity.cc";
    SensorContextTester sensorContext = SensorContextTester.create(new File("."));
    String content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), fileName).toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content));
    InputFile cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath(fileName));
    
    SourceFile file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(9).withMessage("Class has a complexity of 10 which is greater than 5 authorized.")
      .noMore();
  }

}
