/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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
package org.sonar.cxx.config;

import java.io.Serializable;
import java.nio.file.Path;
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
  private List<String> arguments;
  private String output;

  /**
   * Extension to define defines
   */
  private HashMap<String, String> defines;

  /**
   * Extension to define include directories
   */
  private ArrayList<Path> includes;

  /**
   * Initialize members
   */
  public JsonCompilationDatabaseCommandObject() {
    this.directory = "";
    this.file = "";
    this.command = "";
    this.arguments = new ArrayList<>();
    this.output = "";
    this.defines = new HashMap<>();
    this.includes = new ArrayList<>();
  }

  /**
   * The working directory of the compilation.All paths specified in the command or file fields must be either absolute
   * or relative to this directory.
   *
   * @return working directory
   */
  public String getDirectory() {
    return directory;
  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }

  /**
   * The main translation unit source processed by this compilation step.This is used by tools as the key into the
   * compilation database. There can be multiple command objects for the same file, for example if the same source file
   * is compiled with different configurations.
   *
   * @return main translation unit
   */
  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }

  /**
   * The compile command executed.After JSON unescaping, this must be a valid command to rerun the exact compilation
   * step for the translation unit in the environment the build system uses. Parameters use shell quoting and shell
   * escaping of quotes, with ‘"‘ and ‘\‘ being the only special characters. Shell expansion is not supported.
   *
   * @return true if compile command available
   */
  public boolean hasCommand() {
    return !command.isEmpty();
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getCommand() {
    return command;
  }

  /**
   * The compile command executed as list of strings.Either arguments or command is required.
   *
   * @return true if arguments available
   */
  public boolean hasArguments() {
    return !arguments.isEmpty();
  }

  public void setArguments(List<String> arguments) {
    this.arguments = new ArrayList<>(arguments);
  }

  public List<String> getArguments() {
    return new ArrayList<>(arguments);
  }

  /**
   * The name of the output created by this compilation step.This field is optional. It can be used to distinguish
   * different processing modes of the same input file.
   *
   * @return name of the output
   */
  public String getOutput() {
    return output;
  }

  public void setOutput(String output) {
    this.output = output;
  }

  /**
   * Extension to define defines
   *
   * @return true if defines are available
   */
  public boolean hasDefines() {
    return !defines.isEmpty();
  }

  public void setDefines(Map<String, String> defines) {
    this.defines = new HashMap<>(defines);
  }

  public Map<String, String> getDefines() {
    return new HashMap<>(defines);
  }

  /**
   * Extension to define include directories
   *
   * @return true if include directories available
   */
  public boolean hasIncludes() {
    return !includes.isEmpty();
  }

  public void setIncludes(List<Path> includes) {
    this.includes = new ArrayList<>(includes);
  }

  public List<Path> getIncludes() {
    return Collections.unmodifiableList(includes);
  }

}
