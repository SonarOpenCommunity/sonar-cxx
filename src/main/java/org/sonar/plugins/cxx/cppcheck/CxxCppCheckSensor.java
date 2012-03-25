/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.cppcheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.input.TeeInputStream;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.SonarException;
import org.sonar.api.utils.StaxParser;
import org.sonar.api.utils.XmlParserException;
import org.sonar.plugins.cxx.CxxSensor;

/**
 * Sensor for CppCheck external tool.
 *
 * CppCheck is an equivalent to FindBug but for C++
 *
 * @author fbonin
 * @author vhardion
 * @todo enable include dirs (-I)
 * @todo allow configuration of path to analyze
 */
public class CxxCppCheckSensor extends CxxSensor {
  private static final String EXEC = "cppcheck";
  private static final String ARGS = "--enable=all -v --quiet --xml";
  private static Logger logger = LoggerFactory.getLogger(CxxCppCheckSensor.class);
  public static final String REPORT_PATH_KEY = "sonar.cxx.cppcheck.reportPath";
  private static final String DEFAULT_REPORT_PATH = "cppcheck-reports/cppcheck-result-*.xml";

  private RuleFinder ruleFinder = null;
  private Configuration conf = null;
  private boolean dynamicAnalysis = false;


  public CxxCppCheckSensor(RuleFinder ruleFinder, Configuration conf) {
    this.ruleFinder = ruleFinder;
    this.conf = conf;
    this.dynamicAnalysis = conf.getBoolean("sonar.cxx.cppcheck.runAnalysis", this.dynamicAnalysis);
  }


  public void analyse(Project project, SensorContext context) {
    try {
      if (dynamicAnalysis) {
        analyseDynamicly(project, context);
      } else {
        File[] reports = getReports(conf, project.getFileSystem().getBasedir().getPath(),
                                  REPORT_PATH_KEY, DEFAULT_REPORT_PATH);
        for (File report : reports) {
          parseReport(project, context, report);
        }
      }
    } catch (Exception e) {
      String msg = new StringBuilder()
        .append("Cannot feed the cppcheck data into sonar, details: '")
        .append(e)
        .append("'")
        .toString();
      throw new SonarException(msg, e);
    }
  }


  void analyseDynamicly(Project project, SensorContext context)
    throws javax.xml.stream.XMLStreamException
  {
    Process p;

    try {
      String cmd = EXEC + " " + ARGS + " ";
      // + project.getPom().getBuild().getSourceDirectory();
      // $FB project FileSystem content has been patched by CxxSourceImporter
      for (File file : project.getFileSystem().getSourceDirs()) {
        cmd += "\"" + file.getAbsolutePath() + "\" ";
      }
      logger.debug(cmd);
      p = Runtime.getRuntime().exec(cmd);
      p.waitFor();

      // Write result in local file
      File resultOutputFile = new File(project.getFileSystem().getSonarWorkingDirectory() + "/cppcheck-result.xml");
      // resultOutputFile.createNewFile();
      logger.debug("Output result to " + resultOutputFile.getAbsolutePath());

      // Becarefull ... CppCheck print its result into Error output !
      // TeeInputStream is used to read result from stream and both
      // write it to a file
      FileOutputStream fos = new FileOutputStream(resultOutputFile);
      TeeInputStream tis = new TeeInputStream(p.getErrorStream(), fos, true);
      parseReport(project, context, tis);

    } catch (InterruptedException ex) {
      logger.error("Analysis can't wait for the end of the process", ex);
    } catch (IOException ex) {
      logger.error("IO EXCEPTION", ex);
    }
  }


  private void parseReport(final Project project, final SensorContext context, File report)
    throws IOException, javax.xml.stream.XMLStreamException
  {
    logger.info("parsing cppcheck report '{}'", report);
    parseReport(project, context, new FileInputStream(report));
  }


  private void parseReport(final Project project, final SensorContext context, InputStream stream)
    throws IOException, javax.xml.stream.XMLStreamException
  {
    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
      public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
        rootCursor.advance(); //results

        SMInputCursor errorCursor = rootCursor.childElementCursor("error"); //error
        while (errorCursor.getNext() != null) {
          String file = errorCursor.getAttrValue("file");
          String line = errorCursor.getAttrValue("line");
          String id = errorCursor.getAttrValue("id");
          String severity = errorCursor.getAttrValue("severity");
          String msg = errorCursor.getAttrValue("msg");

          processError(project, context, file, Integer.parseInt(line), id, severity, msg);
        }
      }
    });

    parser.parse(stream);
  }


  void processError(Project project, SensorContext context, String file, int line, String ruleId,
                    String severity, String msg) {
    RuleQuery ruleQuery = RuleQuery.create()
      .withRepositoryKey(CxxCppCheckRuleRepository.KEY)
      .withConfigKey(ruleId);
    Rule rule = ruleFinder.find(ruleQuery);
    if (rule != null) {
      org.sonar.api.resources.File resource =
        org.sonar.api.resources.File.fromIOFile(new File(file), project);
      Violation violation = Violation.create(rule, resource).setLineId(line).setMessage(msg);
      context.saveViolation(violation);
    }
    else{
      logger.warn("Cannot find the rule {}-{}, skipping violation", CxxCppCheckRuleRepository.KEY, ruleId);
    }
  }
}
