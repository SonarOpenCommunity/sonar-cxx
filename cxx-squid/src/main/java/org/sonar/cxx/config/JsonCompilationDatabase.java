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
package org.sonar.cxx.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JsonCompilationDatabase
 */
public class JsonCompilationDatabase {

  private static final Logger LOG = LoggerFactory.getLogger(JsonCompilationDatabase.class);

  private final CxxSquidConfiguration squidConfig;

  public JsonCompilationDatabase(CxxSquidConfiguration squidConfig) {
    this.squidConfig = squidConfig;
  }

  private static void addMacro(String keyValue, Map<String, String> defines) {
    String[] strings = keyValue.split("=", 2);
    if (strings.length == 1) {
      defines.put(strings[0], "1");
    } else {
      defines.put(strings[0], strings[1]);
    }
  }

  private static String removeApostophs(String str) {
    if (str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"') {
      return str.substring(1, str.length() - 1);
    }
    return str;
  }

  private static Path makeRelativeToCwd(Path cwd, String include) {
    return cwd.resolve(removeApostophs(include)).normalize();
  }

  /**
   * Tokenize command line with support for escaping
   */
  private static String[] tokenizeCommandLine(String cmdLine) {
    var arg = new StringBuilder(256);
    var args = new ArrayList<String>();
    var insideStr = false;

    for (int i = 0; i < cmdLine.length(); i++) {
      var ch = cmdLine.charAt(i);

      if (ch == ' ' && !insideStr) { // blanks separate arguments (outside of strings)
        if (arg.length() > 0) {
          args.add(removeApostophs(arg.toString()));
          arg.setLength(0);
        }
      } else if (ch == '"') { // begin or end of string
        insideStr = !insideStr;
        arg.append(ch);
      } else {
        arg.append(ch);
      }
    }

    if (arg.length() > 0) {
      args.add(removeApostophs(arg.toString()));
    }

    return args.toArray(new String[0]);
  }

  /**
   * Set up the given CxxSquidConfiguration from the JSON compilation database
   *
   * @param compileCommandsFile
   * @throws IOException
   */
  public void parse(File compileCommandsFile) throws IOException {

    LOG.debug("Parsing 'JSON Compilation Database' format");

    var mapper = new ObjectMapper();
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
    mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

    JsonCompilationDatabaseCommandObject[] commandObjects
      = mapper.readValue(compileCommandsFile,
        JsonCompilationDatabaseCommandObject[].class);

    for (var commandObject : commandObjects) {
      parseCommandObject(commandObject);
    }
  }

  private void parseCommandObject(JsonCompilationDatabaseCommandObject commandObject) {

    var defines = commandObject.getDefines();
    var includes = commandObject.getIncludes();

    Path cwd;
    if (commandObject.getDirectory() != null) {
      cwd = Path.of(commandObject.getDirectory());
    } else {
      cwd = Path.of(".");
    }

    String level;
    if ("__global__".equals(commandObject.getFile())) {
      level = CxxSquidConfiguration.GLOBAL;
    } else {
      level = cwd.resolve(commandObject.getFile()).toAbsolutePath().normalize().toString();
    }

    // No need to parse command lines if we have needed information
    if (!(commandObject.hasDefines() || commandObject.hasIncludes())) {
      String[] args;

      if (commandObject.hasArguments()) {
        args = commandObject.getArguments().toArray(new String[0]);
        if (args.length == 1) {
          args = tokenizeCommandLine(args[0]);
        }
      } else if (commandObject.hasCommand()) {
        args = tokenizeCommandLine(commandObject.getCommand());
      } else {
        return;
      }

      defines = new HashMap<>();
      includes = new ArrayList<>();
      var iSystem = new ArrayList<Path>();
      var iDirAfter = new ArrayList<Path>();
      var next = ArgNext.NONE;

      for (var arg : args) {
        if (arg.startsWith("-D")) {
          arg = arg.substring(2);
          next = ArgNext.DEFINE;
        } else if (arg.startsWith("-I")) {
          arg = arg.substring(2);
          next = ArgNext.INCLUDE;
        } else if (arg.startsWith("-iquote")) {
          arg = arg.substring(7);
          next = ArgNext.INCLUDE;
        } else if (arg.startsWith("-isystem")) {
          arg = arg.substring(8);
          next = ArgNext.ISYSTEM;
        } else if (arg.startsWith("-idirafter")) {
          arg = arg.substring(10);
          next = ArgNext.IDIRAFTER;
        }

        if ((next != ArgNext.NONE) && !arg.isEmpty()) {
          switch (next) {
            case DEFINE:
              addMacro(arg, defines);
              break;
            case INCLUDE:
            case IQUOTE:
              includes.add(makeRelativeToCwd(cwd, arg));
              break;
            case ISYSTEM:
              iSystem.add(makeRelativeToCwd(cwd, arg));
              break;
            case IDIRAFTER:
              iDirAfter.add(makeRelativeToCwd(cwd, arg));
              break;
            default:
              break;
          }
          next = ArgNext.NONE;
        }
      }

      includes.addAll(iSystem);
      includes.addAll(iDirAfter);
    }

    addDefines(level, defines);
    addIncludes(level, includes);
  }

  private void addDefines(String level, Map<String, String> defines) {
    defines.forEach((String k, String v) -> {
      squidConfig.add(level, CxxSquidConfiguration.DEFINES, k + " " + v);
    });
  }

  private void addIncludes(String level, List<Path> includes) {
    includes.forEach((Path include) -> {
      squidConfig.add(level, CxxSquidConfiguration.INCLUDE_DIRECTORIES, include.toString());
    });
  }

  private enum ArgNext {
    NONE, DEFINE, INCLUDE, IQUOTE, ISYSTEM, IDIRAFTER;
  }

}
