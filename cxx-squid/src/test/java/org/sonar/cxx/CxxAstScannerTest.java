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
package org.sonar.cxx;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.squidbridge.AstScanner;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.api.SourceProject;
import org.sonar.squidbridge.indexer.QueryByType;

import com.sonar.sslr.api.Grammar;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

public class CxxAstScannerTest {

  @Test
  public void files() throws UnsupportedEncodingException, IOException {
    
    SensorContextTester sensorContext = SensorContextTester.create(new File("."));
    String content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), "src/test/resources/metrics/trivial.cc").toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", "src/test/resources/metrics/trivial.cc").initMetadata(content));
    String content2 = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), "src/test/resources/metrics/classes.cc").toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", "src/test/resources/metrics/classes.cc").initMetadata(content2));
    
    AstScanner<Grammar> scanner = CxxAstScanner.create(new CxxConfiguration(), sensorContext);
    scanner.scanFiles(new ArrayList<>(Arrays.asList(
      new File("src/test/resources/metrics/trivial.cc"),
      new File("src/test/resources/metrics/classes.cc")))
    );
    SourceProject project = (SourceProject) scanner.getIndex().search(new QueryByType(SourceProject.class)).iterator().next();
    assertThat(project.getInt(CxxMetric.FILES)).isEqualTo(2);
  }

  @Test
  public void comments() throws UnsupportedEncodingException, IOException {
    SensorContextTester sensorContext = SensorContextTester.create(new File("."));
    String content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), "src/test/resources/metrics/comments.cc").toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", "src/test/resources/metrics/comments.cc").initMetadata(content));
    InputFile cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath("src/test/resources/metrics/comments.cc"));
    
    SourceFile file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext);
    assertThat(file.getInt(CxxMetric.COMMENT_LINES)).isEqualTo(6);
    assertThat(file.getNoSonarTagLines()).contains(8).hasSize(1);
  }

  @Test
  public void lines() throws UnsupportedEncodingException, IOException {
    SensorContextTester sensorContext = SensorContextTester.create(new File("."));
    String content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), "src/test/resources/metrics/classes.cc").toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", "src/test/resources/metrics/classes.cc").initMetadata(content));
    InputFile cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath("src/test/resources/metrics/classes.cc"));

    
    SourceFile file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext);
    assertThat(file.getInt(CxxMetric.LINES)).isEqualTo(7);
  }

  @Test
  public void lines_of_code() throws UnsupportedEncodingException, IOException {
    SensorContextTester sensorContext = SensorContextTester.create(new File("."));
    String content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), "src/test/resources/metrics/classes.cc").toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", "src/test/resources/metrics/classes.cc").initMetadata(content));
    InputFile cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath("src/test/resources/metrics/classes.cc"));
    
    SourceFile file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext);
    assertThat(file.getInt(CxxMetric.LINES_OF_CODE)).isEqualTo(5);
  }

  @Test
  public void statements() throws UnsupportedEncodingException, IOException {
    SensorContextTester sensorContext = SensorContextTester.create(new File("."));
    String content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), "src/test/resources/metrics/statements.cc").toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", "src/test/resources/metrics/statements.cc").initMetadata(content));
    InputFile cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath("src/test/resources/metrics/statements.cc"));
    
    SourceFile file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext);
    assertThat(file.getInt(CxxMetric.STATEMENTS)).isEqualTo(4);
  }

  @Test
  public void functions() throws UnsupportedEncodingException, IOException {
    SensorContextTester sensorContext = SensorContextTester.create(new File("."));
    String content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), "src/test/resources/metrics/functions.cc").toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", "src/test/resources/metrics/functions.cc").initMetadata(content));
    InputFile cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath("src/test/resources/metrics/functions.cc"));
    
    SourceFile file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext);
    assertThat(file.getInt(CxxMetric.FUNCTIONS)).isEqualTo(2);
  }

  @Test
  public void classes() throws UnsupportedEncodingException, IOException {
    SensorContextTester sensorContext = SensorContextTester.create(new File("."));
    String content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), "src/test/resources/metrics/classes.cc").toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", "src/test/resources/metrics/classes.cc").initMetadata(content));
    InputFile cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath("src/test/resources/metrics/classes.cc"));
    
    SourceFile file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext);
    assertThat(file.getInt(CxxMetric.CLASSES)).isEqualTo(2);
  }

  @Test
  public void complexity() throws UnsupportedEncodingException, IOException {
    SensorContextTester sensorContext = SensorContextTester.create(new File("."));
    String content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), "src/test/resources/metrics/complexity.cc").toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", "src/test/resources/metrics/complexity.cc").initMetadata(content));
    InputFile cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath("src/test/resources/metrics/complexity.cc"));
    
    SourceFile file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext);
    assertThat(file.getInt(CxxMetric.COMPLEXITY)).isEqualTo(14);
  }

  @Test
  public void error_recovery_declaration() throws UnsupportedEncodingException, IOException {
    SensorContextTester sensorContext = SensorContextTester.create(new File("."));
    String content = new String(Files.readAllBytes(new File(sensorContext.fileSystem().baseDir(), "src/test/resources/parser/bad/error_recovery_declaration.cc").toPath()), "UTF-8");
    sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", "src/test/resources/parser/bad/error_recovery_declaration.cc").initMetadata(content));
    InputFile cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath("src/test/resources/parser/bad/error_recovery_declaration.cc"));
    
    SourceFile file = CxxAstScanner.scanSingleFile(cxxFile, sensorContext);
    assertThat(file.getInt(CxxMetric.FUNCTIONS)).isEqualTo(2);
  }
}
