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

import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * This class represents a configuration property, which is made of a name, a description (which may be empty),
 * a default value, and optionnally a validation callback.
 *
 * @since 1.17
 */
public class ConfigurationProperty {

  private static final ValidationCallback NO_VALIDATION = (String newValueCandidate) -> "";

  private final String name;
  private final String description;
  private String value;
  private final ValidationCallback validationCallback;

  public ConfigurationProperty(String name, String description, String defaultValue) {
    this(name, description, defaultValue, NO_VALIDATION);
  }

  /**
   *
   * @param name
   * @param description
   * @param defaultValue
   * @param validationCallback The validation callback. Note that handy ones are available out-of-the-box by the
   * {@link Validators} class.
   */
  public ConfigurationProperty(@Nonnull String name, @Nonnull String description, @Nonnull String defaultValue,
                               @Nonnull ValidationCallback validationCallback) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(description);
    Objects.requireNonNull(defaultValue);
    Objects.requireNonNull(validationCallback);

    var errorMessage = validationCallback.validate(defaultValue);
    if (!"".equals(errorMessage)) {
      throw new IllegalArgumentException("The default value \"" + defaultValue + "\" did not pass validation: "
                                           + errorMessage);
    }

    this.name = name;
    this.description = description;
    this.validationCallback = validationCallback;
    this.value = defaultValue;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String validate(String newValueCandidate) {
    return validationCallback.validate(newValueCandidate);
  }

  public void setValue(String value) {
    var errorMessage = validate(value);
    if (!"".equals(errorMessage)) {
      throw new IllegalArgumentException("The value \"" + value + "\" did not pass validation: " + errorMessage);
    }

    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
