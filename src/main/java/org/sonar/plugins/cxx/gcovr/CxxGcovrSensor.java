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

package org.sonar.plugins.cxx.gcovr;


import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.AbstractCoverageExtension;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.PropertiesBuilder;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.StaxParser;
import org.sonar.api.utils.XmlParserException;
import org.sonar.plugins.cxx.CxxFile;
import org.sonar.plugins.cxx.CxxPlugin;
import org.sonar.plugins.cxx.utils.ReportsHelper;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import static java.util.Locale.ENGLISH;
import static org.sonar.api.utils.ParsingUtils.parseNumber;
import static org.sonar.api.utils.ParsingUtils.scaleValue;

/**
 * TODO copied from sonar-cobertura-plugin with modifications: JavaFile replaced
 * by C++, fixed SONARPLUGINS-696 C++ collectFileMeasures use Project to locate
 * C++ File getReports use FileSetManager for smarter report select using new
 * now plugin configuration use Fileset (ex ** /coverage.xml)
 */
public class CxxGcovrSensor extends AbstractCoverageExtension implements Sensor {

	private static Logger logger = LoggerFactory
	.getLogger(CxxGcovrSensor.class);

	@Override
	  public boolean shouldExecuteOnProject(Project project) {
	    return super.shouldExecuteOnProject(project) && CxxPlugin.KEY.equals(project.getLanguageKey());
	  }
	

	public static final String GROUP_ID = "org.codehaus.sonar.plugins";
	public static final String ARTIFACT_ID = "sonar-cxx-plugin.gcovr";
	public static final String DEFAULT_GCOVR_REPORTS_DIR = "gcovr-reports";
	public static final String DEFAULT_REPORTS_FILE_PATTERN = "**/coverage.xml";

	private class GcovrReportsHelper extends ReportsHelper
	{
		@Override
		protected String getARTIFACT_ID() {
			return ARTIFACT_ID;
		}
	
		@Override
		protected String getDEFAULT_REPORTS_DIR() {
			return DEFAULT_GCOVR_REPORTS_DIR;
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
	}
	GcovrReportsHelper reportHelper = new GcovrReportsHelper();
	
	public void analyse(Project project, SensorContext context) {
		File reportDirectory = reportHelper.getReportsDirectory(project);
		if (reportDirectory != null) {
			File reports[] = reportHelper.getReports(project, reportDirectory);
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
								collectPackageMeasures(project, rootCursor
										.descendantElementCursor("package"),
										context);
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

	private void collectPackageMeasures(Project project, SMInputCursor pack,
			SensorContext context) throws ParseException, XMLStreamException {
		//logger.info("collectPackageMeasures");
		while (pack.getNext() != null) {
			Map<String, FileData> fileDataPerFilename = new HashMap<String, FileData>();
			collectFileMeasures(project, pack.descendantElementCursor("class"),
					fileDataPerFilename);
			for (FileData cci : fileDataPerFilename.values()) {
				//logger.info("collectPackageMeasures fileKeyExist? {}", cci.getFile().getKey());
				if (fileExist(context, cci.getFile())) {
					logger.info("collectPackageMeasures file Exist {}", cci.getFile().getKey());
					for (Measure measure : cci.getMeasures()) {
						//logger.info("collectPackageMeasures mesure value = {}", measure.toString());
						context.saveMeasure(cci.getFile(), measure);
					}
				}
				else
				{
					logger.info("collectPackageMeasures file DOES NOT Exist {}", cci.getFile().getKey());					
				}
			}
		}
	}

	private boolean fileExist(SensorContext context, CxxFile file) {
		return context.getResource(file) != null;
	}

	private void collectFileMeasures(Project project, SMInputCursor clazz,
			Map<String, FileData> dataPerFilename) throws ParseException,
			XMLStreamException {
		//logger.info("collectFileMeasures");
		while (clazz.getNext() != null) {
			String fileName = clazz.getAttrValue("filename");
			FileData data = dataPerFilename.get(fileName);//javaStyleName);
			if (data == null) {
				data = new FileData(CxxFile.fromFileName(project, fileName, false));
				dataPerFilename.put(fileName/*javaStyleName*/, data);
			}
			//logger.info("collectFileMeasures created CXXFILe", data.getFile().getName());
			collectFileData(clazz, data);
		}
	}

	private void collectFileData(SMInputCursor clazz, FileData data)
			throws ParseException, XMLStreamException {

		//logger.info("collectFileData");
		SMInputCursor line = clazz.childElementCursor("lines").advance()
				.childElementCursor("line");
		while (line.getNext() != null) {
			String lineId = line.getAttrValue("number");
			data.addLine(lineId, (int) parseNumber(line.getAttrValue("hits"),
					ENGLISH));

			String isBranch = line.getAttrValue("branch");
			String text = line.getAttrValue("condition-coverage");
			if (StringUtils.equals(isBranch, "true")
					&& StringUtils.isNotBlank(text)) {
				String[] conditions = StringUtils.split(StringUtils
						.substringBetween(text, "(", ")"), "/");
				data.addConditionLine(lineId, Integer.parseInt(conditions[0]),
						Integer.parseInt(conditions[1]), StringUtils
								.substringBefore(text, " "));
			}
		}
	}

	private class FileData {

		private int lines = 0;
		private int conditions = 0;
		private int coveredLines = 0;
		private int coveredConditions = 0;

		private CxxFile file;
		private PropertiesBuilder<String, Integer> lineHitsBuilder = new PropertiesBuilder<String, Integer>(
				CoreMetrics.COVERAGE_LINE_HITS_DATA);
		private PropertiesBuilder<String, String> branchHitsBuilder = new PropertiesBuilder<String, String>(
				CoreMetrics.BRANCH_COVERAGE_HITS_DATA);

		public void addLine(String lineId, int lineHits) {
			lines++;
			if (lineHits > 0) {
				coveredLines++;
			}
			lineHitsBuilder.add(lineId, lineHits);
		}

		public void addConditionLine(String lineId, int coveredConditions,
				int conditions, String label) {
			this.conditions += conditions;
			this.coveredConditions += coveredConditions;
			branchHitsBuilder.add(lineId, label);
		}

		public FileData(CxxFile file) {
			this.file = file;
		}

		public List<Measure> getMeasures() {
			List<Measure> measures = new ArrayList<Measure>();
			if (lines > 0) {
/* sonar comput this by itself
 				measures.add(new Measure(CoreMetrics.COVERAGE,
						calculateCoverage(coveredLines + coveredConditions,
								lines + conditions)));

				measures.add(new Measure(CoreMetrics.LINE_COVERAGE,
						calculateCoverage(coveredLines, lines)));*/
				measures.add(new Measure(CoreMetrics.LINES_TO_COVER,
						(double) lines));
				measures.add(new Measure(CoreMetrics.UNCOVERED_LINES,
						(double) lines - coveredLines));
				measures.add(lineHitsBuilder.build().setPersistenceMode(
						PersistenceMode.DATABASE));

				if (conditions > 0) {
/* sonar comput this by itself
 				measures.add(new Measure(CoreMetrics.BRANCH_COVERAGE,
							calculateCoverage(coveredConditions, conditions)));*/
					measures.add(new Measure(CoreMetrics.CONDITIONS_TO_COVER,
							(double) conditions));
					measures.add(new Measure(CoreMetrics.UNCOVERED_CONDITIONS,
							(double) conditions - coveredConditions));
					measures.add(branchHitsBuilder.build().setPersistenceMode(
							PersistenceMode.DATABASE));
				}
			}
			return measures;
		}

		public CxxFile getFile() {
			return file;
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private double calculateCoverage(int coveredElements, int elements) {
		if (elements > 0) {
			return scaleValue(100.0 * ((double) coveredElements / (double) elements));
		}
		return 0.0;
	}

}
