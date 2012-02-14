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
package org.sonar.plugins.cxx.gcovr;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.project.MavenProject;
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
import org.sonar.api.batch.SupportedEnvironment;

/**
 * TODO copied from sonar-cobertura-plugin with modifications: JavaFile replaced by C++, fixed SONARPLUGINS-696 C++ collectFileMeasures use
 * Project to locate C++ File getReports use FileSetManager for smarter report select using new now plugin configuration use Fileset (ex **
 * /coverage.xml)
 */

@SupportedEnvironment({ "maven" })
public class CxxGcovrSensor extends AbstractCoverageExtension implements Sensor {

  private static final String GROUP_ID = "org.codehaus.mojo";
  private static final String ARTIFACT_ID = "cxx-maven-plugin";
  private static final String SENSOR_ID = "gcovr";
  private static final String DEFAULT_GCOVR_REPORTS_DIR = "gcovr-reports";
  private static final String DEFAULT_REPORTS_FILE_PATTERN = "**/gcovr-result-*.xml";

  private MavenProject mavenProject = null;

  public CxxGcovrSensor(Project p) {
    mavenProject = p.getPom();
  }

  public CxxGcovrSensor(Project p, MavenProject mp) {
    mavenProject = mp;
  }

  private static Logger logger = LoggerFactory.getLogger(CxxGcovrSensor.class);

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return super.shouldExecuteOnProject(project) && CxxPlugin.KEY.equals(project.getLanguageKey());
  }

  private class GcovrReportsHelper extends ReportsHelper {

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
  }

  GcovrReportsHelper reportHelper = new GcovrReportsHelper();

  public void analyse(Project project, SensorContext context) {
    File reportDirectory = reportHelper.getReportsDirectory(project, mavenProject);
    if (reportDirectory != null) {
      File reports[] = reportHelper.getReports(mavenProject, reportDirectory);
      Map<String, FileData> fileDataPerFilename = new HashMap<String, FileData>();
      for (File report : reports) {
        parseReport(project, report, context, fileDataPerFilename);
      }
      for (FileData cci : fileDataPerFilename.values()) {
        logger.debug("collectPackageMeasures fileKeyExist? {}", cci.getFile().getKey());
        if (fileExist(context, cci.getFile())) {
          logger.debug("collectPackageMeasures file Exist {}", cci.getFile().getKey());
          for (Measure measure : cci.getMeasures()) {
            logger.debug("collectPackageMeasures mesure value = {}", measure.toString());
            context.saveMeasure(cci.getFile(), measure);
          }
        } else {
          logger.warn("collectPackageMeasures file {} IS NOT inventoried ", cci.getFile().getKey());
        }
      }
    }
  }

  private void parseReport(final Project project, File xmlFile, final SensorContext context, final Map<String, FileData> dataPerFilename) {
    try {
      logger.info("parsing {}", xmlFile);
      StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {

        public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
          try {
            rootCursor.advance();
            collectPackageMeasures(project, rootCursor.descendantElementCursor("package"), context, dataPerFilename);
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

  private void collectPackageMeasures(Project project, SMInputCursor pack, SensorContext context, Map<String, FileData> dataPerFilename)
      throws ParseException, XMLStreamException {
    logger.debug("collectPackageMeasures");
    while (pack.getNext() != null) {
      collectFileMeasures(project, pack.descendantElementCursor("class"), dataPerFilename);
    }
  }

  private boolean fileExist(SensorContext context, CxxFile file) {
    return context.getResource(file) != null;
  }

  private void collectFileMeasures(Project project, SMInputCursor clazz, Map<String, FileData> dataPerFilename) throws ParseException,
      XMLStreamException {
    logger.debug("collectFileMeasures");
    while (clazz.getNext() != null) {
      String fileName = clazz.getAttrValue("filename");
      CxxFile cxxfile = CxxFile.fromFileName(project, fileName, false);
      String FileKey = cxxfile.getKey();
      FileData data = dataPerFilename.get(FileKey);
      if (data == null) {
        data = new FileData(cxxfile);
        dataPerFilename.put(FileKey, data);
        logger.debug("collectFileMeasures created CXXFILe", data.getFile().getKey());
      }
      collectFileData(clazz, data);
    }
  }

  private void collectFileData(SMInputCursor clazz, FileData data) throws ParseException, XMLStreamException {

    logger.debug("collectFileData");
    SMInputCursor line = clazz.childElementCursor("lines").advance().childElementCursor("line");
    while (line.getNext() != null) {
      String lineId = line.getAttrValue("number");
      data.addLine(lineId, (int) parseNumber(line.getAttrValue("hits"), ENGLISH));

      String isBranch = line.getAttrValue("branch");
      String text = line.getAttrValue("condition-coverage");
      if (StringUtils.equals(isBranch, "true") && StringUtils.isNotBlank(text)) {
        String[] conditions = StringUtils.split(StringUtils.substringBetween(text, "(", ")"), "/");
        data.addConditionLine(lineId, Integer.parseInt(conditions[0]), Integer.parseInt(conditions[1]),
            StringUtils.substringBefore(text, " "));
      }
    }
  }

  private class FileData {

    private int lines = 0;
    private int conditions = 0;
    private int coveredLines = 0;
    private int coveredConditions = 0;

    private CxxFile file;
    private PropertiesBuilder<String, Integer> lineHitsBuilder = new PropertiesBuilder<String, Integer>(CoreMetrics.COVERAGE_LINE_HITS_DATA);
    private PropertiesBuilder<String, String> branchHitsBuilder = new PropertiesBuilder<String, String>(
        CoreMetrics.BRANCH_COVERAGE_HITS_DATA);

    public void addLine(String lineId, int lineHits) {
      lines++;
      if (lineHits > 0) {
        coveredLines++;
      }
      Map<String, Integer> props = lineHitsBuilder.getProps();
      if (props.containsKey(lineId)) {
        logger.debug("lineHitsBuilder find pre-existing line");
        props.put(lineId, props.get(lineId) + lineHits);
      } else {
        lineHitsBuilder.add(lineId, lineHits);
      }
    }

    public void addConditionLine(String lineId, int coveredConditions, int conditions, String label) {
      this.conditions += conditions;
      this.coveredConditions += coveredConditions;
      Map<String, String> props = branchHitsBuilder.getProps();
      if (props.containsKey(lineId)) {
        logger.debug("branchHitsBuilder find pre-existing line");
        props.put(lineId, props.get(lineId) + ", " + label);
      } else {
        branchHitsBuilder.add(lineId, label);
      }
    }

    public FileData(CxxFile file) {
      this.file = file;
    }

    public List<Measure> getMeasures() {
      List<Measure> measures = new ArrayList<Measure>();
      if (lines > 0) {
        measures.add(new Measure(CoreMetrics.LINES_TO_COVER, (double) lines));
        measures.add(new Measure(CoreMetrics.UNCOVERED_LINES, (double) lines - coveredLines));
        measures.add(lineHitsBuilder.build().setPersistenceMode(PersistenceMode.DATABASE));

        if (conditions > 0) {
          measures.add(new Measure(CoreMetrics.CONDITIONS_TO_COVER, (double) conditions));
          measures.add(new Measure(CoreMetrics.UNCOVERED_CONDITIONS, (double) conditions - coveredConditions));
          measures.add(branchHitsBuilder.build().setPersistenceMode(PersistenceMode.DATABASE));
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
}
