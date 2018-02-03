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
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;
import org.sonar.duplications.internal.pmd.TokensLine;

public class CxxCpdVisitorTest {

  private SensorContextTester context;
  private DefaultInputFile inputFile;
  private CxxLanguage language;

  @Before
  @SuppressWarnings("unchecked")
  public void scanFile() throws UnsupportedEncodingException, IOException {
    language = TestUtils.mockCxxLanguage();
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors");
    File target = new File(baseDir, "cpd.cc");

    String content = new String(Files.readAllBytes(target.toPath()), "UTF-8");
    inputFile = TestInputFileBuilder.create("moduleKey", baseDir, target).setType(InputFile.Type.MAIN)
      .setContents(content).setCharset(Charset.forName("UTF-8")).build();

    context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile);

    CxxCpdVisitor cxxCpdVisitor = new CxxCpdVisitor(context, true, true);
    CxxAstScanner.scanSingleFile(inputFile, context, language, cxxCpdVisitor);
  }

  @Test
  public void testCpdTokens() throws Exception {
    List<TokensLine> cpdTokenLines = context.cpdTokens("moduleKey:" + inputFile.file().getName());
    assertThat(cpdTokenLines).hasSize(75);

    // ld &= 0xFF;
    TokensLine firstTokensLine = cpdTokenLines.get(2);
    assertThat(firstTokensLine.getValue()).isEqualTo("_I&=_N;");
    assertThat(firstTokensLine.getStartLine()).isEqualTo(4);
    assertThat(firstTokensLine.getStartUnit()).isEqualTo(10);
    assertThat(firstTokensLine.getEndLine()).isEqualTo(4);
    assertThat(firstTokensLine.getEndUnit()).isEqualTo(13);

    // if (xosfile_read_stamped_no_path(fn, &ob, 1, 1, 1, 1, 1)) return 1;
    TokensLine secondTokensLine = cpdTokenLines.get(48);
    assertThat(secondTokensLine.getValue()).isEqualTo("if(_I(_I,&_I,_N,_N,_N,_N,_N))return_N;");
    assertThat(secondTokensLine.getStartLine()).isEqualTo(60);
    assertThat(secondTokensLine.getStartUnit()).isEqualTo(283);
    assertThat(secondTokensLine.getEndLine()).isEqualTo(60);
    assertThat(secondTokensLine.getEndUnit()).isEqualTo(305);

    // case 3: return "three";
    TokensLine thirdTokensLine = cpdTokenLines.get(71);
    assertThat(thirdTokensLine.getValue()).isEqualTo("case_N:return_S;");
    assertThat(thirdTokensLine.getStartLine()).isEqualTo(86);
    assertThat(thirdTokensLine.getStartUnit()).isEqualTo(381);
    assertThat(thirdTokensLine.getEndLine()).isEqualTo(86);
    assertThat(thirdTokensLine.getEndUnit()).isEqualTo(386);
  }

}
