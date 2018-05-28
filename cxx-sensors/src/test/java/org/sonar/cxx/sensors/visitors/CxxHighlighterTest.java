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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxHighlighterTest {

  private SensorContextTester context;

  private File target;

  @Before
  public void scanFile() throws IOException {
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors");
    target = new File(baseDir, "highlighter.cc");

    String content = new String(Files.readAllBytes(target.toPath()), "UTF-8");
    DefaultInputFile inputFile = TestInputFileBuilder.create("ProjectKey", baseDir, target)
      .setContents(content).setCharset(Charset.forName("UTF-8")).build();

    context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile);

    CxxHighlighterVisitor cxxHighlighter = new CxxHighlighterVisitor(context);
    CxxAstScanner.scanSingleFile(inputFile, context, TestUtils.mockCxxLanguage(), cxxHighlighter);
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... checkOnRange contains the assertion
  public void keyword() {

    checkOnRange(55, 0, 4, TypeOfText.KEYWORD);  // void
    checkOnRange(57, 3, 4, TypeOfText.KEYWORD);  // auto
    checkOnRange(59, 3, 4, TypeOfText.KEYWORD);  // auto
    checkOnRange(62, 3, 3, TypeOfText.KEYWORD);  // for
    checkOnRange(62, 7, 5, TypeOfText.KEYWORD);  // const
    checkOnRange(62, 13, 4, TypeOfText.KEYWORD); // auto
    checkOnRange(64, 6, 2, TypeOfText.KEYWORD);  // if
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... checkOnRange contains the assertion
  public void stringLiteral() {

    checkOnRange(49, 19, 7, TypeOfText.STRING);  // "hello"
    checkOnRange(50, 19, 18, TypeOfText.STRING); // "hello\tworld\r\n"
    checkOnRange(73, 32, 24, TypeOfText.STRING); // R"([.^$|()\[\]{}*+?\\])"

    checkOnRange(83, 24, 5, TypeOfText.STRING); // "..."
    checkOnRange(84, 24, 7, TypeOfText.STRING); // u8"..."
    checkOnRange(85, 24, 6, TypeOfText.STRING); // L"..."
    checkOnRange(86, 24, 6, TypeOfText.STRING); // u"..."
    checkOnRange(87, 24, 6, TypeOfText.STRING); // U"..."

    // "hello" " world"
    checkOnRange(89, 24, 7, TypeOfText.STRING);
    checkOnRange(89, 32, 8, TypeOfText.STRING);

    // u"" "hello world"
    checkOnRange(90, 24, 3, TypeOfText.STRING);
    checkOnRange(90, 28, 13, TypeOfText.STRING);

    // /*comment1*/ u"" /*comment2*/ "hello world" /*comment3*/; // issue #996
    checkOnRange(91, 24, 12, TypeOfText.COMMENT);
    checkOnRange(91, 37, 3, TypeOfText.STRING);
    checkOnRange(91, 41, 12, TypeOfText.COMMENT);
    checkOnRange(91, 54, 13, TypeOfText.STRING);
    checkOnRange(91, 68, 12, TypeOfText.COMMENT);
    checkOnRange(91, 82, 13, TypeOfText.COMMENT);

    // /*comment4*/ "hello"
    // /*comment5*/ " world" /*comment6*/;
    checkOnRange(93, 24, 12, TypeOfText.COMMENT);
    checkOnRange(93, 37, 7, TypeOfText.STRING);
    checkOnRange(94, 24, 12, TypeOfText.COMMENT);
    checkOnRange(94, 37, 8, TypeOfText.STRING);
    checkOnRange(94, 46, 12, TypeOfText.COMMENT);

    // "hello"
    // "Mary"
    // "Lou";
    checkOnRange(96, 25, 7, TypeOfText.STRING);
    checkOnRange(97, 25, 6, TypeOfText.STRING);
    checkOnRange(98, 25, 5, TypeOfText.STRING);
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... checkOnRange contains the assertion
  public void character() {

    checkOnRange(46, 10, 3, TypeOfText.STRING); // 'x'
    checkOnRange(47, 10, 4, TypeOfText.STRING); // '\t'
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... checkOnRange contains the assertion
  public void comment() {

    check(1, 0, TypeOfText.COMMENT);
    /*\r\n comment\r\n*/
    check(3, 1, TypeOfText.COMMENT);

    checkOnRange(5, 0, 2, TypeOfText.COMMENT);   //
    checkOnRange(6, 0, 10, TypeOfText.COMMENT);  // comment
    checkOnRange(7, 0, 2, TypeOfText.COMMENT);   //

    checkOnRange(57, 22, 10, TypeOfText.COMMENT); // comment
    checkOnRange(58, 3, 10, TypeOfText.COMMENT);  // comment
    checkOnRange(61, 3, 13, TypeOfText.COMMENT);
    /* comment */
    checkOnRange(64, 20, 13, TypeOfText.COMMENT);
    /* comment */
  }

  @Test
  @SuppressWarnings("squid:S2699") // ... checkOnRange contains the assertion
  public void number() {

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
  @SuppressWarnings("squid:S2699") // ... checkOnRange contains the assertion
  public void preprocessDirective() {

    checkOnRange(12, 0, 8, TypeOfText.PREPROCESS_DIRECTIVE); // #include

    checkOnRange(14, 0, 6, TypeOfText.PREPROCESS_DIRECTIVE); // #ifdef
    checkOnRange(15, 0, 10, TypeOfText.PREPROCESS_DIRECTIVE); // #   define
    checkOnRange(16, 0, 5, TypeOfText.PREPROCESS_DIRECTIVE); // #else
    checkOnRange(17, 0, 10, TypeOfText.PREPROCESS_DIRECTIVE); // #   define
    checkOnRange(18, 0, 6, TypeOfText.PREPROCESS_DIRECTIVE); // #endif

    checkOnRange(20, 0, 7, TypeOfText.PREPROCESS_DIRECTIVE); // #define
  }

  /**
   * Checks the highlighting of a range of columns. The first column of a line has index 0. The range is the columns of
   * the token.
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
    String componentKey = "ProjectKey:" + target.getName();
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
