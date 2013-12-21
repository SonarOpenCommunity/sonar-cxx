/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.veraxx;

import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;
import org.sonar.plugins.cxx.utils.EmptyReportException;

import java.io.File;
import java.util.List;
import java.util.Iterator;
/**
 * {@inheritDoc}
 */
public class CxxVeraxxSensor extends CxxReportSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.vera.reportPath";
  private static final String DEFAULT_REPORT_PATH = "vera++-reports/vera++-result-*.xml";
  private RulesProfile profile;

  /**
   * {@inheritDoc}
   */
  public CxxVeraxxSensor(RuleFinder ruleFinder, Settings conf, RulesProfile profile) {
    super(ruleFinder, conf);
    this.profile = profile;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return super.shouldExecuteOnProject(project)
      && !profile.getActiveRulesByRepository(CxxVeraxxRuleRepository.KEY).isEmpty();
  }

  @Override
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected String defaultReportPath() {
    return DEFAULT_REPORT_PATH;
  }

  @Override
  protected void processReport(final Project project, final SensorContext context, File report)
      throws javax.xml.stream.XMLStreamException
  {
    try {
      StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
        /**
         * {@inheritDoc}
         */
        public void stream(SMHierarchicCursor rootCursor) throws javax.xml.stream.XMLStreamException {
          try{
            rootCursor.advance();
          }
          catch(com.ctc.wstx.exc.WstxEOFException eofExc){
            throw new EmptyReportException();
          }
          int countIssues = 0;
          SMInputCursor fileCursor = rootCursor.childElementCursor("file");
          while (fileCursor.getNext() != null) {
            String name = fileCursor.getAttrValue("name");
            // create a full path
            String filename = getCaseSensitiveFileName(name, project.getFileSystem().getSourceDirs());
            CxxUtils.LOG.info("Vera++ processes file = " + filename);              
            SMInputCursor errorCursor = fileCursor.childElementCursor("error");
            while (errorCursor.getNext() != null) {
              if (!name.equals("error")) {
                String line = errorCursor.getAttrValue("line");
                String message = errorCursor.getAttrValue("message");
                String source = errorCursor.getAttrValue("source");

                saveViolation(project, context, CxxVeraxxRuleRepository.KEY,
                        filename, line, source, message);
                countIssues++;
              } else {
                CxxUtils.LOG.debug("Error in file '{}', with message '{}'",
                    errorCursor.getAttrValue("line"),
                    errorCursor.getAttrValue("message"));
              }
            }
          }
          CxxUtils.LOG.info("Vera++ issues processed = " + countIssues);    
        }
      });

      parser.parse(report);
    } catch (com.ctc.wstx.exc.WstxUnexpectedCharException e) {
      CxxUtils.LOG.error("Ignore XML error from Veraxx '{}'", e.toString());
    }
  }
  private static String getCaseSensitiveFileName(String file, List<java.io.File> sourceDirs) {
	    Iterator<java.io.File> iterator = sourceDirs.iterator();
	    while (iterator.hasNext()) {
	      File targetfile = new java.io.File(iterator.next().getPath() + "\\" + file);
	      if (targetfile.exists()) {
	        try {
	          return targetfile.getCanonicalFile().getAbsolutePath();
	        } catch (java.io.IOException e) {
	          CxxUtils.LOG.error("processReport GetRealFileName getName failed '{}'", e.toString());
	        }
	      }
	    }
	    return file;
	  } 
}
