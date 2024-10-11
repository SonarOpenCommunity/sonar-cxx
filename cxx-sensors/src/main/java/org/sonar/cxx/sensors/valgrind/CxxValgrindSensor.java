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
package org.sonar.cxx.sensors.valgrind;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.xml.stream.XMLStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.cxx.sensors.utils.CxxIssuesReportSensor;
import org.sonar.cxx.sensors.utils.InvalidReportException;
import org.sonar.cxx.utils.CxxReportIssue;

/**
 * {@inheritDoc}
 */
public class CxxValgrindSensor extends CxxIssuesReportSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.valgrind.reportPaths";

  private static final Logger LOG = LoggerFactory.getLogger(CxxValgrindSensor.class);

  public static List<PropertyDefinition> properties() {
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(REPORT_PATH_KEY)
        .name("Valgrind Report(s)")
        .description("""
          Comma-separated paths (absolute or relative to the project base directory) to `*.xml` files with \
          `Valgrind` issues. Ant patterns are accepted for relative paths. In the SonarQube UI, \
          enter one entry per field.""")
        .category("CXX External Analyzers")
        .subCategory("Valgrind")
        .onQualifiers(Qualifiers.PROJECT)
        .multiValues(true)
        .build()
    ));
  }

  private static String createErrorMsg(ValgrindError error, ValgrindStack stack, int stackNr) {
    var errorMsg = new StringBuilder(512);
    errorMsg.append(error.getText());
    if (error.getStacks().size() > 1) {
      errorMsg.append(" (Stack ").append(stackNr).append(")");
    }
    errorMsg.append("\n\n").append(stack);
    return errorMsg.toString();
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("CXX Valgrind report import")
      .onlyOnLanguages("cxx", "cpp", "c++", "c")
      .createIssuesForRuleRepository(getRuleRepositoryKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(REPORT_PATH_KEY));
  }

  private boolean frameIsInProject(ValgrindFrame frame) {
    return frame.isLocationKnown() && (getInputFileIfInProject(frame.getPath()) != null);
  }

  @CheckForNull
  private CxxReportIssue createIssue(ValgrindError error, ValgrindStack stack, int stackNr) {
    ValgrindFrame lastOwnFrame = stack.getLastOwnFrame(context.fileSystem().baseDir().getPath());
    if (lastOwnFrame == null) {
      LOG.warn("Cannot find a project file to assign the valgrind error '{}' to", error);
      return null;
    }

    String errorMsg = createErrorMsg(error, stack, stackNr);
    // set the last own frame as a primary location
    var issue = new CxxReportIssue(error.getKind(), lastOwnFrame.getPath(), lastOwnFrame.getLine(), null, errorMsg);
    // add all frames as secondary locations
    for (var frame : stack.getFrames()) {
      boolean frameIsInProject = frameIsInProject(frame);
      String mappedPath = (frameIsInProject) ? frame.getPath() : lastOwnFrame.getPath();
      String mappedLine = (frameIsInProject) ? frame.getLine() : lastOwnFrame.getLine();
      issue.addLocation(mappedPath, mappedLine, null, frame.toString());
    }
    return issue;
  }

  @Override
  protected void processReport(File report) {
    try {
      var parser = new ValgrindReportParser();
      saveErrors(parser.parse(report));
    } catch (XMLStreamException e) {
      throw new InvalidReportException("The 'Valgrind' report is invalid", e);
    }
  }

  @Override
  protected String getReportPathsKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected String getRuleRepositoryKey() {
    return CxxValgrindRuleRepository.KEY;
  }

  void saveErrors(Set<ValgrindError> valgrindErrors) {
    for (var error : valgrindErrors) {
      var stackNr = 0;
      for (var stack : error.getStacks()) {
        CxxReportIssue issue = createIssue(error, stack, stackNr);
        if (issue != null) {
          saveUniqueViolation(issue);
        }
        ++stackNr;
      }
    }
  }

}
