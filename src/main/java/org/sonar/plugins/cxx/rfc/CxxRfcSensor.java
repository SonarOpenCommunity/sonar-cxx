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
package org.sonar.plugins.cxx.rfc;

import java.util.Iterator;
import java.util.Set;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.RangeDistributionBuilder;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.cxx.ast.CxxCppParsedFile;
import org.sonar.plugins.cxx.ast.CxxCppParser;
import org.sonar.plugins.cxx.ast.CxxCppParserException;
import org.sonar.plugins.cxx.ast.cpp.CxxClass;
import org.sonar.plugins.cxx.ast.cpp.CxxClassMember;
import org.sonar.plugins.cxx.ast.cpp.CxxClassMethod;
import org.sonar.plugins.cxx.utils.CxxFileSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;

public class CxxRfcSensor extends CxxFileSensor {

  private static final Number[] RFC_DISTRIB_LIMITS = { 0, 5, 10, 20, 30, 50, 90, 100, 150 };
  
  private CxxCppParser parser = new CxxCppParser();
  private CxxCppParsedFile parsedFile = null;
  private RangeDistributionBuilder rfcBuilder = new RangeDistributionBuilder(CoreMetrics.RFC_DISTRIBUTION, RFC_DISTRIB_LIMITS);
    
  @Override
  protected void parseFile(InputFile file, Project project, SensorContext context) {
    try {
      parsedFile = parser.parseFile(file);
      Set<CxxClass> fileClasses = parsedFile.getClasses();
    
      saveFileRfc(file, project, context, analyzeFileRfc(fileClasses) );
      
    } catch (CxxCppParserException e) {
      CxxUtils.LOG.error(e.getMessage());
    }
  }

  private void saveFileRfc(InputFile inputFile, Project project, SensorContext context, double fileRfc) {
    org.sonar.api.resources.File resource = org.sonar.api.resources.File.fromIOFile(inputFile.getFile(), project);
    if(context.getResource(resource) != null) {
    
      context.saveMeasure(resource, CoreMetrics.RFC, fileRfc);
      context.saveMeasure(resource, rfcBuilder.build().setPersistenceMode(PersistenceMode.MEMORY));

      CxxUtils.LOG.debug("RFC for {} is {}", inputFile.getFile(), fileRfc);
      
    } else {
      CxxUtils.LOG.error("Resource not indexed: " + inputFile.getFile().getAbsolutePath());
    }
  }

  private double analyzeFileRfc(Set<CxxClass> fileClasses) {
    double rfc = 0.0;
    Iterator<CxxClass> it = fileClasses.iterator();
    while(it.hasNext()) {
      double classRfc = analyzeClassRfc(it.next());
      rfc += classRfc;
      rfcBuilder.add(classRfc);
    }
    
    return rfc;
  }

  private double analyzeClassRfc(CxxClass clazz) {
    double classRfc = 0.0;
    
    Iterator<CxxClassMethod> it = clazz.getMethods().iterator();
    while(it.hasNext()) {
      classRfc += analyzeMethodRfc(clazz, it.next());
    }

    return classRfc;
  }

  private double analyzeMethodRfc(CxxClass methodClass, CxxClassMethod method) {
    double methodRfc = method.isImplemented() ? 1.0 : 0.0;
    
    for(String bodyStatement : method.getBody().getDetectedNames()) {
      if( isMethodCall(bodyStatement, methodClass) ) {
        methodRfc += 1.0;
      }
    }
    
    return methodRfc;
  }

  private boolean isMethodCall(String methodName, CxxClass methodClass) {
    Iterator<CxxClass> ancestorIt = methodClass.getAncestors().iterator();
    while(ancestorIt.hasNext()) {
      if(ancestorIt.next().findMethodByName(methodName) != null) {
        return true;
      }
    }
    
    Iterator<CxxClassMember> memberIt = methodClass.getMembers().iterator();
    while(memberIt.hasNext()) {
      String memberType = memberIt.next().getType();
      CxxClass memberTypeClass = parsedFile.findClassByName(memberType);
      if(memberTypeClass != null && memberTypeClass.findMethodByName(methodName) != null) {
        return true;
      }
    }
    
    return false;
  }

}
