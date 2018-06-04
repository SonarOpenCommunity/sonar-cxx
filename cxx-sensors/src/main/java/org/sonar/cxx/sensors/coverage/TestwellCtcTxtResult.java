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
package org.sonar.cxx.sensors.coverage;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.compile;

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

  public static final Pattern REPORT_HEADER = Pattern.compile(MON_SYM.patternString + "\\s+" + MON_DAT.patternString
    + "\\s+" + LIS_DTE.patternString + "\\s+" + COV_VIW.patternString, MULTILINE);
  public static final Pattern REPORT_FOOTER = Pattern.compile(SRC_FLS.patternString + "\\s+" + HDR_EXT.patternString + "\\s+"
    + FKT_EXT.patternString + "\\s*" + SRC_LNS.patternString, MULTILINE);
  public static final Pattern FILE_HEADER = Pattern.compile(FILE_MONI.patternString + "\\s+" + FILE_INST.patternString, MULTILINE);
  public static final Pattern SECTION_SEP = compile("^-{77}|={77}$", MULTILINE);
  public static final Pattern LINE_RESULT = compile("^(?: {10}| *([0-9Ee]+)) (?: {10}| *([0-9Ee]+)) -? *([0-9Ee]+) *(?:}([+-]+))?(.*)$", MULTILINE);
  public static final Pattern FILE_RESULT = compile(FILE_COND.patternString + "\\s+" + FILE_STMT.patternString, MULTILINE);

  private final String patternString;

  private TestwellCtcTxtResult(String key) {
    patternString = key;
  }

}
