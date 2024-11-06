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
package com.sonar.cxx.sslr.api.typed;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Input used by the ActionParser to lex and parse a character sequence.
 *
 * @see ActionParser
 * @since 1.21
 */
public class Input {

  private static final URI FAKE_URI = new File("tests://unittests").toURI();

  private final char[] inputChars;
  private final URI uri;
  private final int[] newLineIndexes;

  /**
   * Create new Input object.
   *
   * @param input input characters for this Input object.
   */
  public Input(char[] input) {
    this(input, FAKE_URI);
  }

  /**
   * Create new Input object.
   *
   * @param input input characters for this Input object
   * @param uri the URI which belongs to this Input
   */
  public Input(char[] input, URI uri) {
    this.inputChars = input;
    this.uri = uri;

    List<Integer> newLineIndexesBuilder = new ArrayList<>();
    for (int i = 0; i < input.length; i++) {
      if (isNewLine(input, i)) {
        newLineIndexesBuilder.add(i + 1);
      }
    }
    this.newLineIndexes = new int[newLineIndexesBuilder.size()];
    for (int i = 0; i < newLineIndexes.length; i++) {
      this.newLineIndexes[i] = newLineIndexesBuilder.get(i);
    }
  }

  /**
   * Get the input characters of this Input object.
   *
   * @return return the input characters of this Input object
   */
  public char[] input() {
    return inputChars;
  }

  /**
   * Get the URI which belongs to this Input.
   *
   * @return return the URI which belongs to this Input.
   */
  public URI uri() {
    return uri;
  }

  /**
   * Returns a string that is a substring of the input characters of this Input. The substring begins at the specified
   * {@code from} and extends to the character at index {@code to - 1}. Thus the length of the substring is
   * {@code to-from}.
   *
   * @param from index of the first character in the string to copy
   * @param to index after the last character in the string to copy
   * @return the specified substring
   */
  public String substring(int from, int to) {
    return String.copyValueOf(inputChars, from, to - from);
  }

  /**
   * Return line and column (offset in line) for a given index.
   *
   * @param index index to search for
   * @return integer array with line number (index 0) and column number (index 1)
   */
  public int[] lineAndColumnAt(int index) {
    var result = new int[2];
    result[0] = lineAt(index);
    result[1] = index - lineStartIndex(result[0]) + 1;
    return result;
  }

  private int lineAt(int index) {
    int i = Arrays.binarySearch(newLineIndexes, index);
    return i >= 0 ? (i + 2) : -i;
  }

  private int lineStartIndex(int line) {
    return line == 1 ? 0 : newLineIndexes[line - 2];
  }

  /**
   * New lines are: \n, \r\n (in which case true is returned for the \n) and \r alone.
   */
  private static boolean isNewLine(char[] input, int i) {
    return input[i] == '\n' || (input[i] == '\r' && (i + 1 == input.length || input[i + 1] != '\n'));
  }

}
