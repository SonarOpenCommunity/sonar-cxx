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
package org.sonar.cxx.sensors.infer;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.sensors.utils.EmptyReportException;
import org.sonar.cxx.sensors.utils.InvalidReportException;
import org.sonar.cxx.sensors.utils.ReportException;
import org.sonar.cxx.utils.CxxReportIssue;

/**
 * Parser for Infer reports
 *
 * @author begarco
 */
public class InferParser {

  private static final Logger LOG = Loggers.get(InferParser.class);

  private final CxxInferSensor sensor;

  public InferParser(CxxInferSensor sensor) {
    this.sensor = sensor;
  }

  public void parse(File report) throws ReportException {
    InferIssue[] inferIssues;

    try {
      try ( JsonReader reader = new JsonReader(new FileReader(report))) {
        inferIssues = new Gson().fromJson(reader, InferIssue[].class);
        if (inferIssues == null) {
          throw new EmptyReportException("The 'Infer JSON' report is empty");
        }
      }
    } catch (IOException e) {
      throw new InvalidReportException("The 'Infer JSON' report is invalid", e);
    }

    for (InferIssue issue : inferIssues) {
      LOG.debug("Read: {}", issue.toString());
      if (issue.getFile() != null) {
        CxxReportIssue cxxReportIssue = new CxxReportIssue(
          issue.getBugType(), issue.getFile(), String.valueOf(issue.getLine()), null, issue.getQualifier());
        sensor.saveUniqueViolation(cxxReportIssue);
      } else {
        LOG.debug("Invalid infer issue '{}', skipping", issue.toString());
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();

  }

  public static class InferIssue {

    @SerializedName("bug_type")
    private String bugType;
    private String qualifier;
    private long line;
    private String file;

    public String getBugType() {
      return bugType;
    }

    public void setBugType(String bugType) {
      this.bugType = bugType;
    }

    public String getQualifier() {
      return qualifier;
    }

    public void setQualifier(String qualifier) {
      this.qualifier = qualifier;
    }

    public long getLine() {
      return line;
    }

    public void setLine(long line) {
      this.line = line;
    }

    public String getFile() {
      return file;
    }

    public void setFile(String file) {
      this.file = file;
    }

    @Override
    public String toString() {
      return String.format("InferIssue [bugType=%s, file=%s, line=%d, qualifier=%s]",
                           getBugType(), getFile(), getLine(), getQualifier());
    }
  }

}
