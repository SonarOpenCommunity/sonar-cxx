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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.RangeDistributionBuilder;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.cxx.utils.CxxSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * {@inheritDoc}
 */
public class CxxCppNcssSensor extends CxxSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.cppncss.reportPath";
  private static final String DEFAULT_REPORT_PATH = "cppncss-reports/cppncss-result-*.xml";
  private static final Number[] METHODS_DISTRIB_BOTTOM_LIMITS = { 1, 2, 4, 6, 8, 10, 12 };
  private static final Number[] FILE_DISTRIB_BOTTOM_LIMITS = { 0, 5, 10, 20, 30, 60, 90 };
  private static final Number[] CLASS_DISTRIB_BOTTOM_LIMITS = { 0, 5, 10, 20, 30, 60, 90 };

  /**
   * {@inheritDoc}
   */
  public CxxCppNcssSensor(Configuration conf) {
    super(conf);
  }

  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }
  
  protected String defaultReportPath() {
    return DEFAULT_REPORT_PATH;
  }

  protected void parseReport(final Project project, final SensorContext context, File report)
    throws javax.xml.stream.XMLStreamException
  {
    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {
      /**
       * {@inheritDoc}
       */
      public void stream(SMHierarchicCursor rootCursor)  throws javax.xml.stream.XMLStreamException {
        Map<String, FileData> files = new HashMap<String, FileData>();
        rootCursor.advance(); //cppncss
        
        SMInputCursor measureCursor = rootCursor.childElementCursor("measure");
        while (measureCursor.getNext() != null) {
          collectMeasure(measureCursor, files);
        }

        for (FileData fileData: files.values()) {
          saveMetrics(project, context, fileData);
        }
      }
    });
    parser.parse(report);
  }
  
  private void collectMeasure(SMInputCursor measureCursor, Map<String, FileData> files)
    throws javax.xml.stream.XMLStreamException
  {
    // collect only function measures
    String type = measureCursor.getAttrValue("type");
    if (type.equalsIgnoreCase("function")) {
      collectFunctions(measureCursor, files);
    }
  }

  private void collectFunctions(SMInputCursor measureCursor, Map<String, FileData> files)
    throws javax.xml.stream.XMLStreamException
  {
    // determine the position of ccn measure using 'labels' analysis
    SMInputCursor childCursor = measureCursor.childElementCursor();
    int ccnIndex = indexOfCCN(childCursor.advance());
    
    // iterate over the function items and collect them
    while (childCursor.getNext() != null) {
      if("item".equalsIgnoreCase(childCursor.getLocalName())){
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
    throws javax.xml.stream.XMLStreamException
  {
    String name = itemCursor.getAttrValue("name");
    String loc[] = name.split(" at ");
    String fullFuncName = loc[0];
    String fullFileName = loc[1];

    loc = fullFuncName.split("::");
    String className = (loc.length > 1) ? loc[0] : "GLOBAL";
    String funcName = (loc.length > 1) ? loc[1] : loc[0];
    loc = fullFileName.split(":");
    String fileName = loc[0];
    
    FileData fileData = files.get(fileName);
    if (fileData == null) {
      fileData = new FileData(fileName);
      files.put(fileName, fileData);
    }

    SMInputCursor valueCursor = itemCursor.childElementCursor("value");
    String methodComplexity = stringValueOfChildWithIndex(valueCursor, ccnIndex);
    fileData.addMethod(className, funcName, Integer.parseInt(methodComplexity.trim()));
  }
  
  private String stringValueOfChildWithIndex(SMInputCursor cursor, int targetIndex)
    throws javax.xml.stream.XMLStreamException
  {
    int index = 0;
    while (index <= targetIndex){
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
      for (ClassData classData: fileData.getClasses()) {
        complexityClassDistribution.add(classData.getComplexity());
        for (Integer complexity: classData.getMethodComplexities()) {
          complexityMethodsDistribution.add(complexity);
        }
      }
      
      context.saveMeasure(file, CoreMetrics.FUNCTIONS, (double)fileData.getNoMethods());
      context.saveMeasure(file, CoreMetrics.COMPLEXITY, (double)fileData.getComplexity());
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

    public String getName() { return name; }

    public int getNoMethods() { return noMethods; }
    
    public int getComplexity() { return complexity; }
    
    /** @return data for classes contained in this file */
    public Collection<ClassData> getClasses() { return classes.values(); }

    /**
     * Adds complexity data for a method with given name in a given class
     * @param className Name of method's class
     * @param methodName The name of the method to add data for
     * @param complexity The complexity number to store
     */
    public void addMethod(String className, String methodName, int complexity) {
      noMethods++;
      this.complexity += complexity;
      
      ClassData classData = classes.get(className);
      if (classData == null) {
        classData = new ClassData();
        classes.put(className, classData);
      }
      classData.addMethod(methodName, complexity);
    }
  }
  
  private static class ClassData {
    private Map<String, Integer> methodComplexities = new HashMap<String, Integer>();
    private int complexity = 0;
    
    /**
     * Adds complexity data for a method with given name
     * @param name The name of the method to add data for
     * @param complexity The complexity number to store
     */
    public void addMethod(String name, int complexity) {
      this.complexity += complexity;
      methodComplexities.put(name, complexity);
    }
    
    public Integer getComplexity() {
      return complexity;
    }
    
    /** @return complexity numbers for methods inside of this class */
    public Collection<Integer> getMethodComplexities() {
      return methodComplexities.values();
    }
  }
}
