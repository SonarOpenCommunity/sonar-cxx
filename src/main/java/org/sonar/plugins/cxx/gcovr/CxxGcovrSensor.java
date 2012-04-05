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

import static java.util.Locale.ENGLISH;
import static org.sonar.api.utils.ParsingUtils.parseNumber;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.PropertiesBuilder;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.StaxParser;
import org.sonar.api.utils.XmlParserException;
import org.sonar.plugins.cxx.CxxSensor;

/**
 * {@inheritDoc}
 */
public class CxxGcovrSensor extends CxxSensor {
  //
  // This is a modified copy of sonar-cobertura-plugin.
  // This code is deemed to die. We can make it reuse the AbstractCoberturaParser as
  // soon as we can overwrite the code in AbstractCoberturaParser.sanitizeFilename()
  //
  
  public static final String REPORT_PATH_KEY = "sonar.cxx.gcovr.reportPath";
  private static final String DEFAULT_REPORT_PATH = "gcovr-reports/gcovr-result-*.xml";
  private static Logger logger = LoggerFactory.getLogger(CxxGcovrSensor.class);

  private Configuration conf = null;

  /**
   * {@inheritDoc}
   */
  public CxxGcovrSensor(Configuration conf) {
    this.conf = conf;
  }

  /**
   * {@inheritDoc}
   */
  public void analyse(Project project, SensorContext context) {
    List<File> reports = getReports(conf, project.getFileSystem().getBasedir().getPath(),
                                REPORT_PATH_KEY, DEFAULT_REPORT_PATH);

    Map<String, FileData> fileDataPerFilename = new HashMap<String, FileData>();
    for (File report : reports) {
      parseReport(project, report, fileDataPerFilename);
    }
    for (FileData cci : fileDataPerFilename.values()) {
      if (fileExist(context, cci.getFile())) {
        logger.debug("file exists: '{}'", cci.getFile().getKey());
        for (Measure measure : cci.getMeasures()) {
          logger.debug("saving measure: '{}'", measure.toString());
          context.saveMeasure(cci.getFile(), measure);
        }
      } else {
        logger.warn("file '{}' IS NOT inventoried ", cci.getFile().getKey());
      }
    }
  }
  
  private void parseReport(final Project project, File xmlFile, final Map<String, FileData> dataPerFilename) {
    try {
      logger.debug("parsing gcovr report '{}'", xmlFile);
      StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
        
        /**
         * {@inheritDoc}
         */
        public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
          try {
            rootCursor.advance();
            collectPackageMeasures(project, rootCursor.descendantElementCursor("package"), dataPerFilename);
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

  private void collectPackageMeasures(Project project, SMInputCursor pack, Map<String, FileData> dataPerFilename)
    throws ParseException, XMLStreamException
  {
    while (pack.getNext() != null) {
      collectFileMeasures(project, pack.descendantElementCursor("class"), dataPerFilename);
    }
  }

  private boolean fileExist(SensorContext context, org.sonar.api.resources.File file) {
    return context.getResource(file) != null;
  }

  private void collectFileMeasures(Project project, SMInputCursor clazz, Map<String, FileData> dataPerFilename)
    throws ParseException, XMLStreamException
  {
    while (clazz.getNext() != null) {
      String fileName = clazz.getAttrValue("filename");
      
      org.sonar.api.resources.File cxxfile =
        org.sonar.api.resources.File.fromIOFile(new File(fileName), project);
      if (cxxfile != null) {
        String fileKey = cxxfile.getKey();
        FileData data = dataPerFilename.get(fileKey);
        if (data == null) {
          data = new FileData(cxxfile);
          dataPerFilename.put(fileKey, data);
        }
        collectFileData(clazz, data);
      }
      else{
        //advance to next block without collecting
        logger.debug("could not get resource for '{}', skipping", fileName);
        SMInputCursor line = clazz.childElementCursor("lines").advance().childElementCursor("line");
        while (line.getNext() != null);
      }
    }
  }

  private void collectFileData(SMInputCursor clazz, FileData data) throws ParseException, XMLStreamException {
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

  private static class FileData {

    private int lines = 0;
    private int conditions = 0;
    private int coveredLines = 0;
    private int coveredConditions = 0;

    private org.sonar.api.resources.File file;
    private PropertiesBuilder<String, Integer> lineHitsBuilder =
      new PropertiesBuilder<String, Integer>(CoreMetrics.COVERAGE_LINE_HITS_DATA);
    private PropertiesBuilder<String, String> branchHitsBuilder =
      new PropertiesBuilder<String, String>(CoreMetrics.BRANCH_COVERAGE_HITS_DATA);

    public void addLine(String lineId, int lineHits) {
      lines++;
      if (lineHits > 0) {
        coveredLines++;
      }
      Map<String, Integer> props = lineHitsBuilder.getProps();
      if (props.containsKey(lineId)) {
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
        props.put(lineId, props.get(lineId) + ", " + label);
      } else {
        branchHitsBuilder.add(lineId, label);
      }
    }

    public FileData(org.sonar.api.resources.File file) {
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

    public org.sonar.api.resources.File getFile() {
      return file;
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
