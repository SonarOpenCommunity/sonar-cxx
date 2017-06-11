/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
public class JsonCompilationDatabaseCommandObject implements Serializable {
  /**
   * The working directory of the compilation. All paths specified in the command or file fields must be either absolute or relative to this directory.
   */
  public String directory;

  /**
   * The main translation unit source processed by this compilation step. This is used by tools as the key into the compilation database. There can be multiple command objects for the same file, for example if the same source file is compiled with different configurations.
   */
  public String file;

  /**
   * The compile command executed. After JSON unescaping, this must be a valid command to rerun the exact compilation step for the translation unit in the environment the build system uses. Parameters use shell quoting and shell escaping of quotes, with ‘"‘ and ‘\‘ being the only special characters. Shell expansion is not supported.
   */
  public String command;

  /**
   * The compile command executed as list of strings. Either arguments or command is required.
   */
  public String arguments;

  /**
   * The name of the output created by this compilation step. This field is optional. It can be used to distinguish different processing modes of the same input file.
   */
  public String output;

  /**
   * Extension to define defines
   */
  public HashMap<String,String> defines;

  /**
   * Extension to define include directories
   */
  public List<String> includes;

}
