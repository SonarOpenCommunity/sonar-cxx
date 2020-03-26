/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxCompilationUnitSettings;
import org.sonar.cxx.CxxSquidConfiguration;

/**
 * JsonCompilationDatabase
 */
public class JsonCompilationDatabase {

  private static final Logger LOG = Loggers.get(JsonCompilationDatabase.class);

  enum ArgNext {
    NONE, DEFINE, INCLUDE, IQUOTE, ISYSTEM, IDIRAFTER;
  }

  private JsonCompilationDatabase() {
    /* utility class is not meant to be instantiated */
  }

  /**
   * Set up the given CxxSquidConfiguration from the JSON compilation database
   *
   * @param squidConfig
   * @param compileCommandsFile
   * @throws IOException
   */
  public static void parse(CxxSquidConfiguration squidConfig, File compileCommandsFile) throws IOException {

    LOG.debug("Parsing 'JSON Compilation Database' format");

    var mapper = new ObjectMapper();
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
    mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

    JsonCompilationDatabaseCommandObject[] commandObjects
                                             = mapper.readValue(compileCommandsFile,
                                                                JsonCompilationDatabaseCommandObject[].class);
    Path cwd;

    for (var commandObject : commandObjects) {
      if (commandObject.getDirectory() != null) {
        cwd = Paths.get(commandObject.getDirectory());
      } else {
        cwd = Paths.get(".");
      }

      Path absPath = cwd.resolve(commandObject.getFile());
      var settings = new CxxCompilationUnitSettings();
      parseCommandObject(settings, cwd, commandObject);

      if ("__global__".equals(commandObject.getFile())) {
        squidConfig.setGlobalCompilationUnitSettings(settings);
      } else {
        squidConfig.addCompilationUnitSettings(absPath.toAbsolutePath().normalize().toString(), settings);
      }
    }
  }

  private static void parseCommandObject(CxxCompilationUnitSettings settings,
                                         Path cwd, JsonCompilationDatabaseCommandObject commandObject) {

    settings.setDefines(commandObject.getDefines());
    settings.setIncludes(commandObject.getIncludes());

    // No need to parse command lines as we have needed information
    if (commandObject.hasDefines() || commandObject.hasIncludes()) {
      return;
    }

    String cmdLine;

    if (commandObject.hasArguments()) {
      cmdLine = commandObject.getArguments().stream().collect(Collectors.joining(" "));
    } else if (commandObject.hasCommand()) {
      cmdLine = commandObject.getCommand();
    } else {
      return;
    }

    String[] args = tokenizeCommandLine(cmdLine);
    ArgNext next = ArgNext.NONE;

    var defines = new HashMap<String, String>();
    var includes = new ArrayList<Path>();
    var iSystem = new ArrayList<Path>();
    var iDirAfter = new ArrayList<Path>();

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
        }
        next = ArgNext.NONE;
      }
    }

    settings.setDefines(defines);
    includes.addAll(iSystem);
    includes.addAll(iDirAfter);
    settings.setIncludes(includes);
  }

  private static void addMacro(String keyValue, HashMap<String, String> defines) {
    String[] strings = keyValue.split("=", 2);
    if (strings.length == 1) {
      defines.put(strings[0], "1");
    } else {
      defines.put(strings[0], strings[1]);
    }
  }

  private static Path makeRelativeToCwd(Path cwd, String include) {
    return cwd.resolve(include).normalize();
  }

  private static String[] tokenizeCommandLine(String cmdLine) {
    var args = new ArrayList<String>();
    boolean escape = false;
    char stringOpen = 0;
    var sb = new StringBuilder(512);

    // Tokenize command line with support for escaping
    for (var ch : cmdLine.toCharArray()) {
      if (escape) {
        escape = false;
        sb.append(ch);
      } else {
        if (stringOpen == 0) {
          // String not open
          if (ch == '\\') {
            escape = true;
          } else if (ch == '\'') {
            stringOpen = '\'';
          } else if (ch == '\"') {
            stringOpen = '\"';
          } else if ((ch == ' ')
                       && (sb.length() > 0)) {
            args.add(sb.toString());
            sb = new StringBuilder(512);
          }
          if (ch != ' ') {
            sb.append(ch);
          }
        } else {
          // String open
          if (ch == '\\') {
            escape = true;
          } else if (ch == stringOpen) {
            stringOpen = 0;
          }

          sb.append(ch);
        }
      }
    }

    if (sb.length() > 0) {
      args.add(sb.toString());
    }

    return args.toArray(new String[0]);
  }

}
