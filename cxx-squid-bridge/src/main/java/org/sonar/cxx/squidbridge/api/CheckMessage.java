/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2025 SonarOpenCommunity
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
 * fork of SSLR Squid Bridge: https://github.com/SonarSource/sslr-squid-bridge/tree/2.6.1
 * Copyright (C) 2010 SonarSource / mailto: sonarqube@googlegroups.com / license: LGPL v3
 */
package org.sonar.cxx.squidbridge.api;

import java.text.MessageFormat;
import java.util.Locale;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Create a message for a check
 *
 * @see CodeCheck
 */
public class CheckMessage {

  private Integer line;
  private Double cost;
  private SourceCode sourceCode;
  private final Object check;
  private final String defaultMessage;
  private final Object[] messageArguments;

  /**
   * Create a message for a check. Defines default message with optional message arguments.
   *
   * @param check check that lead to this message
   * @param message default message text (can contain placeholders for arguments)
   * @param messageArguments additional arguments to format the message (message must contain placeholders)
   */
  public CheckMessage(Object check, String message, Object... messageArguments) {
    this.check = check;
    this.defaultMessage = message;
    this.messageArguments = messageArguments;
  }

  /**
   * Define node in the source code tree to which this message belongs.
   *
   * @param sourceCode node in the source code tree to which this message belongs
   */
  public void setSourceCode(SourceCode sourceCode) {
    this.sourceCode = sourceCode;
  }

  /**
   * Get node in the source code tree to which this message belongs.
   *
   * @return node in the source code tree to which this message belongs
   */
  public SourceCode getSourceCode() {
    return sourceCode;
  }

  /**
   * Define line in the source code to which this message belongs.
   *
   * @param line line in the source code to which this message belongs
   */
  public void setLine(int line) {
    this.line = line;
  }

  /**
   * Get line in the source code to which this message belongs.
   *
   * @return line in the source code to which this message belongs
   */
  public Integer getLine() {
    return line;
  }

  /**
   * Defines the cost for fixing the issue.
   *
   * @param cost cost for fixing the issue
   */
  public void setCost(double cost) {
    this.cost = cost;
  }

  /**
   * Get the cost for fixing the issue.
   *
   * @return cost for fixing the issue
   */
  public Double getCost() {
    return cost;
  }

  /**
   * Get the related check.
   *
   * @return the related check
   */
  public Object getCheck() {
    return check;
  }

  /**
   * Get the default message text.
   *
   * @return default message text (can contain placeholders for arguments)
   */
  public String getDefaultMessage() {
    return defaultMessage;
  }

  /**
   * Get additional arguments to format the message.
   *
   * @return additional arguments to format the message
   */
  public Object[] getMessageArguments() {
    return messageArguments;
  }

  /**
   * Get the formatted text (default message with parameters).
   *
   * @param locale locale to use for formatting
   * @return formatted text (default message with parameters)
   */
  public String getText(Locale locale) {
    return formatDefaultMessage();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("source", sourceCode).append("check", check).append("msg", defaultMessage)
      .append("line", line).toString();
  }

  /**
   * Format the default message, replaces placeholder with arguments.
   *
   * @return message with resolved parameters
   */
  public String formatDefaultMessage() {
    if (messageArguments.length == 0) {
      return defaultMessage;
    } else {
      return MessageFormat.format(defaultMessage, messageArguments);
    }
  }

}
