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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.project.MavenProject;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.SupportedEnvironment;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.StaxParser;
import org.sonar.api.utils.XmlParserException;
import org.sonar.plugins.cxx.CxxFile;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.utils.ReportsHelper;

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
@SupportedEnvironment({ "maven" })
public class CxxCppCheckSensor extends ReportsHelper implements Sensor {

  private static final String EXEC = "cppcheck";
  private static final String ARGS = "--enable=all -v --quiet --xml";

  private static final String GROUP_ID = "org.codehaus.mojo";
  private static final String ARTIFACT_ID = "cxx-maven-plugin";
  private static final String SENSOR_ID = "cppcheck";
  private static final String DEFAULT_CPPCHECK_REPORTS_DIR = "cppcheck-reports";
  private static final String DEFAULT_REPORTS_FILE_PATTERN = "**/cppcheck-result-*.xml";

  private RuleFinder ruleFinder = null;
  private boolean dynamicAnalysis = false;
  private MavenProject mavenProject = null;

  public CxxCppCheckSensor(RuleFinder ruleFinder, Configuration conf, Project p) {
    this.ruleFinder = ruleFinder;
    this.dynamicAnalysis = conf.getBoolean("sonar.cxx.cppcheck.runAnalysis", this.dynamicAnalysis);
    mavenProject = p.getPom();
  }

  public CxxCppCheckSensor(RuleFinder ruleFinder, Configuration conf, Project p, MavenProject mp) {
    this.ruleFinder = ruleFinder;
    this.dynamicAnalysis = conf.getBoolean("sonar.cxx.cppcheck.runAnalysis", this.dynamicAnalysis);
    mavenProject = mp;
  }

  private static Logger logger = LoggerFactory.getLogger(CxxCppCheckSensor.class);

  /**
   * This plugin should be executed on C++ project
   * 
   * @param project
   * @return
   */
  public boolean shouldExecuteOnProject(Project project) {
    return CxxLanguage.KEY.equals(project.getLanguageKey());
  }

  @Override
  protected String getArtifactId() {
    return ARTIFACT_ID;
  }

  @Override
  protected String getSensorId() {
    return SENSOR_ID;
  }

  @Override
  protected String getDefaultReportsDir() {
    return DEFAULT_CPPCHECK_REPORTS_DIR;
  }

  @Override
  protected String getDefaultReportsFilePattern() {
    return DEFAULT_REPORTS_FILE_PATTERN;
  }

  @Override
  protected String getGroupId() {
    return GROUP_ID;
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }

  public void analyse(Project project, SensorContext context) {
    if (dynamicAnalysis) {
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
        parseReport(project, tis, context);

      } catch (InterruptedException ex) {
        logger.error("Analysis can't wait for the end of the process", ex);
      } catch (IOException ex) {
        logger.error("IO EXCEPTION", ex);
      }

    } else {
      File reportDirectory = getReportsDirectory(project, mavenProject);
      if (reportDirectory != null) {
        File reports[] = getReports(mavenProject, reportDirectory);
        for (File report : reports) {
          parseReport(project, report, context);
        }
      }
    }
  }

  private void parseReport(final Project project, File xmlFile, final SensorContext context) {
    try {
      parseReport(project, new FileInputStream(xmlFile), context);
    } catch (FileNotFoundException ex) {
      logger.error("CppCheck Report not found : " + xmlFile.getAbsoluteFile(), ex);
    }
  }

  /**
   * Parse the stream of CppCheck XML report
   * 
   * @param project
   * @param xmlStream
   *          - This stream will be closed at the end of this method
   * @param context
   */
  private void parseReport(final Project project, InputStream xmlStream, final SensorContext context) {
    try {
      logger.info("parsing CppCheck XML stream{}");
      StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {

        public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
          try {
            rootCursor.advance();
            collectError(project, rootCursor.childElementCursor("error"), context);
          } catch (ParseException e) {
            throw new XMLStreamException(e);
          }
        }
      });
      parser.parse(xmlStream);
    } catch (XMLStreamException e) {
      throw new XmlParserException(e);
    } finally {
      try {
        if (xmlStream != null) {
          xmlStream.close();
        }
      } catch (IOException ex) {
        logger.error("Can't close the xml stream", ex);
      }
    }
  }

  private void collectError(Project project, SMInputCursor error, SensorContext context) throws ParseException, XMLStreamException {
    while (error.getNext() != null) {
      // logger.info("collectError nodename = {} {}",
      // error.getPrefixedName(), error.getAttrCount());

      String id = error.getAttrValue("id");
      String msg = error.getAttrValue("msg");
      String file = error.getAttrValue("file");
      String line = error.getAttrValue("line");
      if (StringUtils.isEmpty(line)) {
        line = "0";
      }
      if ( !StringUtils.isEmpty(file)) {
        CxxFile ressource = CxxFile.fromFileName(project, file, getReportsIncludeSourcePath(mavenProject), false);
        if (fileExist(context, ressource)) {
          Rule rule = ruleFinder.findByKey(CxxCppCheckRuleRepository.REPOSITORY_KEY, id);
          if (rule != null) {
            Object t[] = { id, msg, line, ressource.getKey() };
            logger.debug("error id={} msg={} found at line {} from ressource {}", t);

            Violation violation = Violation.create(rule, ressource);
            violation.setMessage(msg);
            violation.setLineId(Integer.parseInt(line));
            context.saveViolation(violation);
          } else {
            Object t[] = { id, msg, line, file };
            logger.warn("No rule for error id={} msg={} found at line {} from file {}", t);
          }
        } else {
          Object t[] = { id, msg, line, file };
          logger.warn("error id={} msg={} found at line {} from file {} has no ressource associated", t);
        }
      } else {
        Object t[] = { id, msg, line };
        logger.warn("error id={} msg={} found at line {} has no file associated", t);
      }
    }
  }

  private boolean fileExist(SensorContext context, CxxFile file) {
    return context.getResource(file) != null;
  }
}
