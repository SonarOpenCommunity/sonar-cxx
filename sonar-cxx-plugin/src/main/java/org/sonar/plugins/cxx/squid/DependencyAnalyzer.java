/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.squid;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.checks.CheckFactory;
import org.sonar.api.design.Dependency;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.resources.Directory;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Violation;
import org.sonar.cxx.checks.CycleBetweenPackagesCheck;
import org.sonar.graph.*;
import org.sonar.plugins.cxx.utils.CxxUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class DependencyAnalyzer {

  private Project project;
  private SensorContext context;
  private CheckFactory checkFactory;

  private DirectedGraph<File, FileEdge> filesGraph = new DirectedGraph<File, FileEdge>();
  private DirectedGraph<Directory, DirectoryEdge> packagesGraph = new DirectedGraph<Directory, DirectoryEdge>();
  private HashMap<Edge, Dependency> dependencyIndex = new HashMap<Edge, Dependency>();
  private Multimap<Directory, File> directoryFiles = HashMultimap.create();

  public DependencyAnalyzer(Project project, SensorContext context, CheckFactory checkFactory) {
    this.project = project;
    this.context = context;
    this.checkFactory = checkFactory;
  }

  public void addFile(File sonarFile, Collection<String> includedFiles) {
    //Store the directory and file
    Directory sonarDir = sonarFile.getParent();
    packagesGraph.addVertex(sonarDir);
    directoryFiles.put(sonarDir, sonarFile);

    //Build the dependency graph
    for (String file : includedFiles) {
      File includedFile = File.fromIOFile(new java.io.File(file), project);
      if (includedFile != null) {
        //Add the dependency in the files graph
        FileEdge fileEdge = new FileEdge(sonarFile, includedFile);
        filesGraph.addEdge(fileEdge);

        //Add the dependency in the packages graph, if the directories are different
        Directory includedDir = includedFile.getParent();
        if (!sonarDir.equals(includedDir)) {
          DirectoryEdge edge = packagesGraph.getEdge(sonarDir, includedDir);
          if (edge == null) {
            edge = new DirectoryEdge(sonarDir, includedDir);
            packagesGraph.addEdge(edge);
          }
          edge.addRootEdge(fileEdge);
        }
      }
      else {
        CxxUtils.LOG.warn("Unable to find resource '" + file + "' to create a dependency with '" + sonarFile.getKey() + "'");
      }
    }
  }

  /** Perform the analysis and save the results.
   */
  public void save() {
    final Collection<Directory> packages = packagesGraph.getVertices();
    for (Directory dir : packages) {
      //Save dependencies (cross-directories, including cross-directory file dependencies)
      for (DirectoryEdge edge : packagesGraph.getOutgoingEdges(dir)) {
        Dependency dependency = new Dependency(dir, edge.getTo())
            .setUsage("references")
            .setWeight(edge.getWeight())
            .setParent(null);
        context.saveDependency(dependency);
        dependencyIndex.put(edge, dependency);

        for(FileEdge subEdge : edge.getRootEdges()) {
          saveFileEdge(subEdge, dependency);
        }
      }

      //Save file dependencies (inside directory) & directory metrics
      saveDirectory(dir);
    }

    IncrementalCyclesAndFESSolver<Directory> cycleDetector = new IncrementalCyclesAndFESSolver<Directory>(packagesGraph, packages);
    Set<Cycle> cycles = cycleDetector.getCycles();
    Set<Edge> feedbackEdges = cycleDetector.getFeedbackEdgeSet();
    int tangles = cycleDetector.getWeightOfFeedbackEdgeSet();

    CxxUtils.LOG.info("Project '" + project.getKey() + "'"
        + " Cycles:" + cycles.size()
        + " Feedback cycles:" + feedbackEdges.size()
        + " Tangles:" + tangles
        + " Weight:" + getEdgesWeight(packagesGraph.getEdges(packages)));

    saveViolations(feedbackEdges, packagesGraph);
    savePositiveMeasure(project, CoreMetrics.PACKAGE_CYCLES, cycles.size());
    savePositiveMeasure(project, CoreMetrics.PACKAGE_FEEDBACK_EDGES, feedbackEdges.size());
    savePositiveMeasure(project, CoreMetrics.PACKAGE_TANGLES, tangles);
    savePositiveMeasure(project, CoreMetrics.PACKAGE_EDGES_WEIGHT, getEdgesWeight(packagesGraph.getEdges(packages)));

    String dsmJson = serializeDsm(packages, feedbackEdges);
    Measure dsmMeasure = new Measure(CoreMetrics.DEPENDENCY_MATRIX, dsmJson)
        .setPersistenceMode(PersistenceMode.DATABASE);
    context.saveMeasure(project, dsmMeasure);
  }

  private void saveDirectory(Directory dir) {
    final Collection<File> files = directoryFiles.get(dir);
    for(File file: files) {
      for (FileEdge edge : filesGraph.getOutgoingEdges(file)) {
        saveFileEdge(edge, null);
      }
    }

    IncrementalCyclesAndFESSolver<File> cycleDetector = new IncrementalCyclesAndFESSolver<File>(filesGraph, files);
    Set<Cycle> cycles = cycleDetector.getCycles();
    MinimumFeedbackEdgeSetSolver solver = new MinimumFeedbackEdgeSetSolver(cycles);
    Set<Edge> feedbackEdges = solver.getEdges();
    int tangles = solver.getWeightOfFeedbackEdgeSet();

    CxxUtils.LOG.info("Directory: '" + dir.getKey() + "'"
        + " Cycles:" + cycles.size()
        + " Feedback cycles:" + feedbackEdges.size()
        + " Tangles:" + tangles
        + " Weight:" + getEdgesWeight(filesGraph.getEdges(files)));

    savePositiveMeasure(dir, CoreMetrics.FILE_CYCLES, cycles.size());
    savePositiveMeasure(dir, CoreMetrics.FILE_FEEDBACK_EDGES, feedbackEdges.size());
    savePositiveMeasure(dir, CoreMetrics.FILE_TANGLES, tangles);
    savePositiveMeasure(dir, CoreMetrics.FILE_EDGES_WEIGHT, getEdgesWeight(filesGraph.getEdges(files)));

    String dsmJson = serializeDsm(files, feedbackEdges);
    context.saveMeasure(dir, new Measure(CoreMetrics.DEPENDENCY_MATRIX, dsmJson));
  }

  private void saveViolations(Set<Edge> feedbackEdges, DirectedGraph<Directory, DirectoryEdge> packagesGraph) {
    ActiveRule rule = CycleBetweenPackagesCheck.getActiveRule(checkFactory);
    if (rule == null) {
      // Rule inactive
      return;
    }
    for (Edge feedbackEdge : feedbackEdges) {
      Directory fromPackage = (Directory) feedbackEdge.getFrom();
      Directory toPackage = (Directory) feedbackEdge.getTo();
      DirectoryEdge edge = packagesGraph.getEdge(fromPackage, toPackage);
      for (FileEdge subEdge : edge.getRootEdges()) {
        Resource fromFile = subEdge.getFrom();
        Resource toFile = subEdge.getTo();
        // If resource cannot be obtained, then silently ignore, because anyway warning will be printed by method addFile
        if ((fromFile != null) && (toFile != null)) {
          Violation violation = Violation.create(rule, fromFile)
              .setMessage("Remove the dependency from file \"" + fromFile.getLongName()
                  + "\" to file \"" + toFile.getLongName() + "\" to break a package cycle.")
              .setCost((double) subEdge.getWeight());
          context.saveViolation(violation);
        }
      }
    }
  }

  private void saveFileEdge(FileEdge edge, Dependency parent) {
    if (!dependencyIndex.containsKey(edge)) {
      Dependency dependency = new Dependency(edge.getFrom(), edge.getTo())
          .setUsage("includes")
          .setWeight(edge.getWeight())
          .setParent(parent);
      context.saveDependency(dependency);
      dependencyIndex.put(edge, dependency);
    }
  }

  private double getEdgesWeight(Collection<? extends Edge> edges) {
    double total = 0.0;
    for (Edge edge : edges) {
      total += edge.getWeight();
    }
    return total;
  }

  private String serializeDsm(Collection<? extends Resource> vertices, Set<Edge> feedbackEdges) {
    Dsm<? extends Resource> dsm = new Dsm(packagesGraph, vertices, feedbackEdges);
    DsmTopologicalSorter.sort(dsm);
    return DsmSerializer.serialize(dsm, dependencyIndex);
  }

  private void savePositiveMeasure(Resource sonarResource, Metric metric, double value) {
    if (value >= 0.0) {
      context.saveMeasure(sonarResource, metric, value);
    }
  }

}
