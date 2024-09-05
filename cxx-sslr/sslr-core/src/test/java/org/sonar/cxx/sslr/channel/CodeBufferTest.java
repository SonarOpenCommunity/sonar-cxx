/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022 SonarOpenCommunity
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
/**
 * fork of SonarSource Language Recognizer: https://github.com/SonarSource/sslr
 * Copyright (C) 2010-2021 SonarSource SA / mailto:info AT sonarsource DOT com / license: LGPL v3
 */
package org.sonar.cxx.sslr.channel;

import java.io.IOException;
import java.util.regex.Pattern;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class CodeBufferTest {

  private final CodeReaderConfiguration defaulConfiguration = new CodeReaderConfiguration();

  @Test
  void testPop() {
    var code = new CodeBuffer("pa", defaulConfiguration);
    assertThat((char) code.pop()).isEqualTo('p');
    assertThat((char) code.pop()).isEqualTo('a');
    assertThat(code.pop()).isEqualTo(-1);
  }

  @Test
  void testSkip() {
    var code = new CodeBuffer("1234", defaulConfiguration);
    assertThat((char) code.skip(2)).isEqualTo('3');
    assertThat(code.skip(2)).isEqualTo(-1);
  }

  @Test
  void testPeek() {
    var code = new CodeBuffer("pa", defaulConfiguration);
    assertThat((char) code.peek()).isEqualTo('p');
    assertThat((char) code.peek()).isEqualTo('p');
    code.pop();
    assertThat((char) code.peek()).isEqualTo('a');
    code.pop();
    assertThat(code.peek()).isEqualTo(-1);
  }

  @Test
  void testLastCharacter() {
    var reader = new CodeBuffer("bar", defaulConfiguration);
    assertThat(reader.lastChar()).isEqualTo(-1);
    reader.pop();
    assertThat((char) reader.lastChar()).isEqualTo('b');
  }

  @Test
  void testGetColumnAndLinePosition() {
    var reader = new CodeBuffer("pa\nc\r\ns\r\n\r\n", defaulConfiguration);
    assertThat(reader.getColumnPosition()).isZero();
    assertThat(reader.getLinePosition()).isEqualTo(1);
    reader.pop(); // p
    reader.pop(); // a
    assertThat(reader.getColumnPosition()).isEqualTo(2);
    assertThat(reader.getLinePosition()).isEqualTo(1);
    reader.peek(); // \n
    reader.lastChar(); // a
    assertThat(reader.getColumnPosition()).isEqualTo(2);
    assertThat(reader.getLinePosition()).isEqualTo(1);
    reader.pop(); // \n
    assertThat(reader.getColumnPosition()).isZero();
    assertThat(reader.getLinePosition()).isEqualTo(2);
    reader.pop(); // c
    assertThat(reader.getColumnPosition()).isEqualTo(1);
    assertThat(reader.getLinePosition()).isEqualTo(2);
    reader.pop(); // \r
    reader.pop(); // \n
    assertThat(reader.getColumnPosition()).isZero();
    assertThat(reader.getLinePosition()).isEqualTo(3);
    assertThat((char) reader.pop()).isEqualTo('s');
    reader.pop(); // \r
    assertThat(reader.getColumnPosition()).isEqualTo(2);
    assertThat(reader.getLinePosition()).isEqualTo(3);
    reader.pop(); // \n
    assertThat(reader.getColumnPosition()).isZero();
    assertThat(reader.getLinePosition()).isEqualTo(4);
    reader.pop(); // \r
    reader.pop(); // \n
    assertThat(reader.getColumnPosition()).isZero();
    assertThat(reader.getLinePosition()).isEqualTo(5);
  }

  @Test
  void testStartAndStopRecording() {
    var reader = new CodeBuffer("123456", defaulConfiguration);
    reader.pop();
    assertThat(reader.stopRecording()).hasToString("");

    reader.startRecording();
    reader.pop();
    reader.pop();
    reader.peek();
    assertThat(reader.stopRecording()).hasToString("23");
    assertThat(reader.stopRecording()).hasToString("");
  }

  @Test
  void testCharAt() {
    var reader = new CodeBuffer("123456", defaulConfiguration);
    assertThat(reader.charAt(0)).isEqualTo('1');
    assertThat(reader.charAt(5)).isEqualTo('6');
  }

  @Test
  void testCharAtIndexOutOfBoundsException() {
    var reader = new CodeBuffer("12345", defaulConfiguration);
    assertThat(reader.charAt(5)).isEqualTo((char) -1);
  }

  @Test
  void testReadWithSpecificTabWidth() {
    var configuration = new CodeReaderConfiguration();
    configuration.setTabWidth(4);
    var reader = new CodeBuffer("pa\n\tc", configuration);
    assertThat(reader.charAt(2)).isEqualTo('\n');
    assertThat(reader.charAt(3)).isEqualTo('\t');
    assertThat(reader.charAt(4)).isEqualTo('c');
    assertThat(reader.getColumnPosition()).isZero();
    assertThat(reader.getLinePosition()).isEqualTo(1);
    reader.pop(); // p
    reader.pop(); // a
    assertThat(reader.getColumnPosition()).isEqualTo(2);
    assertThat(reader.getLinePosition()).isEqualTo(1);
    reader.peek(); // \n
    reader.lastChar(); // a
    assertThat(reader.getColumnPosition()).isEqualTo(2);
    assertThat(reader.getLinePosition()).isEqualTo(1);
    reader.pop(); // \n
    assertThat(reader.getColumnPosition()).isZero();
    assertThat(reader.getLinePosition()).isEqualTo(2);
    reader.pop(); // \t
    assertThat(reader.getColumnPosition()).isEqualTo(4);
    assertThat(reader.getLinePosition()).isEqualTo(2);
    reader.pop(); // c
    assertThat(reader.getColumnPosition()).isEqualTo(5);
    assertThat(reader.getLinePosition()).isEqualTo(2);
  }

  @Test
  void testCodeReaderFilter() throws Exception {
    var configuration = new CodeReaderConfiguration();
    configuration.setCodeReaderFilters(new ReplaceNumbersFilter());
    var code = new CodeBuffer("abcd12efgh34", configuration);
    // test #charAt
    assertThat(code.charAt(0)).isEqualTo('a');
    assertThat(code.charAt(4)).isEqualTo('-');
    assertThat(code.charAt(5)).isEqualTo('-');
    assertThat(code.charAt(6)).isEqualTo('e');
    assertThat(code.charAt(10)).isEqualTo('-');
    assertThat(code.charAt(11)).isEqualTo('-');
    // test peek and pop
    assertThat((char) code.peek()).isEqualTo('a');
    assertThat((char) code.pop()).isEqualTo('a');
    assertThat((char) code.pop()).isEqualTo('b');
    assertThat((char) code.pop()).isEqualTo('c');
    assertThat((char) code.pop()).isEqualTo('d');
    assertThat((char) code.peek()).isEqualTo('-');
    assertThat((char) code.pop()).isEqualTo('-');
    assertThat((char) code.pop()).isEqualTo('-');
    assertThat((char) code.pop()).isEqualTo('e');
    assertThat((char) code.pop()).isEqualTo('f');
    assertThat((char) code.pop()).isEqualTo('g');
    assertThat((char) code.pop()).isEqualTo('h');
    assertThat((char) code.pop()).isEqualTo('-');
    assertThat((char) code.pop()).isEqualTo('-');
  }

  @Test
  void theLengthShouldBeTheSameThanTheStringLength() {
    var myCode = "myCode";
    assertThat(new CodeBuffer(myCode, new CodeReaderConfiguration())).hasSize(6);
  }

  @Test
  void theLengthShouldDecreaseEachTimeTheInputStreamIsConsumed() {
    var myCode = "myCode";
    var codeBuffer = new CodeBuffer(myCode, new CodeReaderConfiguration());
    codeBuffer.pop();
    codeBuffer.pop();
    assertThat(codeBuffer).hasSize(4);
  }

  @Test
  void testSeveralCodeReaderFilter() throws Exception {
    var configuration = new CodeReaderConfiguration();
    configuration.setCodeReaderFilters(new ReplaceNumbersFilter(), new ReplaceCharFilter());
    var code = new CodeBuffer("abcd12efgh34", configuration);
    // test #charAt
    assertThat(code.charAt(0)).isEqualTo('*');
    assertThat(code.charAt(4)).isEqualTo('-');
    assertThat(code.charAt(5)).isEqualTo('-');
    assertThat(code.charAt(6)).isEqualTo('*');
    assertThat(code.charAt(10)).isEqualTo('-');
    assertThat(code.charAt(11)).isEqualTo('-');
    // test peek and pop
    assertThat((char) code.peek()).isEqualTo('*');
    assertThat((char) code.pop()).isEqualTo('*');
    assertThat((char) code.pop()).isEqualTo('*');
    assertThat((char) code.pop()).isEqualTo('*');
    assertThat((char) code.pop()).isEqualTo('*');
    assertThat((char) code.peek()).isEqualTo('-');
    assertThat((char) code.pop()).isEqualTo('-');
    assertThat((char) code.pop()).isEqualTo('-');
    assertThat((char) code.pop()).isEqualTo('*');
    assertThat((char) code.pop()).isEqualTo('*');
    assertThat((char) code.pop()).isEqualTo('*');
    assertThat((char) code.pop()).isEqualTo('*');
    assertThat((char) code.pop()).isEqualTo('-');
    assertThat((char) code.pop()).isEqualTo('-');
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void testChannelCodeReaderFilter() throws Exception {
    // create a windowing channel that drops the 2 first characters, keeps 6 characters and drops the rest of the line
    var configuration = new CodeReaderConfiguration();
    configuration.setCodeReaderFilters(new ChannelCodeReaderFilter(new Object(), new WindowingChannel()));
    var code = new CodeBuffer("0123456789\nABCDEFGHIJ", configuration);
    // test #charAt
    assertThat(code.charAt(0)).isEqualTo('2');
    assertThat(code.charAt(5)).isEqualTo('7');
    assertThat(code.charAt(6)).isEqualTo('\n');
    assertThat(code.charAt(7)).isEqualTo('C');
    assertThat(code.charAt(12)).isEqualTo('H');
    assertThat(code.intAt(13)).isEqualTo(-1);
    // test peek and pop
    assertThat((char) code.peek()).isEqualTo('2');
    assertThat((char) code.pop()).isEqualTo('2');
    assertThat((char) code.pop()).isEqualTo('3');
    assertThat((char) code.pop()).isEqualTo('4');
    assertThat((char) code.pop()).isEqualTo('5');
    assertThat((char) code.pop()).isEqualTo('6');
    assertThat((char) code.pop()).isEqualTo('7');// and 8 shouldn't show up
    assertThat((char) code.pop()).isEqualTo('\n');
    assertThat((char) code.peek()).isEqualTo('C');
    assertThat((char) code.pop()).isEqualTo('C');
    assertThat((char) code.pop()).isEqualTo('D');
    assertThat((char) code.pop()).isEqualTo('E');
    assertThat((char) code.pop()).isEqualTo('F');
    assertThat((char) code.pop()).isEqualTo('G');
    assertThat((char) code.pop()).isEqualTo('H');
    assertThat(code.pop()).isEqualTo(-1);
  }

  /**
   * Backward compatibility with a COBOL plugin: filter returns 0 instead of -1, when end of the stream has been
   * reached.
   */
  @Test
  @Timeout(1000)
  void testWrongEndOfStreamFilter() {
    var configuration = new CodeReaderConfiguration();
    configuration.setCodeReaderFilters(new WrongEndOfStreamFilter());
    new CodeBuffer("foo", configuration);
  }

  class WrongEndOfStreamFilter extends CodeReaderFilter<Object> {

    @Override
    public int read(char[] filteredBuffer, int offset, int length) throws IOException {
      return 0;
    }
  }

  class ReplaceNumbersFilter extends CodeReaderFilter<Object> {

    private final Pattern pattern = Pattern.compile("\\d");
    private final String REPLACEMENT = "-";

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
      var tempBuffer = new char[cbuf.length];
      int charCount = getReader().read(tempBuffer, off, len);
      if (charCount != -1) {
        var filteredString = pattern.matcher(new String(tempBuffer)).replaceAll(REPLACEMENT);
        System.arraycopy(filteredString.toCharArray(), 0, cbuf, 0, tempBuffer.length);
      }
      return charCount;
    }
  }

  class ReplaceCharFilter extends CodeReaderFilter<Object> {

    private final Pattern pattern = Pattern.compile("[a-zA-Z]");
    private final String REPLACEMENT = "*";

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
      var tempBuffer = new char[cbuf.length];
      int charCount = getReader().read(tempBuffer, off, len);
      if (charCount != -1) {
        var filteredString = pattern.matcher(new String(tempBuffer)).replaceAll(REPLACEMENT);
        System.arraycopy(filteredString.toCharArray(), 0, cbuf, 0, tempBuffer.length);
      }
      return charCount;
    }
  }

  @SuppressWarnings("rawtypes")
  class WindowingChannel extends Channel {

    @Override
    public boolean consume(CodeReader code, Object output) {
      int columnPosition = code.getColumnPosition();
      if (code.peek() == '\n') {
        return false;
      }
      if (columnPosition < 2 || columnPosition > 7) {
        code.pop();
        return true;
      }
      return false;
    }
  }
}
