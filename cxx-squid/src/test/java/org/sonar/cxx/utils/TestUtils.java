/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) SonarOpenCommunity
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
package org.sonar.cxx.utils;

import com.sonar.cxx.sslr.api.TokenType;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import javax.annotation.CheckForNull;
import org.sonar.cxx.parser.CxxPunctuator;

public final class TestUtils {

  public static File loadResource(String resourceName) {
    URL resource = TestUtils.class.getResource(resourceName);
    File resourceAsFile = null;
    try {
      resourceAsFile = new File(resource.toURI());
    } catch (URISyntaxException e) {
      System.out.println("Cannot load resource: " + resourceName);
    }

    return resourceAsFile;
  }

  /**
   * Search for a test resource in the classpath. For example getResource("org/sonar/MyClass/foo.txt");
   *
   * @param path the starting slash is optional
   * @return the resource. Null if resource not found
   */
  @CheckForNull
  public static File getResource(String path) {
    String resourcePath = path;
    if (!resourcePath.startsWith("/")) {
      resourcePath = "/" + resourcePath;
    }
    URL url = TestUtils.class.getResource(resourcePath);
    if (url != null) {
      try {
        return new File(url.toURI());
      } catch (URISyntaxException e) {
        return null;
      }
    }
    return null;
  }

  /**
   * @return the punctuator token type the lexer gives {@code value}, or empty if it is no punctuator
   */
  public static Optional<TokenType> punctuatorType(String value) {
    return Arrays.stream(CxxPunctuator.values())
      .filter(punctuator -> punctuator.getValue().equals(value))
      .findFirst()
      .map(TokenType.class::cast);
  }

  private TestUtils() {
    // utility class
  }

}
