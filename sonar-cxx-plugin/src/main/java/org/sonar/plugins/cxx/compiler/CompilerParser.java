/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.compiler;

import java.io.File;
import java.util.List;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;

/**
 * The interface a compiler parser has to implement in order to be used
 * by CxxCompilerSensor
 *
 * @author Ferrand
 */
public interface CompilerParser {
    /**
     * Get the name of the compiler parser.
     * @return The name of the compiler, used in the configuration.
     */
    String key();

    /**
     * Get the key identifying the rules repository for this compiler.
     * @return The key of the rules repository associated with this compiler.
     */
    String rulesRepositoryKey();

    /**
     * Get the default regexp used to parse warning messages.
     * @return The default regexp.
     */
    String defaultRegexp();

    /**
     * Get the default charset
     * @return The default regexp.
     */
    String defaultCharset();

    static class Warning {
        public final String filename;
        public final String line;
        public final String id;
        public final String msg;

        Warning(String filename, String line, String id, String msg) {
          this.filename = filename;
          this.line = line;
          this.id = id;
          this.msg = msg;
        }
    }

    void processReport(final Project project, final SensorContext context, File report, String charset, String reportRegEx, List<Warning> warnings)
        throws java.io.FileNotFoundException;
}
