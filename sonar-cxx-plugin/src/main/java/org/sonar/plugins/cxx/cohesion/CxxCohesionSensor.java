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
package org.sonar.plugins.cxx.cohesion;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.sonar.plugins.cxx.ast.cpp.HasFullName;
import org.sonar.plugins.cxx.cohesion.graph.Edge;
import org.sonar.plugins.cxx.cohesion.graph.Graph;
import org.sonar.plugins.cxx.cohesion.graph.Node;
import org.sonar.plugins.cxx.utils.CxxFileSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;

public class CxxCohesionSensor extends CxxFileSensor {

  private static final Number[] LCOM4_LIMITS = { 2, 3, 4, 5, 10 };

  private CxxCppParser parser = new CxxCppParser();
  private RangeDistributionBuilder builder = new RangeDistributionBuilder(CoreMetrics.LCOM4_DISTRIBUTION, LCOM4_LIMITS);

  @Override
  protected void parseFile(InputFile file, Project project, SensorContext context) {
    try {
      CxxCppParsedFile parsedFile = parser.parseFile(file);
      
      double lcom4 = analyzeFileCohesion(parsedFile.getClasses());

      CxxUtils.LOG.debug(parsedFile + " " + lcom4);
      saveFileMeasure(file, lcom4, project, context);
    } catch (CxxCppParserException e) {
      CxxUtils.LOG.error(e.getMessage());
    }
  }

  private void saveFileMeasure(InputFile inputFile, double fileCohesion, Project project, SensorContext context) {
    org.sonar.api.resources.File resource = org.sonar.api.resources.File.fromIOFile(inputFile.getFile(), project);
    if(context.getResource(resource) != null) {
      context.saveMeasure(resource, CoreMetrics.LCOM4, fileCohesion);
      context.saveMeasure(resource, builder.build().setPersistenceMode(PersistenceMode.MEMORY));
    } else {
      CxxUtils.LOG.error("Resource not indexed: " + inputFile.getFile().getAbsolutePath());
    }
  }

  private double analyzeFileCohesion(Set<CxxClass> classes) {
    double fileCohesion = 0;
    Iterator<CxxClass> it = classes.iterator();
    while(it.hasNext()) {
      CxxClass clazz = it.next();
      double classCohesion = analyzeClassCohesion(clazz);
      builder.add(classCohesion);
      fileCohesion += classCohesion;
    }
    return fileCohesion;
  }

  private double analyzeClassCohesion(CxxClass clazz) {
    CxxUtils.LOG.debug("Analyzing " + clazz + " LCOM4...");
    Set<CxxClassMember> classMembers = clazz.getMembers();
    Set<CxxClassMethod> classMethods = clazz.getMethods();
    if(classMethods.isEmpty()) {
      return 0.0;
    }

    Graph callGraph = constructCallGraph(classMembers, classMethods);
    analyzeMemberAndMethodUsage(callGraph, classMembers, classMethods);
    return getCohesiveComponentsCount(callGraph);
  }

  private double getCohesiveComponentsCount(Graph callGraph) {
    double lcom4 = 0.0;
    Set<Node> graphNodes = new HashSet<Node>(callGraph.getNodes());
    Iterator<Node> nodeIt = callGraph.getNodes().iterator();

    while(nodeIt.hasNext()) { //remove connected nodes
      Set<Node> toRemove = callGraph.visitAllNodesFrom(nodeIt.next());
      if(graphNodes.removeAll(toRemove)) {
        lcom4 += 1.0;
      }
    }
    
    while(!graphNodes.isEmpty()) {  //remove rest
      nodeIt = graphNodes.iterator();
      while(nodeIt.hasNext()) {
        nodeIt.remove();
        lcom4 += 1.0;
      }
    }

    return lcom4;
  }

  private void analyzeMemberAndMethodUsage(Graph callGraph, Set<CxxClassMember> classMembers, Set<CxxClassMethod> classMethods) {
    Iterator<CxxClassMethod> methodIt = classMethods.iterator();
    while(methodIt.hasNext()) {
      CxxClassMethod method = methodIt.next();
      analyzeMemberUsage(callGraph, classMembers, method);
      analyzeMethodUsage(callGraph, classMethods, method);
    }

  }

  private void analyzeMethodUsage(Graph callGraph, Set<CxxClassMethod> classMethods, CxxClassMethod method) {
    Iterator<CxxClassMethod> secondMethodIt = classMethods.iterator();
    while(secondMethodIt.hasNext()) {
      CxxClassMethod secondMethod = secondMethodIt.next();
      if(!secondMethod.equals(method) && isNameUsedInMethod(secondMethod, method)) {
        callGraph.addEdge( new Edge( new Node(secondMethod), new Node(method) ) );
        CxxUtils.LOG.debug("Method connection " + secondMethod + " <--> " + method);
      }

    }
  }

  private void analyzeMemberUsage(Graph callGraph, Set<CxxClassMember> classMembers, CxxClassMethod method) {
    Iterator<CxxClassMember> memberIt = classMembers.iterator();
    while(memberIt.hasNext()) {
      CxxClassMember member = memberIt.next();
      if(isNameUsedInMethod(member, method)) {
        callGraph.addEdge( new Edge( new Node(member), new Node(method) ) );
        CxxUtils.LOG.debug("Member connection " + member + " <--> " + method);
      }
    }
  }

  private boolean isNameUsedInMethod(HasFullName fullNameObject, CxxClassMethod method) {
    List<String> usedNames = method.getBody().getDetectedNames();
    if(usedNames.contains(fullNameObject.getName())) {
      return true;
    } 
    return false;
  }

  private Graph constructCallGraph(Set<CxxClassMember> classMembers, Set<CxxClassMethod> classMethods) {
    Graph callGraph = new Graph();

    Iterator<CxxClassMember> memberIt = classMembers.iterator();
    while(memberIt.hasNext()) {
      callGraph.addNode( new Node(memberIt.next()) );
    }

    Iterator<CxxClassMethod> methodIt = classMethods.iterator();
    while(methodIt.hasNext()) {
      callGraph.addNode( new Node(methodIt.next()));
    }

    return callGraph;
  }

}
