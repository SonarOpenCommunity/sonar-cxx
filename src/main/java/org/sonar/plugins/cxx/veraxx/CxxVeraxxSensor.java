/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits réservés.
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

package org.sonar.plugins.cxx.veraxx;

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

public class CxxVeraxxSensor  extends ReportsHelper implements Sensor {

	  private RuleFinder ruleFinder;
	  
	  public CxxVeraxxSensor(RuleFinder ruleFinder)
	  {
	    this.ruleFinder   = ruleFinder;
	  }
	
	private static Logger logger = LoggerFactory
			.getLogger(CxxVeraxxSensor.class);

	
	public boolean shouldExecuteOnProject(Project project) {
		return CxxPlugin.KEY.equals(project.getLanguageKey());
	}
	

	public static final String GROUP_ID = "org.codehaus.mojo";
	public static final String ARTIFACT_ID = "cxx-maven-plugin";
	public static final String SENSOR_ID = "veraxx";
	public static final String DEFAULT_VERAXX_REPORTS_DIR = "vera++-reports";
	public static final String DEFAULT_REPORTS_FILE_PATTERN = "**/vera++-result-*.xml";

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
		return DEFAULT_VERAXX_REPORTS_DIR;
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
								collectFile(project, rootCursor.childElementCursor("file"), context);
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

	private void collectFile(Project project, SMInputCursor file,
			SensorContext context) throws ParseException, XMLStreamException {
		while (file.getNext() != null) {
			//logger.info("collectError nodename = {} {}", error.getPrefixedName(), error.getAttrCount());
			String fileName = file.getAttrValue("name");
	    	if (!StringUtils.isEmpty(fileName)) {
				SMInputCursor error = file.childElementCursor("error");
				while (error.getNext() != null) {
					
					String line = error.getAttrValue("line");
					String severity = error.getAttrValue("severity");
					String message = error.getAttrValue("message");
					String source = error.getAttrValue("source");
					
			    	if (!StringUtils.isEmpty(source)) {
						
						if (StringUtils.isEmpty(line)) line = "0";
		
				        CxxFile ressource = CxxFile.fromFileName(project, fileName, getReportsIncludeSourcePath(project), false);
				        if (fileExist(context, ressource)) {
					        Rule rule = ruleFinder.findByKey(CxxVeraxxRuleRepository.REPOSITORY_KEY, source);
					        if (rule != null)
					        {
					        	Object t[] = {source, message, line, ressource.getKey()};
					        	logger.debug("error source={} message={} found at line {} from ressource {}", t);
			
						        Violation violation = Violation.create(rule, ressource);
						        violation.setMessage(message);
						        violation.setLineId(Integer.parseInt(line));
						        context.saveViolation(violation);
					        } else {
					        	Object t[] = {source, message, line, fileName};
					        	logger.warn("No rule for error source={} message={} found at line {} from file {}", t);
					        }
				        }
				        else
				        {
				        	Object t[] = {source, message, line, fileName};
				        	logger.warn("error id={} msg={} found at line {} from file {} has no ressource associated", t);	
				        }
			    	} else {
			    		logger.warn("error no source for error");
			    	}			
				}
	    	} else {
	        	logger.warn("error no name in file node");
	        }	
		}
	}
	
	private boolean fileExist(SensorContext context, CxxFile file) {
		return context.getResource(file) != null;
	}
}
