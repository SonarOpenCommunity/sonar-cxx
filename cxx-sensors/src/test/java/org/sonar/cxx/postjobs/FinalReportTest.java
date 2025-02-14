/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.cxx.postjobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.preprocessor.CxxPreprocessor;
import org.sonar.cxx.visitors.CxxParseErrorLoggerVisitor;

class FinalReportTest {

  @RegisterExtension
  private final LogTesterJUnit5 logTester = new LogTesterJUnit5();

  private PostJobContext postJobContext;

  @BeforeEach
  public void setUp() {
    postJobContext = Mockito.mock(PostJobContext.class);
  }

  @Test
  void finalReportTest() throws IOException {
    var dir = "src/test/resources/org/sonar/cxx/postjobs";
    var context = SensorContextTester.create(new File(dir));
    InputFile inputFile = createInputFile(dir + "/syntaxerror.cc", ".", Charset.defaultCharset());
    context.fileSystem().add(inputFile);

    CxxParseErrorLoggerVisitor.resetReport();
    CxxPreprocessor.resetReport();
    CxxAstScanner.scanSingleInputFile(inputFile);

    var postjob = new FinalReport();
    postjob.execute(postJobContext);

    var log = logTester.logs(Level.WARN);
    assertThat(log).hasSize(2);
    assertThat(log.get(0)).contains("include directive error(s)");
    assertThat(log.get(1)).contains("syntax error(s) detected");
  }

  private static DefaultInputFile createInputFile(String fileName, String basePath, Charset charset)
    throws IOException {
    var fb = TestInputFileBuilder.create("", fileName);

    fb.setCharset(charset);
    fb.setProjectBaseDir(Path.of(basePath));
    fb.setContents(getSourceCode(Path.of(basePath, fileName).toFile(), charset));

    return fb.build();
  }

  private static String getSourceCode(File filename, Charset defaultCharset) throws IOException {
    try (var bomInputStream = BOMInputStream.builder()
      .setInputStream(new FileInputStream(filename))
      .setInclude(false)
      .setByteOrderMarks(
        ByteOrderMark.UTF_8,
        ByteOrderMark.UTF_16LE,
        ByteOrderMark.UTF_16BE,
        ByteOrderMark.UTF_32LE,
        ByteOrderMark.UTF_32BE
      )
      .get()) {
      ByteOrderMark bom = bomInputStream.getBOM();
      Charset charset = bom != null ? Charset.forName(bom.getCharsetName()) : defaultCharset;
      byte[] bytes = bomInputStream.readAllBytes();
      return new String(bytes, charset);
    }
  }

}
