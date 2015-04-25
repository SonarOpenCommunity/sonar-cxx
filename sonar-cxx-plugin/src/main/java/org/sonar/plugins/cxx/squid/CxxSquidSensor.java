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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import javax.annotation.Nullable;

import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.component.Perspective;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.RangeDistributionBuilder;
import org.sonar.api.resources.Project;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.checks.CheckList;
import org.sonar.cxx.parser.CxxParser;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.utils.CxxMetrics;
import org.sonar.plugins.cxx.CxxPlugin;
import org.sonar.plugins.cxx.compiler.CxxCompilerSensor;
import org.sonar.squidbridge.AstScanner;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.api.CheckMessage;
import org.sonar.squidbridge.api.SourceCode;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.api.SourceFunction;
import org.sonar.squidbridge.indexer.QueryByParent;
import org.sonar.squidbridge.indexer.QueryByType;
import org.sonar.api.source.Highlightable;
import org.sonar.plugins.cxx.highlighter.CxxHighlighter;

import com.sonar.sslr.api.Grammar;

import org.sonar.cxx.checks.CompileIncludePathNotFoundOrInvalid;
import org.sonar.cxx.checks.DuplicatedIncludeCheck;
import static org.sonar.plugins.cxx.compiler.CxxCompilerSensor.REPORT_CHARSET_DEF;
import org.sonar.plugins.cxx.utils.CxxReportSensor;

/**
 * {@inheritDoc}
 */
public final class CxxSquidSensor implements Sensor {
  private static final Number[] FUNCTIONS_DISTRIB_BOTTOM_LIMITS = {1, 2, 4, 6, 8, 10, 12, 20, 30};
  private static final Number[] FILES_DISTRIB_BOTTOM_LIMITS = {0, 5, 10, 20, 30, 60, 90};

  private final Checks<Object> checks;
  private ActiveRules rules;

  private Project project;
  private SensorContext context;
  private AstScanner<Grammar> scanner;
  private Settings conf;
  private FileSystem fs;
  private ResourcePerspectives resourcePerspectives;
  private final FilePredicate mainFilePredicate;
  /**
   * {@inheritDoc}
   */
  public CxxSquidSensor(ResourcePerspectives resourcePerspectives, Settings conf,
                        FileSystem fs, CheckFactory checkFactory, ActiveRules rules) {
    this.checks = checkFactory.create(CheckList.REPOSITORY_KEY).addAnnotatedChecks(CheckList.getChecks());
    this.rules = rules;
    this.conf = conf;
    this.fs = fs;
    this.resourcePerspectives = resourcePerspectives;
    FilePredicates predicates = fs.predicates();
    this.mainFilePredicate = predicates.and(predicates.hasType(InputFile.Type.MAIN),
                                            predicates.hasLanguage(CxxLanguage.KEY));
  }

  public boolean shouldExecuteOnProject(Project project) {
    return fs.hasFiles(mainFilePredicate);
  }

  /**
   * {@inheritDoc}
   */
  public void analyse(Project project, SensorContext context) {
    this.project = project;
    this.context = context;

    List<SquidAstVisitor<Grammar>> visitors = new ArrayList<SquidAstVisitor<Grammar>>((Collection) checks.all());
    this.scanner = CxxAstScanner.create(createConfiguration(this.fs, this.conf),
                                        visitors.toArray(new SquidAstVisitor[visitors.size()]));

    scanner.scanFiles(Lists.newArrayList(fs.files(mainFilePredicate)));

    Collection<SourceCode> squidSourceFiles = scanner.getIndex().search(new QueryByType(SourceFile.class));
    save(squidSourceFiles);
    
    highlight();
  }

  private void highlight() {
    CxxHighlighter highlighter = new CxxHighlighter(createConfiguration(fs, conf));
    for (InputFile inputFile : fs.inputFiles(mainFilePredicate)) {
      highlighter.highlight(perspective(Highlightable.class, inputFile), inputFile.file());
    }
  }
  
  <P extends Perspective<?>> P perspective(Class<P> clazz, @Nullable InputFile file) {
    if (file == null) {
      throw new IllegalArgumentException("Cannot get " + clazz.getCanonicalName() + "for a null file");
    }
    P result = resourcePerspectives.as(clazz, file);
    if (result == null) {
      throw new IllegalStateException("Could not get " + clazz.getCanonicalName() + " for " + file);
    }
    return result;
  }

  private CxxConfiguration createConfiguration(FileSystem fs, Settings conf) {
    CxxConfiguration cxxConf = new CxxConfiguration(fs, resourcePerspectives, CompileIncludePathNotFoundOrInvalid.getActiveRule(rules));
    cxxConf.setBaseDir(fs.baseDir().getAbsolutePath());
    String[] lines = conf.getStringLines(CxxPlugin.DEFINES_KEY);
    if(lines.length > 0){
      cxxConf.setDefines(Arrays.asList(lines));
    }
    cxxConf.setIncludeDirectories(conf.getStringArray(CxxPlugin.INCLUDE_DIRECTORIES_KEY));
    cxxConf.setErrorRecoveryEnabled(conf.getBoolean(CxxPlugin.ERROR_RECOVERY_KEY));
    cxxConf.setForceIncludeFiles(conf.getStringArray(CxxPlugin.FORCE_INCLUDE_FILES_KEY));
    cxxConf.setCFilesPatterns(conf.getStringArray(CxxPlugin.C_FILES_PATTERNS_KEY));
    cxxConf.setHeaderFileSuffixes(conf.getStringArray(CxxPlugin.HEADER_FILE_SUFFIXES_KEY));
    cxxConf.setMissingIncludeWarningsEnabled(conf.getBoolean(CxxPlugin.MISSING_INCLUDE_WARN));
    
    String filePaths = conf.getString(CxxCompilerSensor.REPORT_PATH_KEY);    
    if (filePaths != null && !"".equals(filePaths)) {
      List<File> reports = CxxReportSensor.getReports(conf, fs.baseDir().getPath(), CxxCompilerSensor.REPORT_PATH_KEY, "");
      cxxConf.setCompilationPropertiesWithBuildLog(reports,
                                                   conf.getString(CxxCompilerSensor.PARSER_KEY_DEF),
                                                   conf.getString(CxxCompilerSensor.REPORT_CHARSET_DEF));
    }

    return cxxConf;
  }

  private void save(Collection<SourceCode> squidSourceFiles) {
    int violationsCount = 0;
    DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer(resourcePerspectives, project, context, rules);
    for (SourceCode squidSourceFile : squidSourceFiles) {
      SourceFile squidFile = (SourceFile) squidSourceFile;
      File ioFile = new File(squidFile.getKey());

      org.sonar.api.resources.File sonarFile = org.sonar.api.resources.File.fromIOFile(ioFile, project); //@todo fromIOFile: deprecated, see http://javadocs.sonarsource.org/4.5.2/apidocs/deprecated-list.html

      saveMeasures(sonarFile, squidFile);
      saveFilesComplexityDistribution(sonarFile, squidFile);
      saveFunctionsComplexityDistribution(sonarFile, squidFile);
      violationsCount += saveViolations(sonarFile, squidFile);
      dependencyAnalyzer.addFile(sonarFile, CxxParser.getIncludedFiles(ioFile));
    }

    Measure measure = new Measure(CxxMetrics.SQUID);
    measure.setIntValue(violationsCount);
    context.saveMeasure(measure);
    dependencyAnalyzer.save();
  }

  private void saveMeasures(org.sonar.api.resources.File sonarFile, SourceFile squidFile) {
    context.saveMeasure(sonarFile, CoreMetrics.FILES, squidFile.getDouble(CxxMetric.FILES));
    context.saveMeasure(sonarFile, CoreMetrics.LINES, squidFile.getDouble(CxxMetric.LINES));
    context.saveMeasure(sonarFile, CoreMetrics.NCLOC, squidFile.getDouble(CxxMetric.LINES_OF_CODE));
    context.saveMeasure(sonarFile, CoreMetrics.STATEMENTS, squidFile.getDouble(CxxMetric.STATEMENTS));
    context.saveMeasure(sonarFile, CoreMetrics.FUNCTIONS, squidFile.getDouble(CxxMetric.FUNCTIONS));
    context.saveMeasure(sonarFile, CoreMetrics.CLASSES, squidFile.getDouble(CxxMetric.CLASSES));
    context.saveMeasure(sonarFile, CoreMetrics.COMPLEXITY, squidFile.getDouble(CxxMetric.COMPLEXITY));
    context.saveMeasure(sonarFile, CoreMetrics.COMMENT_LINES, squidFile.getDouble(CxxMetric.COMMENT_LINES));
    context.saveMeasure(sonarFile, CoreMetrics.PUBLIC_API, squidFile.getDouble(CxxMetric.PUBLIC_API));
    context.saveMeasure(sonarFile, CoreMetrics.PUBLIC_UNDOCUMENTED_API, squidFile.getDouble(CxxMetric.PUBLIC_UNDOCUMENTED_API));
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

  private int saveViolations(org.sonar.api.resources.File sonarFile, SourceFile squidFile) {
    Collection<CheckMessage> messages = squidFile.getCheckMessages();
    int violationsCount = 0;
    if (messages != null) {
      Issuable issuable = resourcePerspectives.as(Issuable.class, sonarFile);
      if (issuable != null) {
        for (CheckMessage message : messages) {
          Issue issue = issuable.newIssueBuilder()
            .ruleKey(checks.ruleKey(message.getCheck()))
            .line(message.getLine())
            .message(message.getText(Locale.ENGLISH))
            .build();
          if (issuable.addIssue(issue))
            violationsCount++;
        }
      }
      return violationsCount;
    }
    return 0;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
