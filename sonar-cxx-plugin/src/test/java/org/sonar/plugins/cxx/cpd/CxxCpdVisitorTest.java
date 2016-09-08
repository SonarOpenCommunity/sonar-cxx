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
package org.sonar.plugins.cxx.cpd;

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
import org.sonar.duplications.internal.pmd.TokensLine;

public class CxxCpdVisitorTest {

  private SensorContextTester context;
  private DefaultInputFile inputFile;

  @Before
  @SuppressWarnings("unchecked")
  public void scanFile() {
    String dir = "src/test/resources/org/sonar/plugins/cxx";

    File file = new File(dir, "/cpd.cc");
    inputFile = new DefaultInputFile("moduleKey", file.getName())
      .initMetadata(new FileMetadata().readMetadata(file, Charsets.UTF_8));

    context = SensorContextTester.create(new File(dir));
    context.fileSystem().add(inputFile);

    CxxCpdVisitor cxxCpdVisitor = new CxxCpdVisitor(context);
    CxxAstScanner.scanSingleFile(inputFile, context, cxxCpdVisitor);
  }

  @Test
  public void testCpdTokens() throws Exception {
    List<TokensLine> cpdTokenLines = context.cpdTokens("moduleKey:" + inputFile.file().getName());
    assertThat(cpdTokenLines).hasSize(75);

    // bits unixtime1(bits ld, bits ex)
    TokensLine firstTokensLine = cpdTokenLines.get(0);
    assertThat(firstTokensLine.getValue()).isEqualTo("_I_I(_I_I,_I_I)");
    assertThat(firstTokensLine.getStartLine()).isEqualTo(2);
    assertThat(firstTokensLine.getStartUnit()).isEqualTo(1);
    assertThat(firstTokensLine.getEndLine()).isEqualTo(2);
    assertThat(firstTokensLine.getEndUnit()).isEqualTo(9);

    // ld &= 0xFF;
    TokensLine secondTokensLine = cpdTokenLines.get(2);
    assertThat(secondTokensLine.getValue()).isEqualTo("_I&=_N;");
    assertThat(secondTokensLine.getStartLine()).isEqualTo(4);
    assertThat(secondTokensLine.getStartUnit()).isEqualTo(11);
    assertThat(secondTokensLine.getEndLine()).isEqualTo(4);
    assertThat(secondTokensLine.getEndUnit()).isEqualTo(14);

    // case 3: return "three";
    TokensLine thirdTokensLine = cpdTokenLines.get(71);
    assertThat(thirdTokensLine.getValue()).isEqualTo("case_N:return_S;");
    assertThat(thirdTokensLine.getStartLine()).isEqualTo(86);
    assertThat(thirdTokensLine.getStartUnit()).isEqualTo(388);
    assertThat(thirdTokensLine.getEndLine()).isEqualTo(86);
    assertThat(thirdTokensLine.getEndUnit()).isEqualTo(393);
  }

}
