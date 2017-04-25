/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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
package org.sonar.cxx.sensors.squid;

import java.io.File;
import java.io.IOException;
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
import org.sonar.api.measures.CoreMetrics;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.squidbridge.AstScanner;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.api.CheckMessage;
import org.sonar.squidbridge.api.SourceCode;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.api.SourceFunction;
import org.sonar.squidbridge.api.SourceClass;
import org.sonar.squidbridge.indexer.QueryByParent;
import org.sonar.squidbridge.indexer.QueryByType;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sonar.sslr.api.Grammar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.ce.measure.RangeDistributionBuilder;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.measures.Metric;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.parser.CxxParser;
import org.sonar.cxx.sensors.compiler.CxxCompilerSensor;
import org.sonar.cxx.sensors.coverage.CxxCoverageCache;
import org.sonar.cxx.sensors.coverage.CxxCoverageSensor;
import org.sonar.cxx.sensors.utils.CxxMetrics;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.JsonCompilationDatabase;
import org.sonar.cxx.sensors.visitors.CxxCpdVisitor;
import org.sonar.cxx.sensors.visitors.CxxHighlighterVisitor;
import org.sonar.cxx.sensors.visitors.FileLinesVisitor;

/**
 * {@inheritDoc}
 */
public class CxxSquidSensor implements Sensor {

  private static final Logger LOG = Loggers.get(CxxSquidSensor.class);
  public static final String SOURCE_FILE_SUFFIXES_KEY = "suffixes.sources";
  public static final String HEADER_FILE_SUFFIXES_KEY = "suffixes.headers";
  public static final String DEFINES_KEY = "defines";
  public static final String INCLUDE_DIRECTORIES_KEY = "includeDirectories";
  public static final String ERROR_RECOVERY_KEY = "errorRecoveryEnabled";
  public static final String FORCE_INCLUDE_FILES_KEY = "forceIncludes";
  public static final String C_FILES_PATTERNS_KEY = "cFilesPatterns";
  public static final String MISSING_INCLUDE_WARN = "missingIncludeWarnings";
  public static final String JSON_COMPILATION_DATABASE_KEY = "jsonCompilationDatabase";
  public static final String SCAN_ONLY_SPECIFIED_SOURCES_KEY = "scanOnlySpecifiedSources";
  
  public static final String CPD_IGNORE_LITERALS_KEY = "cpd.ignoreLiterals";
  public static final String CPD_IGNORE_IDENTIFIERS_KEY = "cpd.ignoreIdentifiers";
  
  private static final Number[] LIMITS_COMPLEXITY_METHODS = {1, 2, 4, 6, 8, 10, 12, 20, 30};
  private static final Number[] LIMITS_COMPLEXITY_FILES = {0, 5, 10, 20, 30, 60, 90};
  public static final String KEY = "Squid";

  private final FileLinesContextFactory fileLinesContextFactory;
  private final CxxChecks checks;
  private ActiveRules rules;

  private AstScanner<Grammar> scanner;
  private final CxxLanguage language;
  private final CxxCoverageCache cache;
    
  /**
   * {@inheritDoc}
   */
  public CxxSquidSensor(CxxLanguage language,
          FileLinesContextFactory fileLinesContextFactory,
          CheckFactory checkFactory,
          ActiveRules rules,
          @Nullable CxxCoverageCache coverageCache) {
    this(language, fileLinesContextFactory, checkFactory, rules, null, coverageCache);    
  }
  
  /**
   * {@inheritDoc}
   */
  public CxxSquidSensor(CxxLanguage language,
          FileLinesContextFactory fileLinesContextFactory,
          CheckFactory checkFactory,
          ActiveRules rules,
          @Nullable CustomCxxRulesDefinition[] customRulesDefinition,
          @Nullable CxxCoverageCache coverageCache) {
    this.checks = CxxChecks.createCxxCheck(checkFactory)
      .addChecks(language.getRepositoryKey(), language.getChecks())
      .addCustomChecks(customRulesDefinition);
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.rules = rules;
    this.language = language;
           
    if (coverageCache == null) {
      this.cache = new CxxCoverageCache();
    } else {
      this.cache = coverageCache;
    }
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(this.language.getKey()).name(language.getName() + " SquidSensor");
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(SensorContext context) {       
    Map<InputFile, Set<Integer>> linesOfCode = new HashMap<>();
        
    List<SquidAstVisitor<Grammar>> visitors = new ArrayList<>((Collection) checks.all());
    visitors.add(new CxxHighlighterVisitor(context));
    visitors.add(new FileLinesVisitor(fileLinesContextFactory, context.fileSystem(), linesOfCode));
    visitors.add(
            new CxxCpdVisitor(
                    context,
                    this.language.getBooleanOption(CPD_IGNORE_LITERALS_KEY),
                    this.language.getBooleanOption(CPD_IGNORE_IDENTIFIERS_KEY)));
    
    CxxConfiguration cxxConf = createConfiguration(context.fileSystem());
    this.scanner = CxxAstScanner.create(this.language, cxxConf, context,
      visitors.toArray(new SquidAstVisitor[visitors.size()]));

    List<File> files;
    if (cxxConf.isScanOnlySpecifiedSources()) {
      files = cxxConf.getCompilationUnitSourceFiles();
    } else {
      Iterable<InputFile> inputFiles = context.fileSystem().inputFiles(context.fileSystem().predicates()
              .and(context.fileSystem().predicates()
                      .hasLanguage(this.language.getKey()), context.fileSystem().predicates()
                              .hasType(InputFile.Type.MAIN)));

      files = new ArrayList<>();
      for(InputFile file : inputFiles) {
        files.add(file.file());
      }
    }
    scanner.scanFiles(files);
    
    (new CxxCoverageSensor(this.cache, this.language)).execute(context, linesOfCode);
    
    Collection<SourceCode> squidSourceFiles = scanner.getIndex().search(new QueryByType(SourceFile.class));
    save(squidSourceFiles, context);
  }

  private CxxConfiguration createConfiguration(FileSystem fs) {
    CxxConfiguration cxxConf = new CxxConfiguration(fs, this.language);
    cxxConf.setBaseDir(fs.baseDir().getAbsolutePath());
    String[] lines = this.language.getStringLinesOption(DEFINES_KEY);
    cxxConf.setDefines(lines);
    cxxConf.setIncludeDirectories(this.language.getStringArrayOption(INCLUDE_DIRECTORIES_KEY));
    cxxConf.setErrorRecoveryEnabled(this.language.getBooleanOption(ERROR_RECOVERY_KEY));
    cxxConf.setForceIncludeFiles(this.language.getStringArrayOption(FORCE_INCLUDE_FILES_KEY));
    cxxConf.setCFilesPatterns(this.language.getStringArrayOption(C_FILES_PATTERNS_KEY));
    cxxConf.setHeaderFileSuffixes(this.language.getStringArrayOption(HEADER_FILE_SUFFIXES_KEY));
    cxxConf.setMissingIncludeWarningsEnabled(this.language.getBooleanOption(MISSING_INCLUDE_WARN));
    cxxConf.setJsonCompilationDatabaseFile(this.language.getStringOption(JSON_COMPILATION_DATABASE_KEY));
    cxxConf.setScanOnlySpecifiedSources(this.language.getBooleanOption(SCAN_ONLY_SPECIFIED_SOURCES_KEY));

    if (cxxConf.getJsonCompilationDatabaseFile() != null) {
      try {
        new JsonCompilationDatabase(cxxConf, new File(cxxConf.getJsonCompilationDatabaseFile()));
      } catch (IOException e) {
        LOG.debug("Cannot access Json DB File: {}", e);
      }
    }

    String filePaths = this.language.getStringOption(CxxCompilerSensor.REPORT_PATH_KEY);
    if (filePaths != null && !"".equals(filePaths)) {
      List<File> reports = CxxReportSensor.getReports(this.language, fs.baseDir(), CxxCompilerSensor.REPORT_PATH_KEY);
      cxxConf.setCompilationPropertiesWithBuildLog(reports,
        this.language.getStringOption(CxxCompilerSensor.PARSER_KEY_DEF),
        this.language.getStringOption(CxxCompilerSensor.REPORT_CHARSET_DEF));
    }

    return cxxConf;
  }

  private void save(Collection<SourceCode> squidSourceFiles, SensorContext context) {
    int violationsCount = 0;
    DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer(context, rules, this.language);

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

    String metricKey = CxxMetrics.GetKey(KEY, language);
    Metric metric = this.language.getMetric(metricKey);

    if (metric != null) {
      context.<Integer>newMeasure()
        .forMetric(metric)
        .on(context.module())
        .withValue(violationsCount)
        .save();               
    }
    
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

        NewIssue newIssue = sensorContext
                .newIssue()
                .forRule(RuleKey.of(this.language.getRepositoryKey(), checks.ruleKey((SquidAstVisitor<Grammar>) message.getCheck()).rule()));
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
