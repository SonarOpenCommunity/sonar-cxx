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
package org.sonar.plugins.cxx.cppncss;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.RangeDistributionBuilder;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.StaxParser;
import org.sonar.api.utils.XmlParserException;
import org.sonar.plugins.cxx.CxxFile;
import org.sonar.plugins.cxx.CxxPlugin;
import org.sonar.plugins.cxx.utils.ReportsHelper;

public class CxxCppNcssSensor extends ReportsHelper implements Sensor {

	private static Logger logger = LoggerFactory
			.getLogger(CxxCppNcssSensor.class);

	public boolean shouldExecuteOnProject(Project project) {
		return CxxPlugin.KEY.equals(project.getLanguageKey());
	}

	public static final String GROUP_ID = "org.codehaus.mojo";
	public static final String ARTIFACT_ID = "cxx-maven-plugin";
	public static final String SENSOR_ID = "cppncss";
	public static final String DEFAULT_GCOVR_REPORTS_DIR = "cppncss-reports";
	public static final String DEFAULT_REPORTS_FILE_PATTERN = "**/cppncss-result-*.xml";

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
		return DEFAULT_GCOVR_REPORTS_DIR;
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

	private final static Number[] METHODS_DISTRIB_BOTTOM_LIMITS = { 1, 2, 4, 6,
			8, 10, 12 };
	private final static Number[] FILE_DISTRIB_BOTTOM_LIMITS = { 0, 5, 10, 20,
			30, 60, 90 };
	private final static Number[] CLASS_DISTRIB_BOTTOM_LIMITS = { 0, 5, 10, 20,
			30, 60, 90 };
	
	private class FileData {
		FileData(String name) {
			FileName = name;
		}
		
		private class classData
		{
			private Map<String,Integer> ComplexityPerMethod = new HashMap<String, Integer>();
			private Integer ClassComplexity = new Integer(0);
			private Integer NbClassMethod = new Integer(0);
			
			private void putComplexityPerMethod(String name, Integer Complexity)
			{
				NbClassMethod++;
				ClassComplexity += Complexity;
				Integer c = ComplexityPerMethod.get(name);
				if (c == null) {
					c = new Integer(Complexity);
					ComplexityPerMethod.put(name, c);
				} else {
					// alert !
				}
			}

			public Integer getClassComplexity() {
				return ClassComplexity;
			}

			public Collection<Integer> complexityPerMethodValues() {
				return ComplexityPerMethod.values();
			}
		}

		public void addMethod(String clazz, String name, Integer Complexity) {
			NbMethod++;
			FileComplexity += Complexity;
			
			classData ComplexityPerMethod = ComplexityPerClassPerMethod.get(clazz);
			if (ComplexityPerMethod == null) {
				ComplexityPerMethod = new classData();
				ComplexityPerClassPerMethod.put(clazz, ComplexityPerMethod);
			}
			ComplexityPerMethod.putComplexityPerMethod(name, Complexity);
		}

		public void saveMetric(Project project, SensorContext context) {
			CxxFile file = CxxFile.fromFileName(project, FileName,
					getReportsIncludeSourcePath(project), false);
			if (context.getResource(file) != null) {
				
				RangeDistributionBuilder complexityMethodsDistribution = new RangeDistributionBuilder(
						CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION,
						METHODS_DISTRIB_BOTTOM_LIMITS);
				RangeDistributionBuilder complexityFileDistribution = new RangeDistributionBuilder(
						CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION,
						FILE_DISTRIB_BOTTOM_LIMITS);
				RangeDistributionBuilder complexityClassDistribution = new RangeDistributionBuilder(
						CoreMetrics.CLASS_COMPLEXITY_DISTRIBUTION,
						CLASS_DISTRIB_BOTTOM_LIMITS);

				complexityFileDistribution.add(FileComplexity);
				for (classData cd : ComplexityPerClassPerMethod.values()) {
					complexityClassDistribution.add(cd.getClassComplexity());
					for (Integer c : cd.complexityPerMethodValues()) {
						complexityMethodsDistribution.add(c);
					}
				}
				context.saveMeasure(file, CoreMetrics.FUNCTIONS, NbMethod
						.doubleValue());
				context.saveMeasure(file, CoreMetrics.COMPLEXITY,
						FileComplexity.doubleValue());
				//logger.info("File saveMeasure NbMethod={}, FileComplexity={}", NbMethod, FileComplexity);
				
				context.saveMeasure(file, complexityMethodsDistribution.build()
						.setPersistenceMode(PersistenceMode.MEMORY));
				//logger.info("File complexityMethodsDistribution={}", complexityMethodsDistribution.build());
				context.saveMeasure(file, complexityClassDistribution.build()
						.setPersistenceMode(PersistenceMode.MEMORY));
				//logger.info("File complexityClassDistribution={}", complexityClassDistribution.build());
				context.saveMeasure(file, complexityFileDistribution.build()
						.setPersistenceMode(PersistenceMode.MEMORY));
				//logger.info("File complexityFileDistribution={}", complexityFileDistribution.build());
			}
		}

		private String FileName;
		private Integer NbMethod = new Integer(0);
		private Map<String, classData> ComplexityPerClassPerMethod = new HashMap<String, classData>();
		private Integer FileComplexity = new Integer(0);
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
								Map<String, FileData> fileDataPerFilename = new HashMap<String, FileData>();
								rootCursor.advance();
								collectMeasure(project, rootCursor
										.childElementCursor("measure"),
										fileDataPerFilename, context);
								for (FileData d : fileDataPerFilename.values()) {
									d.saveMetric(project, context);
								}
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

	private void collectMeasure(Project project, SMInputCursor mesure,
			Map<String, FileData> fileDataPerFilename, SensorContext context)
			throws ParseException, XMLStreamException {
		while (mesure.getNext() != null) {
			String type = mesure.getAttrValue("type");
			logger.debug("collect Mesure type = {}",type);
			if (!StringUtils.isEmpty(type)) {

				SMInputCursor mesureChild = mesure.childElementCursor().advance();
				
				// collect labels	
				List<String> valueLabels = new ArrayList<String>();
				if (mesureChild.getLocalName().equalsIgnoreCase("labels"))
				{
					logger.debug("collect labels");
					SMInputCursor itemValueLabels = mesureChild.childElementCursor("label");
					while (itemValueLabels.getNext() != null) {
						String label = itemValueLabels.getElemStringValue();
						logger.debug("new label = {}", label);
						valueLabels.add(label);
					}
				}
				
				// collect only function metrics
				if (type.equalsIgnoreCase("Function")) {
					collectFunctionItems(project, fileDataPerFilename,
							valueLabels, mesureChild, context);
				} else if (type.equalsIgnoreCase("File")) {
					// nothing
				}
			}
		}
	}

	private void collectFunctionItems(Project project,
		Map<String, FileData> fileDataPerFilename,
		List<String> valueLabels, SMInputCursor items, SensorContext context)
		throws ParseException, XMLStreamException {
	
		while (items.getNext() != null) {
			
			String name = items.getAttrValue("name");
			//logger.info("collect Funtion name = {}", name);
			if (!StringUtils.isEmpty(name)) {
				String loc[] = name.split(" at ");
				String fullFuncName = loc[0];
				String fullFileName = loc[1];
				
				loc = fullFuncName.split("::");
				String className = (loc.length > 1)?loc[0]:"GLOBAL";
				String funcName = (loc.length > 1)?loc[1]:loc[0];
				loc = fullFileName.split(":");
				String fileName = loc[0];
				
				Object tab[] = {fileName, className, funcName};
				logger.debug("collect Funtion : fileName = {}, className = {}, funcName = {}", tab);
				FileData data = fileDataPerFilename.get(fileName);
				if (data == null) {
					data = new FileData(fileName);
					fileDataPerFilename.put(fileName, data);
				}
			
				SMInputCursor value = items.childElementCursor("value");
				int i = 0;
				while (value.getNext() != null) {
					if (valueLabels.get(i).equalsIgnoreCase("CCN")) {
						String MethodeComplexity = value.getElemStringValue();
						if (!StringUtils.isEmpty(MethodeComplexity)) {
							//logger.info("Found Funtion CCN = {}", MethodeComplexity);
							data.addMethod(className, funcName, Integer
									.parseInt(MethodeComplexity.trim()));
						}
					}
					i++;
				}
			}
		}
	}
}
