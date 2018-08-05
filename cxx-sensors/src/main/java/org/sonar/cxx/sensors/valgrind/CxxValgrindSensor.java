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
package org.sonar.cxx.sensors.valgrind;

import java.io.File;
import java.util.Set;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.CxxMetricsFactory;
import org.sonar.cxx.sensors.utils.CxxIssuesReportSensor;
import org.sonar.cxx.utils.CxxReportIssue;

/**
 * {@inheritDoc}
 */
public class CxxValgrindSensor extends CxxIssuesReportSensor {

  private static final Logger LOG = Loggers.get(CxxValgrindSensor.class);
  public static final String REPORT_PATH_KEY = "valgrind.reportPath";

  /**
   * CxxValgrindSensor for Valgrind Sensor
   *
   * @param language defines settings C or C++
   */
  public CxxValgrindSensor(CxxLanguage language) {
    super(language, REPORT_PATH_KEY, CxxValgrindRuleRepository.getRepositoryKey(language));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name(getLanguage().getName() + " ValgrindSensor")
      .onlyOnLanguage(getLanguage().getKey())
      .createIssuesForRuleRepository(getRuleRepositoryKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathKey()));
  }

  @Override
  protected void processReport(final SensorContext context, File report)
    throws javax.xml.stream.XMLStreamException {
    LOG.debug("Parsing 'Valgrind' format");
    ValgrindReportParser parser = new ValgrindReportParser();
    saveErrors(context, parser.processReport(report));
  }

  private static String createErrorMsg(ValgrindError error, ValgrindStack stack, int stackNr) {
    StringBuilder errorMsg = new StringBuilder();
    errorMsg.append(error.getText());
    if (error.getStacks().size() > 1) {
      errorMsg.append(" (Stack ").append(stackNr).append(")");
    }
    errorMsg.append("\n\n").append(stack);
    return errorMsg.toString();
  }

  private Boolean frameIsInProject(SensorContext context, ValgrindFrame frame) {
    return frame.isLocationKnown() && (getInputFileIfInProject(context, frame.getPath()) != null);
  }

  private CxxReportIssue createIssue(SensorContext context, ValgrindError error, ValgrindStack stack, int stackNr) {
    ValgrindFrame lastOwnFrame = stack.getLastOwnFrame(context.fileSystem().baseDir().getPath());
    if (lastOwnFrame == null) {
      LOG.warn("Cannot find a project file to assign the valgrind error '{}' to", error);
      return null;
    }

    String errorMsg = createErrorMsg(error, stack, stackNr);
    // set the last own frame as a primary location
    CxxReportIssue issue = new CxxReportIssue(error.getKind(), lastOwnFrame.getPath(),
        lastOwnFrame.getLine(), errorMsg);
    // add all frames as secondary locations
    for (ValgrindFrame frame : stack.getFrames()) {
      Boolean frameIsInProject = frameIsInProject(context, frame);
      String mappedPath = (frameIsInProject) ? frame.getPath() : lastOwnFrame.getPath();
      String mappedLine = (frameIsInProject) ? frame.getLine() : lastOwnFrame.getLine();
      issue.addLocation(mappedPath, mappedLine, frame.toString());
    }
    return issue;
  }

  void saveErrors(SensorContext context, Set<ValgrindError> valgrindErrors) {
    for (ValgrindError error : valgrindErrors) {
      int stackNr = 0;
      for (ValgrindStack stack : error.getStacks()) {
        CxxReportIssue issue = createIssue(context, error, stack, stackNr);
        if (issue != null) {
          saveUniqueViolation(context, issue);
        }
        ++stackNr;
      }
    }
  }

  @Override
  protected CxxMetricsFactory.Key getMetricKey() {
    return CxxMetricsFactory.Key.VALGRIND_SENSOR_KEY;
  }
}
