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
package org.sonar.cxx.sensors.compiler;

import java.io.File;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import org.sonar.api.batch.sensor.SensorContext;

/**
 * The interface a compiler parser has to implement in order to be used by CxxCompilerSensor
 *
 * @author Ferrand
 */
public interface CompilerParser {

  /**
   * Get the name of the compiler parser.
   *
   * @return The name of the compiler, used in the configuration.
   */
  String key();

  /**
   * Get the key identifying the rules repository for this compiler.
   *
   * @return The key of the rules repository associated with this compiler.
   */
  String rulesRepositoryKey();

  /**
   * Get the default regexp used to parse warning messages.
   *
   * @return The default regexp.
   */
  String defaultRegexp();

  /**
   * Get the default charset
   *
   * @return The default regexp.
   */
  String defaultCharset();

  static class Warning {

    public final String filename;
    public final String line;
    public final String id;
    public final String msg;

    Warning(@Nullable String filename, @Nullable String line, @Nullable String id, @Nullable String msg) {
      this.filename = getValueOrDefault(filename, "");
      this.line = getValueOrDefault(line, "");
      this.id = getValueOrDefault(id, "");
      this.msg = getValueOrDefault(msg, "");
    }

    private static String getValueOrDefault(@Nullable String value, String defaultValue) {
      return isNotNullOrEmpty(value) ? value : defaultValue;
    }

    private static boolean isNotNullOrEmpty(@Nullable String str) {
      return str != null && !str.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
      //check for self-comparison
      if (this == other) {
        return true;
      }

      if (other == null) {
        return false;
      }

      if (this.getClass() != other.getClass()) {
        return false;
      }

      //cast to native object is now safe
      Warning otherW = (Warning) other;
      return this.hashCode() == otherW.hashCode();
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(filename) ^ Objects.hashCode(line) ^ Objects.hashCode(id) ^ Objects.hashCode(msg);
    }

  }

  void processReport(final SensorContext context, File report, String charset, String reportRegEx, List<Warning> warnings)
    throws java.io.FileNotFoundException;
}
