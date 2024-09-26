/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2024 SonarOpenCommunity
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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * The CodeBuffer class provides all the basic features required to manipulate a source code character stream. Those
 * features are :
 * <ul>
 * <li>Read and consume next source code character : pop()</li>
 * <li>Retrieve last consumed character : lastChar()</li>
 * <li>Read without consuming next source code character : peek()</li>
 * <li>Read without consuming character at the specified index after the cursor</li>
 * <li>Position of the pending cursor : line and column</li>
 * </ul>
 */
public class CodeBuffer implements CharSequence {

  private int lastChar = -1;
  private Cursor cursor;
  private char[] buffer;
  private int bufferPosition = 0;
  private static final char LF = '\n';
  private static final char CR = '\r';
  private int tabWidth;

  private boolean recordingMode = false;
  private StringBuilder recordedCharacters = new StringBuilder();

  protected CodeBuffer(String code, CodeReaderConfiguration configuration) {
    this(new StringReader(code), configuration);
  }

  /**
   * Note that this constructor will read everything from reader and will close it.
   */
  protected CodeBuffer(Reader initialCodeReader, CodeReaderConfiguration configuration) {

    /* Make sure the reader passed-in gets closed when done. */
    try (var reader = initialCodeReader) {
      lastChar = -1;
      cursor = new Cursor();
      tabWidth = configuration.getTabWidth();

      var filteredReader = reader;

      /* Setup the filters on the reader */
      for (CodeReaderFilter<?> codeReaderFilter : configuration.getCodeReaderFilters()) {
        filteredReader = new Filter(filteredReader, codeReaderFilter, configuration);
      }

      /* Make sure to close the filtered reader when done (cascading through the lot) */
      try (var usedReader = filteredReader) {
        buffer = read(usedReader);
      }

    } catch (IOException e) {
      throw new ChannelException(e.getMessage(), e);
    }
  }

  private char[] read(Reader reader) throws IOException {
    var sb = new StringBuilder();
    var str = new char[4 * 1024];
    int n;
    while ((n = reader.read(str)) > 0) {
      sb.append(str, 0, n);
    }
    return sb.toString().toCharArray();
  }

  /**
   * Read and consume the next character
   *
   * @return the next character or -1 if the end of the stream is reached
   */
  public final int pop() {
    if (bufferPosition >= buffer.length) {
      return -1;
    }
    int character = buffer[bufferPosition];
    bufferPosition++;
    updateCursorPosition(character);
    if (recordingMode) {
      recordedCharacters.append((char) character);
    }
    lastChar = character;
    return character;
  }

  /**
   * Read and consume the next characters
   *
   * @param number number of characters to consume
   * @return the next character or -1 if the end of the stream is reached
   */
  public final int skip(int number) {
    while (number != 0) {
      pop();
      number--;
    }
    return peek();
  }

  private void updateCursorPosition(int character) {
    // see Java Language Specification : http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.4
    if (character == LF || character == CR && peek() != LF) {
      cursor.line++;
      cursor.column = 0;
    } else if (character == '\t') {
      cursor.column += tabWidth;
    } else {
      cursor.column++;
    }
  }

  /**
   * Looks at the last consumed character
   *
   * @return the last character or -1 if the no character has been yet consumed
   */
  public final int lastChar() {
    return lastChar;
  }

  /**
   * Looks at the next character without consuming it
   *
   * @return the next character or -1 if the end of the stream has been reached
   */
  public final int peek() {
    return intAt(0);
  }

  /**
   * @return the current line of the cursor
   */
  public final int getLinePosition() {
    return cursor.line;
  }

  public final Cursor getCursor() {
    return cursor;
  }

  /**
   * @return the current column of the cursor
   */
  public final int getColumnPosition() {
    return cursor.column;
  }

  /**
   * Overrides the current column position
   */
  public final CodeBuffer setColumnPosition(int cp) {
    this.cursor.column = cp;
    return this;
  }

  /**
   * Overrides the current line position
   */
  public final void setLinePosition(int lp) {
    this.cursor.line = lp;
  }

  public final void startRecording() {
    recordingMode = true;
  }

  public final CharSequence stopRecording() {
    recordingMode = false;
    CharSequence result = recordedCharacters;
    recordedCharacters = new StringBuilder();
    return result;
  }

  /**
   * Returns the character at the specified index after the cursor without consuming it
   *
   * @param index
   * the relative index of the character to be returned
   * @return the desired character
   * @see java.lang.CharSequence#charAt(int)
   */
  @Override
  public final char charAt(int index) {
    return (char) intAt(index);
  }

  protected final int intAt(int index) {
    if (bufferPosition + index >= buffer.length) {
      return -1;
    }
    return buffer[bufferPosition + index];
  }

  /**
   * Returns the relative length of the string (i.e. excluding the popped chars)
   */
  @Override
  public final int length() {
    return buffer.length - bufferPosition;
  }

  @Override
  public final CharSequence subSequence(int start, int end) {
    throw new UnsupportedOperationException();
  }

  @Override
  public final String toString() {
    var result = new StringBuilder();
    result.append("CodeReader(");
    result.append("line:").append(cursor.line);
    result.append("|column:").append(cursor.column);
    result.append("|cursor value:'").append((char) peek()).append("'");
    result.append(")");
    return result.toString();
  }

  public static class Cursor implements Cloneable {

    private int line = 1;
    private int column = 0;

    public int getLine() {
      return line;
    }

    public int getColumn() {
      return column;
    }

    @Override
    public Cursor clone() {
      Cursor clone;

      try {
        clone = (Cursor) super.clone();
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }

      clone.column = column;
      clone.line = line;

      return clone;
    }
  }

  /**
   * Bridge class between CodeBuffer and CodeReaderFilter
   */
  static final class Filter extends FilterReader {

    private final CodeReaderFilter<?> codeReaderFilter;

    public Filter(Reader in, CodeReaderFilter<?> codeReaderFilter, CodeReaderConfiguration configuration) {
      super(in);
      this.codeReaderFilter = codeReaderFilter;
      this.codeReaderFilter.setConfiguration(configuration.cloneWithoutCodeReaderFilters());
      this.codeReaderFilter.setReader(in);
    }

    @Override
    public int read() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
      int read = codeReaderFilter.read(cbuf, off, len);
      return read == 0 ? -1 : read;
    }

    @Override
    public long skip(long n) throws IOException {
      throw new UnsupportedOperationException();
    }

  }
}
