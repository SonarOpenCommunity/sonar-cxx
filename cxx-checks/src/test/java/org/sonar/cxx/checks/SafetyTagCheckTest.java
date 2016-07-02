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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifier;

public class SafetyTagCheckTest {

  @Test
  public void test() throws UnsupportedEncodingException, IOException {
    SafetyTagCheck check = new SafetyTagCheck();
    check.regularExpression = "<Safetykey>.*</Safetykey>";
    check.suffix = "_SAFETY";

    String fileName = "src/test/resources/checks/SafetyTagCheck.cc";
    SensorContextTester sensorContext = SensorContextTester.create(new File("."));
    String content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), fileName).toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content));
    InputFile cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath(fileName));
    
    SourceFile file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check); 
    
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(21).withMessage("Source files implementing risk mitigations shall use special name suffix '_SAFETY' : <Safetykey>MyRimName</Safetykey>");

    check = new SafetyTagCheck();
    check.regularExpression = "<Safetykey>.*</Safetykey>";
    check.suffix = "_SAFETY";

    
    fileName = "src/test/resources/checks/SafetyTagCheck_SAFETY.cc";
    content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), fileName).toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content));
    cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath(fileName));    
    file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);
    
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new SafetyTagCheck();
    check.regularExpression = "<Safetykey>";
    check.suffix = "_SAFETY";

    fileName = "src/test/resources/checks/SafetyTagCheck_SAFETY.cc";
    content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), fileName).toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content));
    cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath(fileName));
    
    file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new SafetyTagCheck();
    check.regularExpression = "@hazard";

    fileName = "src/test/resources/checks/SafetyTagCheck_SAFETY.cc";
    content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), fileName).toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content));
    cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath(fileName));
    
    file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

}
