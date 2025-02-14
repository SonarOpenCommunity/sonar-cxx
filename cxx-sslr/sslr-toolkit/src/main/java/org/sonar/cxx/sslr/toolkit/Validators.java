/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2025 SonarOpenCommunity
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
package org.sonar.cxx.sslr.toolkit; // cxx: in use

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Provides a few handy configuration property validators out-of-the-box.
 *
 * @since 1.17
 */
public final class Validators {

  private static final CharsetValidator CHARSET_VALIDATOR = new CharsetValidator();
  private static final BooleanValidator BOOLEAN_VALIDATOR = new BooleanValidator();

  private Validators() {
  }

  /**
   * Validates that the property holds a valid {@link Charset} name.
   *
   * @return A charset validator
   */
  public static ValidationCallback charsetValidator() {
    return CHARSET_VALIDATOR;
  }

  private static class CharsetValidator implements ValidationCallback {

    @Override
    public String validate(String newValueCandidate) {
      try {
        Charset.forName(newValueCandidate);
        return "";
      } catch (IllegalCharsetNameException e) {
        return "Illegal charset: " + e.getMessage();
      } catch (UnsupportedCharsetException e) {
        return "Unsupported charset: " + e.getMessage();
      }
    }

  }

  /**
   * Validates that the property holds an integer within the given lower and upper bounds.
   *
   * @param lowerBound
   * @param upperBound
   * @return An integer range validator
   */
  public static ValidationCallback integerRangeValidator(int lowerBound, int upperBound) {
    return new IntegerRangeValidator(lowerBound, upperBound);
  }

  private static class IntegerRangeValidator implements ValidationCallback {

    private final int lowerBound;
    private final int upperBound;

    public IntegerRangeValidator(int lowerBound, int upperBound) {
      if (lowerBound > upperBound) {
        throw new IllegalArgumentException("lowerBound(" + lowerBound + ") <= upperBound(" + upperBound + ")");
      }

      this.lowerBound = lowerBound;
      this.upperBound = upperBound;
    }

    @Override
    public String validate(String newValueCandidate) {
      try {
        int value = Integer.parseInt(newValueCandidate);

        if (value < lowerBound || value > upperBound) {
          return getErrorMessage(value);
        } else {
          return "";
        }
      } catch (NumberFormatException e) {
        return "Not an integer: " + newValueCandidate;
      }
    }

    private String getErrorMessage(int value) {
      String errorMessage;

      if (lowerBound == upperBound) {
        errorMessage = "Must be equal to " + lowerBound + ": " + value;
      } else if (upperBound == Integer.MAX_VALUE) {
        switch (lowerBound) {
          case 0:
            errorMessage = "Must be positive or 0: " + value;
            break;
          case 1:
            errorMessage = "Must be strictly positive: " + value;
            break;
          default:
            errorMessage = "Must be greater or equal to " + lowerBound + ": " + value;
            break;
        }
      } else if (lowerBound == Integer.MIN_VALUE) {
        switch (upperBound) {
          case 0:
            errorMessage = "Must be negative or 0: " + value;
            break;
          case -1:
            errorMessage = "Must be strictly negative: " + value;
            break;
          default:
            errorMessage = "Must be lower or equal to " + upperBound + ": " + value;
            break;
        }
      } else {
        errorMessage = "Must be between " + lowerBound + " and " + upperBound + ": " + value;
      }

      return errorMessage;
    }
  }

  /**
   * Validates that the property holds a boolean value, i.e. either "true" or "false".
   *
   * @return A boolean validator
   */
  public static ValidationCallback booleanValidator() {
    return BOOLEAN_VALIDATOR;
  }

  private static class BooleanValidator implements ValidationCallback {

    @Override
    public String validate(String newValueCandidate) {
      return !"false".equals(newValueCandidate) && !"true".equals(newValueCandidate)
               ? ("Must be either \"true\" or \"false\": " + newValueCandidate) : "";
    }

  }

}
