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
import java.util.List;
import org.apache.commons.io.Charsets;
import static org.fest.assertions.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.cxx.CxxAstScanner;

import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.cxx.sensors.utils.TestUtils;
import org.sonar.duplications.internal.pmd.TokensLine;

public class CxxCpdVisitorTest {

  private SensorContextTester context;
  private DefaultInputFile inputFile;

  @Before
  @SuppressWarnings("unchecked")
  public void scanFile() {
    String dir = "src/test/resources/org/sonar/cxx/sensors";

    File file = new File(dir, "/cpd.cc");
    inputFile = new DefaultInputFile("moduleKey", file.getName())
      .initMetadata(new FileMetadata().readMetadata(file, Charsets.UTF_8));

    context = SensorContextTester.create(new File(dir));
    context.fileSystem().add(inputFile);

    CxxCpdVisitor cxxCpdVisitor = new CxxCpdVisitor(context, true, true);
    CxxAstScanner.scanSingleFile(inputFile, context, TestUtils.mockCxxLanguage(), cxxCpdVisitor);
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
