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
package org.sonar.cxx.sensors.drmemory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
  public static final Pattern rx_message_finder = Pattern.compile("^Error #\\d+:(.*)");
  public static final Pattern rx_file_finder = Pattern.compile("^.*\\[(.*):(\\d+)\\]$");
  public static final int TOP_COUNT = 4;

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

    private String id;
    private String title;

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

  public static class DrMemoryError {

    public static class Location {

      private String file = "";
      private Integer line;

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

    private DrMemoryErrorType type = DrMemoryErrorType.UNRECOGNIZED;
    private List<Location> stackTrace = new ArrayList<>();
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
  }

  private DrMemoryParser() {
  }

  /**
   * DrMemory parser
   *
   * @param file with findings
   * @param charset file encoding character set
   * @return list of issues extracted from file
   */
  public static List<DrMemoryError> parse(File file, String charset) {

    List<DrMemoryError> result = new ArrayList<>();

    List<String> elements = getElements(file, charset);

    for (String element : elements) {
      Matcher m = rx_message_finder.matcher(element);

      if (m.find()) {
        DrMemoryError error = new DrMemoryError();
        error.type = extractErrorType(m.group(1));
        String[] elementSplitted = CxxUtils.EOLPattern.split(element);
        error.message = elementSplitted[0];
        for (String elementPart : elementSplitted) {
          Matcher locationMatcher = rx_file_finder.matcher(elementPart);
          if (locationMatcher.find()) {
            Location location = new Location();
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

  private static DrMemoryErrorType extractErrorType(String title) {
    String cleanedTitle = clean(title);
    for (DrMemoryErrorType drMemoryErrorType : DrMemoryErrorType.values()) {
      if (cleanedTitle.startsWith(drMemoryErrorType.getTitle())) {
        return drMemoryErrorType;
      }
    }
    return DrMemoryErrorType.UNRECOGNIZED;
  }

  private static String clean(String title) {
    return title.trim();
  }

  /**
   * get all DrMemory elements from file
   *
   * @param file with findings
   * @param charset file encoding character set
   * @return list of elements from report file
   */
  public static List<String> getElements(File file, String charset) {

    List<String> list = new ArrayList<>();
    try (InputStream input = java.nio.file.Files.newInputStream(file.toPath())) {
      BufferedReader br = new BufferedReader(new InputStreamReader(input, charset));
      StringBuilder sb = new StringBuilder();
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
        cnt++;
      }

      if (sb.length() > 0) {
        list.add(sb.toString());
      }

      br.close();
    } catch (IOException e) {
      String msg = new StringBuilder().append("Cannot feed the data into sonar, details: '")
        .append(e)
        .append("'").toString();
      LOG.error(msg);
    }
    return list;
  }
}
