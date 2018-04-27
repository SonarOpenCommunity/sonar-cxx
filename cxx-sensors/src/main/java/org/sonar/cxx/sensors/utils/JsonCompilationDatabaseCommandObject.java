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
package org.sonar.cxx.sensors.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JsonCompilationDatabaseCommandObject
 */
public class JsonCompilationDatabaseCommandObject implements Serializable {

  private static final long serialVersionUID = -1274733574224184395L;
  private String directory;
  private String file;
  private String command;
  private String arguments;
  private String output;

  /**
   * Extension to define defines
   */
  private HashMap<String, String> defines;

  /**
   * Extension to define include directories
   */
  private ArrayList<String> includes;

  /**
   * Initialize members
   */
  public JsonCompilationDatabaseCommandObject() {
    this.directory = "";
    this.file = "";
    this.command = "";
    this.arguments = "";
    this.output = "";
    this.defines = new HashMap<>();
    this.includes = new ArrayList<>();
  }

  /**
   * The working directory of the compilation. All paths specified in the command or file fields must be either absolute
   * or relative to this directory.
   */
  public String getDirectory() {
    return directory;
  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }

  /**
   * The main translation unit source processed by this compilation step. This is used by tools as the key into the
   * compilation database. There can be multiple command objects for the same file, for example if the same source file
   * is compiled with different configurations.
   */
  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }

  /**
   * The compile command executed. After JSON unescaping, this must be a valid command to rerun the exact compilation
   * step for the translation unit in the environment the build system uses. Parameters use shell quoting and shell
   * escaping of quotes, with ‘"‘ and ‘\‘ being the only special characters. Shell expansion is not supported.
   */
  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  /**
   * The compile command executed as list of strings. Either arguments or command is required.
   */
  public String getArguments() {
    return arguments;
  }

  public void setArguments(String arguments) {
    this.arguments = arguments;
  }

  /**
   * The name of the output created by this compilation step. This field is optional. It can be used to distinguish
   * different processing modes of the same input file.
   */
  public String getOutput() {
    return output;
  }

  public void setOutput(String output) {
    this.output = output;
  }

  /**
   * Extension to define defines
   */
  public Map<String, String> getDefines() {
    return defines;
  }

  public void setDefines(Map<String, String> defines) {
    this.defines = new HashMap<>(defines);
  }

  /**
   * Extension to define include directories
   */
  public List<String> getIncludes() {
    return Collections.unmodifiableList(includes);
  }

  public void setIncludes(List<String> includes) {
    this.includes = new ArrayList<>(includes);
  }

}
