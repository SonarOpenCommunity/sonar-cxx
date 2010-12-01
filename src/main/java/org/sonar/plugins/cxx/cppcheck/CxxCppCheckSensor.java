/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 Franck Bonin
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.cppcheck;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.StaxParser;
import org.sonar.api.utils.XmlParserException;
import org.sonar.plugins.cxx.CxxFile;
import org.sonar.plugins.cxx.CxxPlugin;
import org.sonar.plugins.cxx.rats.CxxRatsRuleRepository;
import org.sonar.plugins.cxx.utils.ReportsHelper;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.rules.Violation;

public class CxxCppCheckSensor  extends ReportsHelper implements Sensor {

	  private RuleFinder ruleFinder;
	  
	  public CxxCppCheckSensor(RuleFinder ruleFinder)
	  {
	    this.ruleFinder   = ruleFinder;
	  }
	
	private static Logger logger = LoggerFactory
			.getLogger(CxxCppCheckSensor.class);

	
	public boolean shouldExecuteOnProject(Project project) {
		return CxxPlugin.KEY.equals(project.getLanguageKey());
	}
	

	public static final String GROUP_ID = "org.codehaus.sonar.plugins";
	public static final String ARTIFACT_ID = "sonar-cxx-plugin.cppcheck";
	public static final String DEFAULT_CPPCHECK_REPORTS_DIR = "cppcheck-reports";
	public static final String DEFAULT_REPORTS_FILE_PATTERN = "**/cppcheck-result-*.xml";

	@Override
	protected String getARTIFACT_ID() {
		return ARTIFACT_ID;
	}

	@Override
	protected String getDEFAULT_REPORTS_DIR() {
		return DEFAULT_CPPCHECK_REPORTS_DIR;
	}

	@Override
	protected String getDEFAULT_REPORTS_FILE_PATTERN() {
		return DEFAULT_REPORTS_FILE_PATTERN;
	}

	@Override
	protected String getGROUP_ID() {
		return GROUP_ID;
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	public void analyse(Project project, SensorContext context) {
		File reportDirectory = getReportsDirectory(project);
		if (reportDirectory != null) {
			File reports[] = getReports(project, reportDirectory);
			for (File report : reports) {
				parseReport(project, report, context);
			}
		}
	}
	
	private void parseReport(final Project project, File xmlFile,
			final SensorContext context) {
		try {
			logger.info("parsing {}", xmlFile);
			StaxParser parser = new StaxParser(
					new StaxParser.XmlStreamHandler() {

						public void stream(SMHierarchicCursor rootCursor)
								throws XMLStreamException {
							try {
								rootCursor.advance();
								collectError(project, rootCursor.childElementCursor("error"), context);
							} catch (ParseException e) {
								throw new XMLStreamException(e);
							}
						}
					});
			parser.parse(xmlFile);
		} catch (XMLStreamException e) {
			throw new XmlParserException(e);
		}
	}

	private void collectError(Project project, SMInputCursor error,
			SensorContext context) throws ParseException, XMLStreamException {
		while (error.getNext() != null) {
			//logger.info("collectError nodename = {} {}", error.getPrefixedName(), error.getAttrCount());
			
			String id = error.getAttrValue("id");
			String msg = error.getAttrValue("msg");
			String file = error.getAttrValue("file");
			String line = error.getAttrValue("line");
			if (StringUtils.isEmpty(line)) line = "0";
	    	if (!StringUtils.isEmpty(file)) {
		        CxxFile ressource = CxxFile.fromFileName(project, file, getReportsIncludeSourcePath(project), false);
		        if (fileExist(context, ressource)) {
			        Rule rule = ruleFinder.findByKey(CxxCppCheckRuleRepository.REPOSITORY_KEY, id);
			        if (rule != null)
			        {
			        	Object t[] = {id, msg, line, ressource.getKey()};
			        	logger.info("error id={} msg={} found at line {} from ressource {}", t);
	
				        Violation violation = Violation.create(rule, ressource);
				        violation.setMessage(msg);
				        violation.setLineId(Integer.parseInt(line));
				        context.saveViolation(violation);
			        } else {
			        	Object t[] = {id, msg, line, file};
			        	logger.warn("No rule for error id={} msg={} found at line {} from file {}", t);
			        }
		        }
		        else
		        {
		        	Object t[] = {id, msg, line, file};
		        	logger.warn("error id={} msg={} found at line {} from file {} has no ressource associated", t);	
		        }
	    	} else {
	        	Object t[] = {id, msg, line};
	        	logger.warn("error id={} msg={} found at line {} has no file associated", t);
	        }		
		}
	}
	
	private boolean fileExist(SensorContext context, CxxFile file) {
		return context.getResource(file) != null;
	}
}
