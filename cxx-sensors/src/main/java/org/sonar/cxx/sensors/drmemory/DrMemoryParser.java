/*
 * Sonar C++ Plugin (Community)
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
package org.sonar.cxx.sensors.drmemory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.sensors.drmemory.DrMemoryParser.DrMemoryError.Location;
import org.sonar.cxx.sensors.utils.CxxUtils;

public final class DrMemoryParser {

  private static final Logger LOG = Loggers.get(DrMemoryParser.class);
  private static final Pattern RX_MESSAGE_FINDER = Pattern.compile("^Error #\\d+:(.*)");
  private static final Pattern RX_FILE_FINDER = Pattern.compile("^.*\\[(.*):(\\d+)\\]$");
  private static final int TOP_COUNT = 4;

  private DrMemoryParser() {
  }

  /**
   * DrMemory parser
   *
   * @param file with findings
   * @param encoding file encoding character set
   * @return list of issues extracted from file
   */
  public static List<DrMemoryError> parse(File file, String encoding) {

    var result = new ArrayList<DrMemoryError>();

    List<String> elements = getElements(file, encoding);

    for (var element : elements) {
      Matcher m = RX_MESSAGE_FINDER.matcher(element);

      if (m.find()) {
        var error = new DrMemoryError();
        error.type = extractErrorType(m.group(1));
        String[] elementSplitted = CxxUtils.EOL_PATTERN.split(element);
        error.message = elementSplitted[0];
        for (var elementPart : elementSplitted) {
          Matcher locationMatcher = RX_FILE_FINDER.matcher(elementPart);
          if (locationMatcher.find()) {
            var location = new Location();
            location.file = locationMatcher.group(1);
            location.line = Integer.valueOf(locationMatcher.group(2));
            error.stackTrace.add(location);
          }
        }
        result.add(error);

      }
    }

    return result;
  }

  /**
   * get all DrMemory elements from file
   *
   * @param file with findings
   * @param encoding file encoding character set
   * @return list of elements from report file
   */
  public static List<String> getElements(File file, String encoding) {

    var list = new ArrayList<String>();
    try ( var br = new BufferedReader(
      new InputStreamReader(java.nio.file.Files.newInputStream(file.toPath()), encoding))) {
      var sb = new StringBuilder(4096);
      String line;
      int cnt = 0;
      final Pattern whitespacesOnly = Pattern.compile("^\\s*$");

      while ((line = br.readLine()) != null) {
        if (cnt > (TOP_COUNT)) {
          if (whitespacesOnly.matcher(line).matches()) {
            list.add(sb.toString());
            sb.setLength(0);
          } else {
            sb.append(line);
            sb.append('\n');
          }
        }
        ++cnt;
      }

      if (sb.length() > 0) {
        list.add(sb.toString());
      }
    } catch (IOException e) {
      var msg = new StringBuilder(512).append("Cannot feed the data into sonar, details: '")
        .append(e)
        .append("'").toString();
      LOG.error(msg);
    }
    return list;
  }

  private static DrMemoryErrorType extractErrorType(String title) {
    String cleanedTitle = clean(title);
    for (var drMemoryErrorType : DrMemoryErrorType.values()) {
      if (cleanedTitle.startsWith(drMemoryErrorType.getTitle())) {
        return drMemoryErrorType;
      }
    }
    return DrMemoryErrorType.UNRECOGNIZED;
  }

  private static String clean(String title) {
    return title.trim();
  }

  public static class DrMemoryError {

    private DrMemoryErrorType type = DrMemoryErrorType.UNRECOGNIZED;
    private final List<Location> stackTrace = new ArrayList<>();
    private String message = "";

    public DrMemoryErrorType getType() {
      return type;
    }

    public void setType(DrMemoryErrorType type) {
      this.type = type;
    }

    public List<Location> getStackTrace() {
      return Collections.unmodifiableList(stackTrace);
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    @Override
    public String toString() {
      return "DrMemoryError [type=" + type + ", stackTrace=" + stackTrace + ", message=" + message + "]";
    }

    public static class Location {

      private String file = "";
      private Integer line = 0;

      public String getFile() {
        return file;
      }

      public void setFile(String file) {
        this.file = file;
      }

      public Integer getLine() {
        return line;
      }

      public void setLine(Integer line) {
        this.line = line;
      }

      @Override
      public String toString() {
        return "Location [file=" + file + ", line=" + line + "]";
      }
    }
  }

  /**
   * DrMemory supported error types
   *
   */
  public enum DrMemoryErrorType {
    UNADRESSABLE_ACCESS("UnadressableAccess", "UNADDRESSABLE ACCESS"),
    UNINITIALIZE_READ("UninitializedRead", "UNINITIALIZED READ"),
    INVALID_HEAP_ARGUMENT("InvalidHeapArgument", "INVALID HEAP ARGUMENT"),
    GDI_USAGE_ERROR("GdiUsageError", "GDI Usage Error"),
    HANDLE_LEAK("HandleLeak", "HANDLE LEAK"),
    WARNING("DrMemoryWarning", "WARNING"),
    POSSIBLE_LEAK("PossibleMemoryLeak", "POSSIBLE LEAK"),
    LEAK("MemoryLeak", "LEAK"),
    UNRECOGNIZED("Dr Memory unrecognized error", "");

    private final String id;
    private final String title;

    DrMemoryErrorType(String id, String title) {
      this.id = id;
      this.title = title;
    }

    public String getId() {
      return id;
    }

    public String getTitle() {
      return title;
    }
  }

}
