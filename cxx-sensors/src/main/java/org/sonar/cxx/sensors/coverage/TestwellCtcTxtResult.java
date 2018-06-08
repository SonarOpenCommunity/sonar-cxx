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
/*
 * Derived from https://github.com/Londran/sonar-ctc/blob/master/src/main/java/org/sonar/plugins/ctc/api/parser/CtcResult.java
 */
package org.sonar.cxx.sensors.coverage;

import java.util.regex.Pattern;

public enum TestwellCtcTxtResult {

  MON_SYM("Symbol file\\(s\\) used   : (.*$(?:\\s+^ +.*$)*)"),
  MON_DAT("Data file\\(s\\) used     : (.*$(?:\\s+^ +.*$)*)"),
  LIS_DTE("Listing produced at   : (.*)$"),
  COV_VIW("Coverage view         : (.*)$"),
  SRC_FLS("Source files       : (.*)$"),
  HDR_EXT("(?:Headers extracted  : .*$)?"),
  FKT_EXT("(?:Functions          : .*$)?"),
  SRC_LNS("Source lines       : (.*)$"),
  FILE_MONI("MONITORED (?:.*) FILE : (.*)$"),
  FILE_INST("INSTRUMENTATION MODE  : (.*)$"),
  FILE_COND("^\\Q***TER\\E +\\d+ % \\( *(\\d+)/ *(\\d+)\\) of FILE (?:.*)$"),
  FILE_STMT("^ {6} +\\d+ % \\( *(\\d+)/ *(\\d+)\\) statement.*$");

  public static final Pattern REPORT_HEADER = Pattern.compile(String.join("\\s+", MON_SYM.patternString, MON_DAT.patternString, LIS_DTE.patternString, COV_VIW.patternString), Pattern.MULTILINE);
  public static final Pattern REPORT_FOOTER = Pattern.compile(String.join("\\s+", SRC_FLS.patternString, HDR_EXT.patternString, FKT_EXT.patternString, SRC_LNS.patternString), Pattern.MULTILINE);
  public static final Pattern FILE_HEADER = Pattern.compile(String.join("\\s+", FILE_MONI.patternString, FILE_INST.patternString), Pattern.MULTILINE);
  public static final Pattern SECTION_SEP = Pattern.compile("^-{77}|={77}$", Pattern.MULTILINE);
  public static final Pattern LINE_RESULT = Pattern.compile("^(?: {10}| *([0-9Ee]+)) (?: {10}| *([0-9Ee]+)) -? *([0-9Ee]+) *(?:}([+-]+))?(.*)$", Pattern.MULTILINE);
  public static final Pattern FILE_RESULT = Pattern.compile(String.join("\\s+", FILE_COND.patternString, FILE_STMT.patternString), Pattern.MULTILINE);

  private final String patternString;

  private TestwellCtcTxtResult(String key) {
    patternString = key;
  }

}
