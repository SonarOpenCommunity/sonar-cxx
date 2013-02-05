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
package org.sonar.plugins.cxx.cppncss;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.checks.AnnotationCheckFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.RangeDistributionBuilder;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.cxx.CxxPlugin;
import org.sonar.plugins.cxx.CxxSourceCode;
import org.sonar.plugins.cxx.checks.CxxChecksRepository;
import org.sonar.plugins.cxx.checks.CxxMaximumComplexity;
import org.sonar.plugins.cxx.utils.CxxOsValidator;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * {@inheritDoc}
 */
public class CxxCppNcssSensor extends CxxReportSensor {

  public static final String REPORT_PATH_KEY = "sonar.cxx.cppncss.reportPath";
  private static final String DEFAULT_REPORT_PATH = "cppncss-reports/cppncss-result-*.xml";
  private static final Number[] METHODS_DISTRIB_BOTTOM_LIMITS = { 1, 2, 4, 6, 8, 10, 12 };
  private static final Number[] FILE_DISTRIB_BOTTOM_LIMITS = { 0, 5, 10, 20, 30, 60, 90 };
  private static final Number[] CLASS_DISTRIB_BOTTOM_LIMITS = { 0, 5, 10, 20, 30, 60, 90 };
  private final AnnotationCheckFactory annotationCheckFactory;
  
  /**
   * {@inheritDoc}
   */
  public CxxCppNcssSensor(Settings conf, RulesProfile profile) {
    super(conf);
        this.annotationCheckFactory = AnnotationCheckFactory.create(profile, CxxChecksRepository.REPOSITORY_KEY, CxxChecksRepository.getChecks());
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
          throws javax.xml.stream.XMLStreamException {
    try {
      StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
        /**
         * {@inheritDoc}
         */
        public void stream(SMHierarchicCursor rootCursor) throws javax.xml.stream.XMLStreamException {
          Map<String, FileData> files = new HashMap<String, FileData>();
          rootCursor.advance(); //cppncss

          SMInputCursor measureCursor = rootCursor.childElementCursor("measure");
          while (measureCursor.getNext() != null) {
            collectMeasure(measureCursor, files);
          }

          for (FileData fileData : files.values()) {
            saveMetrics(project, context, fileData);
          }
        }
      });
      parser.parse(report);
    } catch (javax.xml.stream.XMLStreamException e) {
      CxxUtils.LOG.warn("Ignore XML stream exception for CppNccs '{}'", e.toString());
    }
  }

  private void collectMeasure(SMInputCursor measureCursor, Map<String, FileData> files)
          throws javax.xml.stream.XMLStreamException {
    // collect only function measures
    String type = measureCursor.getAttrValue("type");
    if (type.equalsIgnoreCase("function")) {
      collectFunctions(measureCursor, files);
    }
  }

  private void collectFunctions(SMInputCursor measureCursor, Map<String, FileData> files)
          throws javax.xml.stream.XMLStreamException {
    // determine the position of ccn measure using 'labels' analysis
    SMInputCursor childCursor = measureCursor.childElementCursor();
    int ccnIndex = indexOfCCN(childCursor.advance());

    // iterate over the function items and collect them
    while (childCursor.getNext() != null) {
      if ("item".equalsIgnoreCase(childCursor.getLocalName())) {
        collectFunction(ccnIndex, childCursor, files);
      }
    }
  }

  private int indexOfCCN(SMInputCursor labelsCursor) throws javax.xml.stream.XMLStreamException {
    int index = 0;
    SMInputCursor labelCursor = labelsCursor.childElementCursor();
    while (labelCursor.getNext() != null) {
      if ("CCN".equalsIgnoreCase(labelCursor.getElemStringValue())) {
        return index;
      }
      index++;
    }
    throw labelCursor.constructStreamException("Cannot find the CNN-label");
  }

  private void collectFunction(int ccnIndex, SMInputCursor itemCursor, Map<String, FileData> files)
          throws javax.xml.stream.XMLStreamException {
    String name = itemCursor.getAttrValue("name");
    String loc[] = name.split(" at ");
    String fullFuncName = loc[0];
    String fullFileName = loc[1];

    loc = fullFuncName.split("::");
    String className = (loc.length > 1) ? loc[0] : "GLOBAL";
    String funcName = (loc.length > 1) ? loc[1] : loc[0];

    CxxCppNcssFile file = new CxxCppNcssFile(fullFileName, CxxOsValidator.getOSType());
    String fileName = file.getFileName();
    int line = file.getLine();

    FileData fileData = files.get(fileName);
    if (fileData == null) {
      fileData = new FileData(fileName);
      files.put(fileName, fileData);
    }

    SMInputCursor valueCursor = itemCursor.childElementCursor("value");
    String methodComplexity = stringValueOfChildWithIndex(valueCursor, ccnIndex);
    fileData.addMethod(className, funcName, Integer.parseInt(methodComplexity.trim()), line);
  }

  private String stringValueOfChildWithIndex(SMInputCursor cursor, int targetIndex)
          throws javax.xml.stream.XMLStreamException {
    int index = 0;
    while (index <= targetIndex) {
      cursor.advance();
      index++;
    }
    return cursor.getElemStringValue();
  }

  private void saveMetrics(Project project, SensorContext context, FileData fileData) {
    String filePath = fileData.getName();
    org.sonar.api.resources.File file =
            org.sonar.api.resources.File.fromIOFile(new File(filePath), project);

    if (context.getResource(file) != null) {
      CxxUtils.LOG.debug("Saving complexity measures for file '{}'", filePath);

      RangeDistributionBuilder complexityMethodsDistribution =
              new RangeDistributionBuilder(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION,
              METHODS_DISTRIB_BOTTOM_LIMITS);
      RangeDistributionBuilder complexityFileDistribution =
              new RangeDistributionBuilder(CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION,
              FILE_DISTRIB_BOTTOM_LIMITS);
      RangeDistributionBuilder complexityClassDistribution =
              new RangeDistributionBuilder(CoreMetrics.CLASS_COMPLEXITY_DISTRIBUTION,
              CLASS_DISTRIB_BOTTOM_LIMITS);

      complexityFileDistribution.add(fileData.getComplexity());

      CxxSourceCode sourceCode = new CxxSourceCode(file, new File(filePath));

      for (ClassData classData : fileData.getClasses()) {
        complexityClassDistribution.add(classData.getComplexity());
        for (FunctionData methods : classData.getMethods()) {

          CxxMaximumComplexity check = (CxxMaximumComplexity) CxxChecksRepository.getCheck(annotationCheckFactory, CxxMaximumComplexity.getMyself());

          if (check != null) {
            check.setLine(methods.line);            
            check.setFunctionComplexity(methods.complexity);
            check.setFunctionName(methods.name);
            check.validate(annotationCheckFactory.getActiveRule(check).getRule(), sourceCode);
          }


          complexityMethodsDistribution.add(methods.complexity);
        }
      }

      sourceCode.saveViolations(context);

      context.saveMeasure(file, CoreMetrics.FUNCTIONS, (double) fileData.getNoMethods());
      context.saveMeasure(file, CoreMetrics.COMPLEXITY, (double) fileData.getComplexity());
      context.saveMeasure(file, complexityMethodsDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
      context.saveMeasure(file, complexityClassDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
      context.saveMeasure(file, complexityFileDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));

    } else {
      CxxUtils.LOG.debug("Ignoring complexity measures for file '{}'", filePath);
    }
  }

  private static class FileData {

    private String name;
    private int noMethods = 0;
    private Map<String, ClassData> classes = new HashMap<String, ClassData>();
    private int complexity = 0;

    FileData(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public int getNoMethods() {
      return noMethods;
    }

    public int getComplexity() {
      return complexity;
    }

    /**
     * @return data for classes contained in this file
     */
    public Collection<ClassData> getClasses() {
      return classes.values();
    }

    /**
     * Adds complexity data for a method with given name in a given class
     *
     * @param className Name of method's class
     * @param methodName The name of the method to add data for
     * @param complexity The complexity number to store
     */
    public void addMethod(String className, String methodName, int complexity, int line) {
      noMethods++;
      this.complexity += complexity;

      ClassData classData = classes.get(className);
      if (classData == null) {
        classData = new ClassData();
        classes.put(className, classData);
      }
      classData.addMethod(methodName, complexity, line);
    }
  }

  private static class ClassData {

    private List<FunctionData> methods = new ArrayList<FunctionData>();
    private int complexity = 0;

    /**
     * Adds complexity data for a method with given name
     *
     * @param name The name of the method to add data for
     * @param complexity The complexity number to store
     */
    public void addMethod(String name, int complexity, int line) {
      this.complexity += complexity;
      methods.add(new FunctionData(name, complexity, line));
    }

    public Integer getComplexity() {
      return complexity;
    }

    /**
     * @return complexity numbers for methods inside of this class
     */
    public List<FunctionData> getMethods() {
      return methods;
    }
  }

  private static class FunctionData {

    public final String name;
    public final int complexity;
    public final int line;

    FunctionData(String name, int complexity, int line) {
      this.name = name;
      this.complexity = complexity;
      this.line = line;
    }
  }
}
