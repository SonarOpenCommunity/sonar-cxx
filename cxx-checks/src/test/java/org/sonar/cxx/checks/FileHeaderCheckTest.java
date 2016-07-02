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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifier;

public class FileHeaderCheckTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void test() throws UnsupportedEncodingException, IOException {
    FileHeaderCheck check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2005";

    String fileName = "src/test/resources/checks/FileHeaderCheck/Class1.cc";
    SensorContextTester sensorContext = SensorContextTester.create(new File("."));
    String content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), fileName).toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content));
    InputFile cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath(fileName));
    
    SourceFile file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);
    
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 20\\d\\d";

    file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(null);

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2005";

    fileName = "src/test/resources/checks/FileHeaderCheck/Class2.cc";
    content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), fileName).toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content));
    cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath(fileName));    
    file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);    
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(null).withMessage("Add or update the header of this file.");

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012";
    file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);       
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\n// foo";

    file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);    
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\r\n// foo";

    file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\r// foo";

    file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\r\r// foo";

    file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(null);

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\n// foo\n\n\n\n\n\n\n\n\n\ngfoo";

    file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(null);

    check = new FileHeaderCheck();
    check.headerFormat = "/*foo http://www.example.org*/";

    fileName = "src/test/resources/checks/FileHeaderCheck/Class3.cc";
    content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), fileName).toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content));
    cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath(fileName));    
    file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);    
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

  @Test
  public void regex() throws UnsupportedEncodingException, IOException {
    FileHeaderCheck check = new FileHeaderCheck();
    check.headerFormat = "// copyright \\d\\d\\d";
    check.isRegularExpression = true;
    String fileName = "src/test/resources/checks/FileHeaderCheck/Regex1.cc";
    SensorContextTester sensorContext = SensorContextTester.create(new File("."));
    String content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), fileName).toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content));
    InputFile cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath(fileName));
    
    SourceFile file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);    
    CheckMessagesVerifier.verify(file.getCheckMessages()).next().atLine(null).withMessage("Add or update the header of this file.");
    // Check that the regular expression is compiled once
    check = new FileHeaderCheck();
    
    file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);
    CheckMessagesVerifier.verify(file.getCheckMessages()).next().atLine(null).withMessage("Add or update the header of this file.");

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright \\d{4}\\n// mycompany";
    check.isRegularExpression = true;

    
    fileName = "src/test/resources/checks/FileHeaderCheck/Regex2.cc";
    content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), fileName).toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content));
    cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath(fileName));    
    file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);     
    CheckMessagesVerifier.verify(file.getCheckMessages()).next().atLine(null).withMessage("Add or update the header of this file.");

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright \\d{4}\\r?\\n// mycompany";
    check.isRegularExpression = true;
    
    
    fileName = "src/test/resources/checks/FileHeaderCheck/Regex3.cc";
    content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), fileName).toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content));
    cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath(fileName));    
    file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);  
    CheckMessagesVerifier.verify(file.getCheckMessages()).noMore();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright \\d{4}\\n// mycompany";
    check.isRegularExpression = true;
    
    fileName = "src/test/resources/checks/FileHeaderCheck/Regex4.cc";
    content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), fileName).toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content));
    cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath(fileName));    
    file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);  
    CheckMessagesVerifier.verify(file.getCheckMessages()).next().atLine(null).withMessage("Add or update the header of this file.");

    check = new FileHeaderCheck();
    check.headerFormat = "^(?=.*?\\bCopyright\\b)(?=.*?\\bVendor\\b)(?=.*?\\d{4}(-\\d{4})?).*$";
    check.isRegularExpression = true;
    
    fileName = "src/test/resources/checks/FileHeaderCheck/Regex5.cc";
    content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), fileName).toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content));
    cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath(fileName));    
    file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);  
    CheckMessagesVerifier.verify(file.getCheckMessages()).noMore();

    check = new FileHeaderCheck();
    
    fileName = "src/test/resources/checks/FileHeaderCheck/Regex6.cc";
    content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), fileName).toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content));
    cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath(fileName));    
    file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext, check);  
    CheckMessagesVerifier.verify(file.getCheckMessages()).next().atLine(null).withMessage("Add or update the header of this file.");    
  }

  @Test
  public void should_fail_with_bad_regular_expression() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("[" + FileHeaderCheck.class.getSimpleName() + "] Unable to compile the regular expression: *");

    FileHeaderCheck check = new FileHeaderCheck();
    check.headerFormat = "*";
    check.isRegularExpression = true;
    check.init();
  }


}
