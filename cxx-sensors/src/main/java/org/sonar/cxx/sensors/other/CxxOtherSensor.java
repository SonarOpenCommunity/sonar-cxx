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
package org.sonar.cxx.sensors.other;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.CxxMetricsFactory;
import org.sonar.cxx.sensors.utils.CxxIssuesReportSensor;
import org.sonar.cxx.sensors.utils.CxxUtils;
import org.sonar.cxx.sensors.utils.StaxParser;
import org.sonar.cxx.utils.CxxReportIssue;

/**
 * Custom Rule Import, all static analysis are supported.
 *
 * @author jorge costa, stefan weiser
 */
public class CxxOtherSensor extends CxxIssuesReportSensor {

  private static final int MAX_STYLESHEETS = 10;
  private static final Logger LOG = Loggers.get(CxxOtherSensor.class);
  public static final String REPORT_PATH_KEY = "other.reportPath";
  public static final String OTHER_XSLT_KEY = "other.xslt.";
  public static final String STYLESHEET_KEY = ".stylesheet";
  public static final String INPUT_KEY = ".inputs";
  public static final String OUTPUT_KEY = ".outputs";

  /**
   * CxxOtherSensor for Other Sensor
   *
   * @param language defines settings C or C++
   */
  public CxxOtherSensor(CxxLanguage language) {
    super(language, REPORT_PATH_KEY, CxxOtherRepository.getRepositoryKey(language));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name(getLanguage().getName() + " ExternalRulesSensor")
      .onlyOnLanguage(getLanguage().getKey())
      .createIssuesForRuleRepository(getRuleRepositoryKey())
      .onlyWhenConfiguration(conf -> conf.hasKey(getReportPathKey()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(SensorContext context) {
    transformFiles(context.fileSystem().baseDir(), context);
    super.execute(context);
  }

  @Override
  public void processReport(final SensorContext context, File report) throws XMLStreamException, IOException,
    URISyntaxException, TransformerException {
    LOG.debug("Parsing 'other' format");

    StaxParser parser = new StaxParser((SMHierarchicCursor rootCursor) -> {
      rootCursor.advance();

      SMInputCursor errorCursor = rootCursor.childElementCursor("error");
      while (errorCursor.getNext() != null) {
        String file = errorCursor.getAttrValue("file");
        String line = errorCursor.getAttrValue("line");
        String id = errorCursor.getAttrValue("id");
        String msg = errorCursor.getAttrValue("msg");

        CxxReportIssue issue = new CxxReportIssue(id, file, line, msg);
        saveUniqueViolation(context, issue);
      }
    });

    parser.parse(report);
  }

  @Override
  protected CxxMetricsFactory.Key getMetricKey() {
    return CxxMetricsFactory.Key.OTHER_SENSOR_ISSUES_KEY;
  }

  public void transformFiles(final File baseDir, SensorContext context) {
    for (int i = 1; i < MAX_STYLESHEETS; i++) {
      String stylesheetKey = getLanguage().getPluginProperty(OTHER_XSLT_KEY + i + STYLESHEET_KEY);
      String inputKey = getLanguage().getPluginProperty(OTHER_XSLT_KEY + i + INPUT_KEY);
      String outputKey = getLanguage().getPluginProperty(OTHER_XSLT_KEY + i + OUTPUT_KEY);

      String stylesheet = stylesheetKey == null ? null : resolveFilename(baseDir.getAbsolutePath(), context.config().get(stylesheetKey).orElse(null));
      List<File> inputs = inputKey == null ? new ArrayList<>() : getReports(context.config(), baseDir, inputKey);
      String[] outputStrings = outputKey == null ? null : context.config().getStringArray(outputKey);
      List<String> outputs = outputStrings == null ? new ArrayList<>() : Arrays.asList(outputStrings);

      if ((stylesheet == null) && (inputs.isEmpty()) && (outputs.isEmpty())) {
        break; // no or last item
      }

      if (stylesheet == null) {
        LOG.error(stylesheetKey + " is not defined.");
        break;
      }

      if (!checkInput(inputKey, outputKey, inputs, outputs)) {
        break;
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("Converting " + stylesheet + " with " + inputs + " to " + outputs + ".");
      }

      transformFileList(baseDir.getAbsolutePath(), stylesheet, inputs, outputs);
    }
  }

  private static boolean checkInput(String inputKey, String outputKey, @Nullable List<File> inputs,
    @Nullable List<String> outputs) {
    return isValidInput(inputKey, inputs) && isValidOutput(outputKey, outputs) && hasCorrectSize(inputs, outputs);
  }

  /**
   * @param inputs
   * @param outputs
   * @return
   */
  private static boolean hasCorrectSize(List<File> inputs, List<String> outputs) {
    if (inputs.size() != outputs.size()) {
      LOG.error("Number of source XML files is not equal to the the number of output files.");
      return false;
    }
    return true;
  }

  /**
   * @param outputKey
   * @param outputs
   * @return
   */
  private static boolean isValidOutput(@Nullable String outputKey, @Nullable List<String> outputs) {
    if ((outputKey == null) || (outputs == null) || (outputs.isEmpty())) {
      if (outputKey != null) {
        LOG.error(outputKey + " file is not defined.");
      } else {
        LOG.error("outputKey is not defined.");
      }
      return false;
    }
    return true;
  }

  /**
   * @param inputKey
   * @param inputs
   */
  private static boolean isValidInput(@Nullable String inputKey, @Nullable List<File> inputs) {

    if ((inputKey == null) || (inputs == null) || (inputs.isEmpty())) {
      if (inputKey != null) {
        LOG.error(inputKey + " file is not defined.");
      } else {
        LOG.error("inputKey is not defined.");
      }
      return false;
    }

    return true;
  }

  private void transformFileList(final String baseDir, String stylesheet, List<File> inputs, List<String> outputs) {
    for (int j = 0; j < inputs.size(); j++) {
      try {
        String normalizedOutputFilename = resolveFilename(baseDir, outputs.get(j));
        CxxUtils.transformFile(new StreamSource(new File(stylesheet)), inputs.get(j), new File(normalizedOutputFilename));
      } catch (TransformerException e) {
        String msg = new StringBuilder()
          .append("Cannot transform report files: '")
          .append(e)
          .append("'")
          .toString();
        LOG.error(msg);
        CxxUtils.validateRecovery(e, getLanguage());
      }
    }
  }
}
