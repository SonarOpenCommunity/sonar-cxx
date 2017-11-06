/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.CxxUtils;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.StaxParser;

/**
 * Custom Rule Import, all static analysis are supported.
 *
 * @author jorge costa, stefan weiser
 */
public class CxxOtherSensor extends CxxReportSensor {
  private static final int MAX_STYLESHEETS = 10;
  private static final Logger LOG = Loggers.get(CxxOtherSensor.class);
  public static final String REPORT_PATH_KEY = "other.reportPath";
  public static final String KEY = "other";
  public static final String OTHER_XSLT_KEY = KEY + ".xslt.";
  public static final String STYLESHEET_KEY = ".stylesheet";
  public static final String INPUT_KEY = ".inputs";
  public static final String OUTPUT_KEY = ".outputs";

  /**
   * {@inheritDoc}
   */
  public CxxOtherSensor(CxxLanguage language, Settings settings) {
    super(language, settings);
  }

  @Override
  public String getReportPathKey() {
    return this.language.getPluginProperty(REPORT_PATH_KEY);
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(this.language.getKey()).name(language.getName() + " ExternalRulesSensor");
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

    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {

      /**
       * {@inheritDoc}
       */
      @Override
      public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
        rootCursor.advance();

        SMInputCursor errorCursor = rootCursor.childElementCursor("error");
        while (errorCursor.getNext() != null) {
          String file = errorCursor.getAttrValue("file");
          String line = errorCursor.getAttrValue("line");
          String id = errorCursor.getAttrValue("id");
          String msg = errorCursor.getAttrValue("msg");

          saveUniqueViolation(context, CxxOtherRepository.KEY, file, line, id, msg);
        }
      }
    });

    parser.parse(report);
  }

  @Override
  protected String getSensorKey() {
    return KEY;
  }  

  public void transformFiles(final File baseDir, SensorContext context) {
    boolean goOn = true;
    for (int i = 1; (i < MAX_STYLESHEETS) && goOn; i++) {
      String stylesheetKey = this.language.getPluginProperty(OTHER_XSLT_KEY + i + STYLESHEET_KEY);
      String inputKey = this.language.getPluginProperty(OTHER_XSLT_KEY + i + INPUT_KEY);
      String outputKey = this.language.getPluginProperty(OTHER_XSLT_KEY + i + OUTPUT_KEY);

      if (stylesheetKey==null) {
        LOG.error("'{}' is not defined.", OTHER_XSLT_KEY + i + STYLESHEET_KEY);
        break;
      }
      String stylesheet = resolveFilename(baseDir.getAbsolutePath(), context.settings().getString(stylesheetKey));


      List<File> inputs = getReports(context.settings(), baseDir, inputKey);
      String[] outputStrings = context.settings().getStringArray(outputKey);
      List<String> outputs = Arrays.asList((outputStrings != null) ? outputStrings : new String[] {});

      if (stylesheet == null && inputKey==null && outputKey==null) {
        goOn = false;
      } else {
        if (stylesheet == null) {
          LOG.error(stylesheetKey + " is not defined.");
          goOn = false;
        } else {
          goOn = checkInput(inputKey, outputKey, inputs, outputs); 
        }
      }

      if (goOn) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Converting " + stylesheet + " with " + inputs + " to " + outputs + ".");
        }
        File stylesheetFile = new File(stylesheet);
        if (stylesheetFile.isAbsolute()) {
          transformFileList(baseDir.getAbsolutePath(), stylesheetFile, inputs, outputs);
        }
      }
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
  private static boolean isValidOutput(String outputKey, @Nullable List<String> outputs) {
    if ((outputs == null) || (outputs.isEmpty())) {
      LOG.error(outputKey + " is not defined.");
      return false;
      }
    return true;
  }

  /**
   * @param inputKey
   * @param inputs
   */
  private static boolean isValidInput(String inputKey, @Nullable List<File> inputs) {
    if ((inputs == null) || (inputs.isEmpty())) {
      LOG.error(inputKey + " file is not defined.");
      return false;
      }
    return true;
  }

  private void transformFileList(final String baseDir, File stylesheetFile, List<File> inputs, List<String> outputs) {
    for (int j = 0; j < inputs.size(); j++) {
      try {
        String normalizedOutputFilename = resolveFilename(baseDir, outputs.get(j));
        CxxUtils.transformFile(new StreamSource(stylesheetFile), inputs.get(j), new File(normalizedOutputFilename));
      } catch (TransformerException e) {
        String msg = new StringBuilder()
          .append("Cannot transform report files: '")
          .append(e)
          .append("'")
          .toString();
        LOG.error(msg);
        CxxUtils.validateRecovery(e, this.language);
      }
    }
  }
}
