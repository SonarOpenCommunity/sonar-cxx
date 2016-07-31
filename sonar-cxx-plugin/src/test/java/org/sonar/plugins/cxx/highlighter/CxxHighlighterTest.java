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
package org.sonar.plugins.cxx.highlighter;

import java.io.File;
import java.util.List;
import org.apache.commons.io.Charsets;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.cxx.CxxAstScanner;

import static org.fest.assertions.Assertions.assertThat;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;

public class CxxHighlighterTest {

  private SensorContextTester context;

  private File file;

  @Before
  @SuppressWarnings("unchecked")
  public void scanFile() {
    String dir = "src/test/resources/org/sonar/plugins/cxx";

    file = new File(dir, "/highlighter.cc");
    DefaultInputFile inputFile = new DefaultInputFile("moduleKey", file.getName())
      .initMetadata(new FileMetadata().readMetadata(file, Charsets.UTF_8));

    context = SensorContextTester.create(new File(dir));
    context.fileSystem().add(inputFile);

    CxxHighlighter cxxHighlighter = new CxxHighlighter(context);
    CxxAstScanner.scanSingleFile(inputFile, context, cxxHighlighter);
  }

  @Test
  public void keyword() throws Exception {

    checkOnRange(55, 0, 4, TypeOfText.KEYWORD);  // void
    checkOnRange(57, 3, 4, TypeOfText.KEYWORD);  // auto
    checkOnRange(59, 3, 4, TypeOfText.KEYWORD);  // auto
    checkOnRange(62, 3, 3, TypeOfText.KEYWORD);  // for
    checkOnRange(62, 7, 5, TypeOfText.KEYWORD);  // const
    checkOnRange(62, 13, 4, TypeOfText.KEYWORD); // auto
    checkOnRange(64, 6, 2, TypeOfText.KEYWORD);  // if
  }

  @Test
  public void stringLiteral() throws Exception {

    checkOnRange(49, 19, 7, TypeOfText.STRING);  // "hello"
    checkOnRange(50, 19, 18, TypeOfText.STRING); // "hello\tworld\r\n"
  }

  @Test
  public void character() throws Exception {

    checkOnRange(46, 10, 3, TypeOfText.STRING); // 'x'
    checkOnRange(47, 10, 4, TypeOfText.STRING); // '\t'
  }

  @Test
  public void comment() throws Exception {

    check(1, 0, TypeOfText.COMMENT);
    /*\r\n comment\r\n*/
    check(3, 1, TypeOfText.COMMENT);

    checkOnRange(5, 0, 2, TypeOfText.COMMENT);   //
    checkOnRange(6, 0, 10, TypeOfText.COMMENT);   // comment
    checkOnRange(7, 0, 2, TypeOfText.COMMENT);   //

    checkOnRange(57, 22, 10, TypeOfText.COMMENT); // comment
    checkOnRange(58, 3, 10, TypeOfText.COMMENT);  // comment
    checkOnRange(61, 3, 13, TypeOfText.COMMENT);
    /* comment */
    checkOnRange(64, 20, 13, TypeOfText.COMMENT);
    /* comment */
  }

  @Test
  public void number() throws Exception {

    checkOnRange(27, 10, 1, TypeOfText.CONSTANT); //  0
    checkOnRange(28, 10, 1, TypeOfText.CONSTANT); // -1 (without minus)
    checkOnRange(29, 10, 1, TypeOfText.CONSTANT); // +1 (without plus)

    checkOnRange(31, 14, 2, TypeOfText.CONSTANT); // 0u
    checkOnRange(32, 19, 3, TypeOfText.CONSTANT); // 1ul

    checkOnRange(34, 9, 3, TypeOfText.CONSTANT);  // 0x0
    checkOnRange(35, 9, 3, TypeOfText.CONSTANT);  // 0b0
    checkOnRange(36, 9, 16, TypeOfText.CONSTANT); // 0b0100'1100'0110

    checkOnRange(38, 12, 3, TypeOfText.CONSTANT); //  0.0
    checkOnRange(39, 12, 3, TypeOfText.CONSTANT); // -1.0 (without minus)
    checkOnRange(40, 12, 3, TypeOfText.CONSTANT); // +1.0 (without plus)
    checkOnRange(41, 12, 8, TypeOfText.CONSTANT); // 3.14E-10
  }

  @Test
  public void preprocessDirective() throws Exception {
    
    checkOnRange(12, 0, 8, TypeOfText.PREPROCESS_DIRECTIVE); // #include
    
    checkOnRange(14, 0, 6, TypeOfText.PREPROCESS_DIRECTIVE); // #ifdef
    checkOnRange(15, 0,10, TypeOfText.PREPROCESS_DIRECTIVE); // #   define
    checkOnRange(16, 0, 5, TypeOfText.PREPROCESS_DIRECTIVE); // #else
    checkOnRange(17, 0,10, TypeOfText.PREPROCESS_DIRECTIVE); // #   define
    checkOnRange(18, 0, 6, TypeOfText.PREPROCESS_DIRECTIVE); // #endif
    
    checkOnRange(20, 0, 7, TypeOfText.PREPROCESS_DIRECTIVE); // #define
  }
  
  /**
   * Checks the highlighting of a range of columns. The first column of a line
   * has index 0. The range is the columns of the token.
   */
  private void checkOnRange(int line, int firstColumn, int length, TypeOfText expectedTypeOfText) {
    // check that every column of the token is highlighted (and with the expected type)
    for (int column = firstColumn; column < firstColumn + length; column++) {
      checkInternal(line, column, "", expectedTypeOfText);
    }

    // check that the column before the token is not highlighted
    if (firstColumn != 0) {
      checkInternal(line, firstColumn - 1, " (= before the token)", null);
    }

    // check that the column after the token is not highlighted
    checkInternal(line, firstColumn + length, " (= after the token)", null);
  }

  /**
   * Checks the highlighting of one column. The first column of a line has index 0.
   */
  private void check(int line, int column, TypeOfText expectedTypeOfText) {
    checkInternal(line, column, "", expectedTypeOfText);
  }

  private void checkInternal(int line, int column, String messageComplement, TypeOfText expectedTypeOfText) {
    String componentKey = "moduleKey:" + file.getName();
    List<TypeOfText> foundTypeOfTexts = context.highlightingTypeAt(componentKey, line, column);

    int expectedNumberOfTypeOfText = expectedTypeOfText == null ? 0 : 1;
    String message = "number of TypeOfTexts at line " + line + " and column " + column + messageComplement;
    assertThat(foundTypeOfTexts).as(message).hasSize(expectedNumberOfTypeOfText);
    if (expectedNumberOfTypeOfText > 0) {
      message = "found TypeOfTexts at line " + line + " and column " + column + messageComplement;
      assertThat(foundTypeOfTexts.get(0)).as(message).isEqualTo(expectedTypeOfText);
    }
  }

}
