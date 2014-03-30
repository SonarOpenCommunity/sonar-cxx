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

import com.google.common.collect.Lists;
import com.sonar.sslr.squid.AstScanner;
import com.sonar.sslr.squid.SquidAstVisitor;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.checks.AnnotationCheckFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.RangeDistributionBuilder;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Violation;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.checks.CheckList;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.CxxPlugin;
import org.sonar.squid.api.CheckMessage;
import org.sonar.squid.api.SourceCode;
import org.sonar.squid.api.SourceFile;
import org.sonar.squid.api.SourceFunction;
import org.sonar.squid.indexer.QueryByParent;
import org.sonar.squid.indexer.QueryByType;
import com.sonar.sslr.api.Grammar;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * {@inheritDoc}
 */
public final class CxxSquidSensor implements Sensor {
  private static final Number[] FUNCTIONS_DISTRIB_BOTTOM_LIMITS = {1, 2, 4, 6, 8, 10, 12, 20, 30};
  private static final Number[] FILES_DISTRIB_BOTTOM_LIMITS = {0, 5, 10, 20, 30, 60, 90};

  private final AnnotationCheckFactory annotationCheckFactory;
  
  private Project project;
  private SensorContext context;
  private AstScanner<Grammar> scanner;
  private Settings conf;
  private ModuleFileSystem fs;
  
  /**
   * {@inheritDoc}
   */
  public CxxSquidSensor(RulesProfile profile, Settings conf, ModuleFileSystem fs) {
    this.annotationCheckFactory = AnnotationCheckFactory.create(profile, CheckList.REPOSITORY_KEY, CheckList.getChecks());
    this.conf = conf;
    this.fs = fs;
  }

  public boolean shouldExecuteOnProject(Project project) {
    return !project.getFileSystem().mainFiles(CxxLanguage.KEY).isEmpty();
  }
  
  /**
   * {@inheritDoc}
   */
  public void analyse(Project project, SensorContext context) {
    this.project = project;
    this.context = context;
    
    Collection<SquidAstVisitor<Grammar>> squidChecks = annotationCheckFactory.getChecks();
    List<SquidAstVisitor<Grammar>> visitors = Lists.newArrayList(squidChecks);
    this.scanner = CxxAstScanner.create(createConfiguration(project, conf),
                                        visitors.toArray(new SquidAstVisitor[visitors.size()]));
    
    scanner.scanFiles(fs.files(CxxLanguage.sourceQuery));
    
    Collection<SourceCode> squidSourceFiles = scanner.getIndex().search(new QueryByType(SourceFile.class));
    save(squidSourceFiles);
  }

  private CxxConfiguration createConfiguration(Project project, Settings conf) {
    CxxConfiguration cxxConf = new CxxConfiguration(fs.sourceCharset());
    cxxConf.setBaseDir(fs.baseDir().getAbsolutePath());
    String[] lines = conf.getStringLines(CxxPlugin.DEFINES_KEY);
    if(lines.length > 0){
      cxxConf.setDefines(Arrays.asList(lines));
    }
    cxxConf.setIncludeDirectories(conf.getStringArray(CxxPlugin.INCLUDE_DIRECTORIES_KEY));
    return cxxConf;
  }

  private void save(Collection<SourceCode> squidSourceFiles) {
    for (SourceCode squidSourceFile : squidSourceFiles) {
      SourceFile squidFile = (SourceFile) squidSourceFile;

      org.sonar.api.resources.File sonarFile = org.sonar.api.resources.File.fromIOFile(new File(squidFile.getKey()), project);

      saveMeasures(sonarFile, squidFile);
      saveFilesComplexityDistribution(sonarFile, squidFile);
      saveFunctionsComplexityDistribution(sonarFile, squidFile);
      saveViolations(sonarFile, squidFile);
    }
  }

  private void saveMeasures(org.sonar.api.resources.File sonarFile, SourceFile squidFile) {
    context.saveMeasure(sonarFile, CoreMetrics.FILES, squidFile.getDouble(CxxMetric.FILES));
    context.saveMeasure(sonarFile, CoreMetrics.LINES, squidFile.getDouble(CxxMetric.LINES));
    context.saveMeasure(sonarFile, CoreMetrics.NCLOC, squidFile.getDouble(CxxMetric.LINES_OF_CODE));
    context.saveMeasure(sonarFile, CoreMetrics.STATEMENTS, squidFile.getDouble(CxxMetric.STATEMENTS));
    context.saveMeasure(sonarFile, CoreMetrics.FUNCTIONS, squidFile.getDouble(CxxMetric.FUNCTIONS));
    context.saveMeasure(sonarFile, CoreMetrics.CLASSES, squidFile.getDouble(CxxMetric.CLASSES));
    context.saveMeasure(sonarFile, CoreMetrics.COMPLEXITY, squidFile.getDouble(CxxMetric.COMPLEXITY));
    context.saveMeasure(sonarFile, CoreMetrics.COMMENT_BLANK_LINES, squidFile.getDouble(CxxMetric.COMMENT_BLANK_LINES));
    context.saveMeasure(sonarFile, CoreMetrics.COMMENT_LINES, squidFile.getDouble(CxxMetric.COMMENT_LINES));
  }

  private void saveFunctionsComplexityDistribution(org.sonar.api.resources.File sonarFile, SourceFile squidFile) {
    Collection<SourceCode> squidFunctionsInFile = scanner.getIndex().search(new QueryByParent(squidFile), new QueryByType(SourceFunction.class));
    RangeDistributionBuilder complexityDistribution = new RangeDistributionBuilder(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION, FUNCTIONS_DISTRIB_BOTTOM_LIMITS);
    for (SourceCode squidFunction : squidFunctionsInFile) {
      complexityDistribution.add(squidFunction.getDouble(CxxMetric.COMPLEXITY));
    }
    context.saveMeasure(sonarFile, complexityDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
  }

  private void saveFilesComplexityDistribution(org.sonar.api.resources.File sonarFile, SourceFile squidFile) {
    RangeDistributionBuilder complexityDistribution = new RangeDistributionBuilder(CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION, FILES_DISTRIB_BOTTOM_LIMITS);
    complexityDistribution.add(squidFile.getDouble(CxxMetric.COMPLEXITY));
    context.saveMeasure(sonarFile, complexityDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
  }

  private void saveViolations(org.sonar.api.resources.File sonarFile, SourceFile squidFile) {
    Collection<CheckMessage> messages = squidFile.getCheckMessages();
    if (messages != null) {
      for (CheckMessage message : messages) {
        Violation violation = Violation.create(annotationCheckFactory.getActiveRule(message.getCheck()), sonarFile)
            .setLineId(message.getLine())
            .setMessage(message.getText(Locale.ENGLISH));
        context.saveViolation(violation);
      }
    }
  }
 
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
