/*
 * C++ Community Plugin (cxx plugin)
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
package org.sonar.cxx.prejobs;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.FilenameUtils;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.scanner.sensor.ProjectSensor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.sensors.utils.CxxUtils;

@Phase(name = Phase.Name.PRE)
public class XlstSensor implements ProjectSensor {

  public static final String OTHER_XSLT_KEY = "sonar.cxx.xslt.";
  public static final String STYLESHEET_KEY = ".stylesheet";
  public static final String INPUT_KEY = ".inputs";
  public static final String OUTPUT_KEY = ".outputs";
  private static final String MISSING_VALUE = "XLST: '{}' value is not defined.";

  private static final Logger LOG = Loggers.get(XlstSensor.class);
  private static final int MAX_STYLESHEETS = 10;

  private SensorContext context;

  private static void transformFile(Source stylesheetFile, File input, File output) throws TransformerException {
    TransformerFactory factory = TransformerFactory.newInstance();
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    Transformer transformer = factory.newTransformer(stylesheetFile);
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(new StreamSource(input), new StreamResult(output));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("XSL transformation(s)")
      .onlyWhenConfiguration(conf -> conf.hasKey(OTHER_XSLT_KEY + 1 + STYLESHEET_KEY));
  }

  @Override
  public void execute(SensorContext context) {
    this.context = context;
    for (var i = 1; i <= MAX_STYLESHEETS; i++) {
      var paramError = false;

      final String stylesheetKey = OTHER_XSLT_KEY + i + STYLESHEET_KEY;
      final String inputKey = OTHER_XSLT_KEY + i + INPUT_KEY;
      final String outputKey = OTHER_XSLT_KEY + i + OUTPUT_KEY;

      if (!context.config().hasKey(stylesheetKey)
            && !context.config().hasKey(inputKey)
            && !context.config().hasKey(outputKey)) {
        break; // no or last item
      }

      final String stylesheet = context.config().get(stylesheetKey).orElse("");
      if (stylesheet.isEmpty()) {
        LOG.error(MISSING_VALUE, stylesheetKey);
        paramError = true;
      }

      final List<File> inputs = CxxUtils.getFiles(context, inputKey);
      if (inputs.isEmpty()) {
        LOG.error(MISSING_VALUE, inputKey);
        paramError = true;
      }

      final String outputs = context.config().get(outputKey).orElse("");
      if (outputs.isEmpty()) {
        LOG.error(MISSING_VALUE, outputKey);
        paramError = true;
      }

      if (paramError) {
        break;
      }

      LOG.debug("XLST: Converting '{}' with '{}' to '{}'.", inputs, stylesheet, outputs);
      transformFileList(context.fileSystem().baseDir().getAbsolutePath(), stylesheet, inputs, outputs);
    }
  }

  private void transformFileList(String baseDir, String stylesheet, List<File> inputs, String outputs) {
    for (var j = 0; j < inputs.size(); j++) {
      try {
        InputStream inputStream = this.getClass().getResourceAsStream("/xsl/" + stylesheet);
        Source stylesheetFile;
        if (inputStream != null) {
          stylesheetFile = new StreamSource(inputStream);
        } else {
          stylesheetFile = new StreamSource(new File(CxxUtils.resolveAntPath(baseDir, stylesheet)));
        }
        File inputFile = inputs.get(j);
        File outputFile = createOutputFile(inputFile.getPath(), outputs);
        transformFile(stylesheetFile, inputFile, outputFile);
      } catch (TransformerException | NullPointerException e) {
        CxxUtils.validateRecovery("Cannot XLS transform files", e, context.config());
      }
    }
  }

  private File createOutputFile(String path, String outputs) {
    if (outputs.contains("*")) {
      outputs = outputs.replace("*", FilenameUtils.getBaseName(path));
    }
    return new File(FilenameUtils.getFullPath(path), outputs);
  }

}
