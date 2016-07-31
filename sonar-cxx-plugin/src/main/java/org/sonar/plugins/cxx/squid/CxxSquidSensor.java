/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.cxx.squid;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import javax.annotation.Nullable;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.plugins.cxx.api.CustomCxxRulesDefinition;
import org.sonar.cxx.checks.CheckList;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.utils.CxxMetrics;
import org.sonar.plugins.cxx.CxxPlugin;
import org.sonar.plugins.cxx.compiler.CxxCompilerSensor;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.squidbridge.AstScanner;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.api.CheckMessage;
import org.sonar.squidbridge.api.SourceCode;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.api.SourceFunction;
import org.sonar.squidbridge.api.SourceClass;
import org.sonar.squidbridge.indexer.QueryByParent;
import org.sonar.squidbridge.indexer.QueryByType;
import com.sonar.sslr.api.Grammar;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.ce.measure.RangeDistributionBuilder;
import org.sonar.api.rule.RuleKey;
import org.sonar.cxx.parser.CxxParser;
import org.sonar.plugins.cxx.CxxHighlighter;


/**
 * {@inheritDoc}
 */
public final class CxxSquidSensor implements Sensor {

  private static final Number[] LIMITS_COMPLEXITY_METHODS = {1, 2, 4, 6, 8, 10, 12, 20, 30};
  private static final Number[] LIMITS_COMPLEXITY_FILES = {0, 5, 10, 20, 30, 60, 90};

  private final CxxChecks checks;
  private ActiveRules rules;

  private AstScanner<Grammar> scanner;
  private Settings settings;
  
  
  /**
   * {@inheritDoc}
   */
  public CxxSquidSensor(Settings settings, CheckFactory checkFactory, ActiveRules rules) {
    this(settings, checkFactory, rules, null);    
  }
  
  /**
   * {@inheritDoc}
   */
  public CxxSquidSensor(Settings settings, CheckFactory checkFactory, ActiveRules rules,
    @Nullable CustomCxxRulesDefinition[] customRulesDefinition) {
    this.checks = CxxChecks.createCxxCheck(checkFactory)
      .addChecks(CheckList.REPOSITORY_KEY, CheckList.getChecks())
      .addCustomChecks(customRulesDefinition);
    this.rules = rules;
    this.settings = settings;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(CxxLanguage.KEY).name("CxxSquidSensor");
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(SensorContext context) {       
    List<SquidAstVisitor<Grammar>> visitors = new ArrayList<>((Collection) checks.all());
    visitors.add(new CxxHighlighter(context));
    this.scanner = CxxAstScanner.create(createConfiguration(context.fileSystem(), this.settings), context,
      visitors.toArray(new SquidAstVisitor[visitors.size()]));

    Iterable<InputFile> inputFiles = context.fileSystem().inputFiles(context.fileSystem().predicates()
            .and(context.fileSystem().predicates()
                    .hasLanguage(CxxLanguage.KEY), context.fileSystem().predicates()
                            .hasType(InputFile.Type.MAIN)));
    
    List<File> files = new ArrayList<>();
    for(InputFile file : inputFiles) {
      files.add(file.file());
    }
    scanner.scanFiles(files);
    
    Collection<SourceCode> squidSourceFiles = scanner.getIndex().search(new QueryByType(SourceFile.class));
    save(squidSourceFiles, context);
  }

  private CxxConfiguration createConfiguration(FileSystem fs, Settings settings) {
    CxxConfiguration cxxConf = new CxxConfiguration(fs);
    cxxConf.setBaseDir(fs.baseDir().getAbsolutePath());
    String[] lines = settings.getStringLines(CxxPlugin.DEFINES_KEY);
    if (lines.length > 0) {
      cxxConf.setDefines(Arrays.asList(lines));
    }
    cxxConf.setIncludeDirectories(settings.getStringArray(CxxPlugin.INCLUDE_DIRECTORIES_KEY));
    cxxConf.setErrorRecoveryEnabled(settings.getBoolean(CxxPlugin.ERROR_RECOVERY_KEY));
    cxxConf.setForceIncludeFiles(settings.getStringArray(CxxPlugin.FORCE_INCLUDE_FILES_KEY));
    cxxConf.setCFilesPatterns(settings.getStringArray(CxxPlugin.C_FILES_PATTERNS_KEY));
    cxxConf.setHeaderFileSuffixes(settings.getStringArray(CxxPlugin.HEADER_FILE_SUFFIXES_KEY));
    cxxConf.setMissingIncludeWarningsEnabled(settings.getBoolean(CxxPlugin.MISSING_INCLUDE_WARN));

    String filePaths = settings.getString(CxxCompilerSensor.REPORT_PATH_KEY);
    if (filePaths != null && !"".equals(filePaths)) {
      List<File> reports = CxxReportSensor.getReports(settings, fs.baseDir(), CxxCompilerSensor.REPORT_PATH_KEY);
      cxxConf.setCompilationPropertiesWithBuildLog(reports,
        settings.getString(CxxCompilerSensor.PARSER_KEY_DEF),
        settings.getString(CxxCompilerSensor.REPORT_CHARSET_DEF));
    }

    return cxxConf;
  }

  private void save(Collection<SourceCode> squidSourceFiles, SensorContext context) {
    int violationsCount = 0;
    DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer(context, rules);

    for (SourceCode squidSourceFile : squidSourceFiles) {
      SourceFile squidFile = (SourceFile) squidSourceFile;
      File ioFile = new File(squidFile.getKey());
      InputFile inputFile = context.fileSystem().inputFile(context.fileSystem().predicates().is(ioFile));

      saveMeasures(inputFile, squidFile, context);
      saveFunctionAndClassComplexityDistribution(inputFile, squidFile, context);
      saveFilesComplexityDistribution(inputFile, squidFile, context);
      violationsCount += saveViolations(inputFile, squidFile, context);
      dependencyAnalyzer.addFile(inputFile, CxxParser.getIncludedFiles(ioFile), context);
    }

    context.<Integer>newMeasure().forMetric(CxxMetrics.SQUID).on(context.module()).withValue(violationsCount).save();
    dependencyAnalyzer.save(context);
  }

  private void saveMeasures(InputFile inputFile, SourceFile squidFile, SensorContext context) {
    context.<Integer>newMeasure().forMetric(CoreMetrics.FILES).on(inputFile).withValue(squidFile.getInt(CxxMetric.FILES)).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.NCLOC).on(inputFile).withValue(squidFile.getInt(CxxMetric.LINES_OF_CODE)).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.STATEMENTS).on(inputFile).withValue(squidFile.getInt(CxxMetric.STATEMENTS)).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.FUNCTIONS).on(inputFile).withValue(squidFile.getInt(CxxMetric.FUNCTIONS)).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.CLASSES).on(inputFile).withValue(squidFile.getInt(CxxMetric.CLASSES)).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.COMPLEXITY).on(inputFile).withValue(squidFile.getInt(CxxMetric.COMPLEXITY)).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.COMMENT_LINES).on(inputFile).withValue(squidFile.getInt(CxxMetric.COMMENT_LINES)).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.PUBLIC_API).on(inputFile).withValue(squidFile.getInt(CxxMetric.PUBLIC_API)).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.PUBLIC_UNDOCUMENTED_API).on(inputFile).withValue(squidFile.getInt(CxxMetric.PUBLIC_UNDOCUMENTED_API)).save();       
  }
  
  private void saveFunctionAndClassComplexityDistribution(InputFile inputFile, SourceFile squidFile, SensorContext context) {
    int complexityInFunctions = 0;
    int complexityInClasses = 0;

    RangeDistributionBuilder methodComplexityDistribution = new RangeDistributionBuilder(LIMITS_COMPLEXITY_METHODS);
    Collection<SourceCode> squidFunctionsInFile = scanner.getIndex().search(new QueryByParent(squidFile), new QueryByType(SourceFunction.class));
    for (SourceCode squidFunction : squidFunctionsInFile) {
      double functionComplexity = squidFunction.getDouble(CxxMetric.COMPLEXITY);
      complexityInFunctions += functionComplexity;
      if (squidFunction.getKey().contains("::")) {
        complexityInClasses += functionComplexity;
      }
      methodComplexityDistribution.add(functionComplexity);
    }
    
    context.<String>newMeasure().forMetric(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION).on(inputFile).withValue(methodComplexityDistribution.build()).save();

    Collection<SourceCode> classes = scanner.getIndex().search(new QueryByParent(squidFile), new QueryByType(SourceClass.class));
    for (SourceCode squidClass : classes) {
      double classComplexity = squidClass.getDouble(CxxMetric.COMPLEXITY);
      complexityInClasses += classComplexity;
    }

    context.<Integer>newMeasure().forMetric(CoreMetrics.COMPLEXITY_IN_CLASSES).on(inputFile).withValue(complexityInClasses).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.COMPLEXITY_IN_FUNCTIONS).on(inputFile).withValue(complexityInFunctions).save();
  }

  private void saveFilesComplexityDistribution(InputFile inputFile, SourceFile squidFile, SensorContext context) {    
    RangeDistributionBuilder fileComplexityDistribution = new RangeDistributionBuilder(LIMITS_COMPLEXITY_FILES);
    double complexity = squidFile.getDouble(CxxMetric.COMPLEXITY);
    fileComplexityDistribution.add(complexity);    
    context.<String>newMeasure().forMetric(CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION).on(inputFile).withValue(fileComplexityDistribution.build()).save();
  }

  private int saveViolations(InputFile inputFile, SourceFile squidFile, SensorContext sensorContext) {
    Collection<CheckMessage> messages = squidFile.getCheckMessages();
    int violationsCount = 0;
    if (messages != null) {
      for (CheckMessage message : messages) {
        int line = 1;
        if (message.getLine() != null && message.getLine() > 0) {
         line = message.getLine();
        }

        NewIssue newIssue = sensorContext.newIssue().forRule(RuleKey.of(CheckList.REPOSITORY_KEY, checks.ruleKey((SquidAstVisitor<Grammar>) message.getCheck()).rule()));
        NewIssueLocation location = newIssue.newLocation()
          .on(inputFile)
          .at(inputFile.selectLine(line))
          .message(message.getText(Locale.ENGLISH));

        newIssue.at(location);
        newIssue.save();
        
        // @todo - this will add a issue regardless of the save
        violationsCount++;     
      }
    }

    return violationsCount;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
