/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
package org.sonar.cxx.sensors.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

public class TextScanner implements Closeable {

  private final Scanner scanner;
  private final String encoding;

  /**
   * Constructs a new {@code Scanner} that produces values scanned from the specified file.
   *
   * Bytes from the file are converted into characters using the found encoding.
   * Tries first to read a BOM. If no BOM exists defaultEncoding is used.
   *
   * @param source A file to be scanned
   * @param defaultEncoding The encoding type used to convert bytes from the file into characters if file has no BOM
   * @throws FileNotFoundException if source is not found
   * @throws IOException if error evaluating BOM
   * @throws IllegalArgumentException if the specified encoding is not found
   */
  public TextScanner(File source, String defaultEncoding) throws IOException {
    BOMInputStream bomInputStream = null;
    try {
      bomInputStream = new BOMInputStream(new FileInputStream(source),
                                          ByteOrderMark.UTF_8,
                                          ByteOrderMark.UTF_16LE,
                                          ByteOrderMark.UTF_16BE,
                                          ByteOrderMark.UTF_32LE,
                                          ByteOrderMark.UTF_32BE);
      ByteOrderMark bom = bomInputStream.getBOM();
      encoding = (bom != null) ? bom.getCharsetName() : defaultEncoding;
      scanner = new Scanner(bomInputStream, encoding);
    } catch (IOException e) {
      if (bomInputStream != null) {
        bomInputStream.close();
      }
      throw e;
    }
  }

  /**
   * Closes this scanner.
   *
   * <p>
   * If this scanner has not yet been closed then if its underlying {@linkplain java.lang.Readable readable} also
   * implements the {@link java.io.Closeable} interface then the readable's {@code close} method will be invoked.
   * If this scanner is already closed then invoking this method will have no effect.
   *
   * <p>
   * Attempting to perform search operations after a scanner has been closed will result
   * in an {@link IllegalStateException}.
   *
   */
  @Override
  public void close() {
    scanner.close();
  }

  /**
   * Sets this scanner's delimiting pattern to the specified pattern.
   *
   * @param pattern A delimiting pattern
   * @return this scanner
   */
  public TextScanner useDelimiter(Pattern pattern) {
    scanner.useDelimiter(pattern);
    return this;
  }

  /**
   * Returns true if there is another line in the input of this scanner.
   *
   * This method may block while waiting for input. The scanner does not advance past any input.
   *
   * @return true if and only if this scanner has another line of input
   * @throws IllegalStateException if this scanner is closed
   */
  public boolean hasNextLine() {
    return scanner.hasNextLine();
  }

  /**
   * Finds and returns the next complete token from this scanner.
   *
   * A complete token is preceded and followed by input that matches the delimiter pattern. This method may block
   * while waiting for input to scan, even if a previous invocation of {@link #hasNext} returned {@code true}.
   *
   * @return the next token
   * @throws NoSuchElementException if no more tokens are available
   * @throws IllegalStateException if this scanner is closed
   * @see java.util.Iterator
   */
  public String next() {
    return scanner.next();
  }

  /**
   * Advances this scanner past the current line and returns the input that was skipped.
   *
   * This method returns the rest of the current line, excluding any line separator at the end. The position is
   * set to the beginning of the next line.
   *
   * <p>
   * Since this method continues to search through the input looking for a line separator, it may buffer all
   * of the input searching for the line to skip if no line separators are present.
   *
   * @return the line that was skipped
   * @throws NoSuchElementException if no line was found
   * @throws IllegalStateException if this scanner is closed
   */
  public String nextLine() {
    return scanner.nextLine();
  }

  /**
   * @return encoding used by the scanner
   */
  public String encoding() {
    return encoding;
  }

}
