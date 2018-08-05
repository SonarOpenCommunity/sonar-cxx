/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.measures.Metric;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.CxxMetricsFactory;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.sensors.compiler.CxxCompilerSensor;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.JsonCompilationDatabase;
import org.sonar.cxx.sensors.visitors.CxxCpdVisitor;
import org.sonar.cxx.sensors.visitors.CxxFileLinesVisitor;
import org.sonar.cxx.sensors.visitors.CxxHighlighterVisitor;
import org.sonar.cxx.utils.CxxReportIssue;
import org.sonar.cxx.utils.CxxReportLocation;
import org.sonar.cxx.visitors.MultiLocatitionSquidCheck;
import org.sonar.squidbridge.AstScanner;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.api.CheckMessage;
import org.sonar.squidbridge.api.SourceCode;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.indexer.QueryByType;

import com.sonar.sslr.api.Grammar;

/**
 * {@inheritDoc}
 */
public class CxxSquidSensor implements Sensor {

  private static final Logger LOG = Loggers.get(CxxSquidSensor.class);
  public static final String DEFINES_KEY = "defines";
  public static final String INCLUDE_DIRECTORIES_KEY = "includeDirectories";
  public static final String ERROR_RECOVERY_KEY = "errorRecoveryEnabled";
  public static final String FORCE_INCLUDE_FILES_KEY = "forceIncludes";
  public static final String C_FILES_PATTERNS_KEY = "cFilesPatterns";
  public static final String MISSING_INCLUDE_WARN = "missingIncludeWarnings";
  public static final String JSON_COMPILATION_DATABASE_KEY = "jsonCompilationDatabase";

  public static final String CPD_IGNORE_LITERALS_KEY = "cpd.ignoreLiterals";
  public static final String CPD_IGNORE_IDENTIFIERS_KEY = "cpd.ignoreIdentifiers";

  public static final String KEY = "Squid";

  private final FileLinesContextFactory fileLinesContextFactory;
  private final CxxChecks checks;

  private final CxxLanguage language;

  /**
   * {@inheritDoc}
   */
  public CxxSquidSensor(CxxLanguage language,
    FileLinesContextFactory fileLinesContextFactory,
    CheckFactory checkFactory) {
    this(language, fileLinesContextFactory, checkFactory, null);
  }

  /**
   * {@inheritDoc}
   */
  public CxxSquidSensor(CxxLanguage language,
    FileLinesContextFactory fileLinesContextFactory,
    CheckFactory checkFactory,
    @Nullable CustomCxxRulesDefinition[] customRulesDefinition) {
    this.checks = CxxChecks.createCxxCheck(checkFactory)
      .addChecks(language.getRepositoryKey(), language.getChecks())
      .addCustomChecks(customRulesDefinition);
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.language = language;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name(language.getName() + " SquidSensor")
      .onlyOnLanguage(this.language.getKey())
      .onlyOnFileType(InputFile.Type.MAIN);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(SensorContext context) {

    List<SquidAstVisitor<Grammar>> visitors = new ArrayList<>((Collection) checks.all());
    visitors.add(new CxxHighlighterVisitor(context));
    visitors.add(new CxxFileLinesVisitor(language, fileLinesContextFactory, context));

    visitors.add(
      new CxxCpdVisitor(
        context,
        this.language.getBooleanOption(CPD_IGNORE_LITERALS_KEY).orElse(Boolean.FALSE),
        this.language.getBooleanOption(CPD_IGNORE_IDENTIFIERS_KEY).orElse(Boolean.FALSE)));

    CxxConfiguration cxxConf = createConfiguration(context.fileSystem(), context);
    AstScanner<Grammar> scanner = CxxAstScanner.create(this.language, cxxConf,
      visitors.toArray(new SquidAstVisitor[visitors.size()]));

    Iterable<InputFile> inputFiles = context.fileSystem().inputFiles(context.fileSystem().predicates()
      .and(context.fileSystem().predicates()
        .hasLanguage(this.language.getKey()), context.fileSystem().predicates()
        .hasType(InputFile.Type.MAIN)));

    List<File> files = new ArrayList<>();
    for (InputFile file : inputFiles) {
      files.add(new File(file.uri().getPath()));
    }

    scanner.scanFiles(files);

    Collection<SourceCode> squidSourceFiles = scanner.getIndex().search(new QueryByType(SourceFile.class));
    save(squidSourceFiles, context);
  }

  private CxxConfiguration createConfiguration(FileSystem fs, SensorContext context) {
    CxxConfiguration cxxConf = new CxxConfiguration(fs);
    cxxConf.setBaseDir(fs.baseDir().getAbsolutePath());
    String[] lines = this.language.getStringLinesOption(DEFINES_KEY);
    cxxConf.setDefines(lines);
    cxxConf.setIncludeDirectories(this.language.getStringArrayOption(INCLUDE_DIRECTORIES_KEY));
    cxxConf.setErrorRecoveryEnabled(this.language.getBooleanOption(ERROR_RECOVERY_KEY).orElse(Boolean.FALSE));
    cxxConf.setForceIncludeFiles(this.language.getStringArrayOption(FORCE_INCLUDE_FILES_KEY));
    // FIXME this.language.getStringArrayOption(C_FILES_PATTERNS_KEY) must be fixed
    // 1. it doesn't match C plugin (C_FILES_PATTERNS_KEY) as key makes sense
    //    for C++ plugin only
    // 2. event for C++ plugin this.language.getStringArrayOption(...) works wrong:
    //    it returns empty string if property is not set, but it have to return the
    //    default value instead
    //    For proper implemenation see CppLanguage::CppLanguage()
    //    or createStringArray(settings.getStringArray(C_FILES_PATTERNS_KEY), DEFAULT_C_FILES)
    cxxConf.setCFilesPatterns(this.language.getStringArrayOption(C_FILES_PATTERNS_KEY));
    cxxConf.setHeaderFileSuffixes(this.language.getHeaderFileSuffixes());
    cxxConf.setMissingIncludeWarningsEnabled(this.language.getBooleanOption(MISSING_INCLUDE_WARN)
      .orElse(Boolean.FALSE));
    cxxConf.setJsonCompilationDatabaseFile(this.language.getStringOption(JSON_COMPILATION_DATABASE_KEY)
      .orElse(null));

    if (cxxConf.getJsonCompilationDatabaseFile() != null) {
      try {
        new JsonCompilationDatabase(cxxConf, new File(cxxConf.getJsonCompilationDatabaseFile()));
      } catch (IOException e) {
        LOG.debug("Cannot access Json DB File: {}", e);
      }
    }

    String filePaths = this.language.getStringOption(CxxCompilerSensor.REPORT_PATH_KEY).orElse("");
    if (filePaths != null && !"".equals(filePaths)) {
      List<File> reports = CxxReportSensor.getReports(context.config(), fs.baseDir(),
        this.language.getPluginProperty(CxxCompilerSensor.REPORT_PATH_KEY));
      cxxConf.setCompilationPropertiesWithBuildLog(reports,
        this.language.getStringOption(CxxCompilerSensor.PARSER_KEY_DEF).orElse(""),
        this.language.getStringOption(CxxCompilerSensor.REPORT_CHARSET_DEF)
          .orElse(CxxCompilerSensor.DEFAULT_CHARSET_DEF));
    }

    return cxxConf;
  }

  private void save(Collection<SourceCode> squidSourceFiles, SensorContext context) {
    // don't publish metrics on modules, which were not analyzed
    // otherwise hierarchical multi-module projects will contain wrong metrics ( == 0)
    // see also AggregateMeasureComputer
    if (squidSourceFiles.isEmpty())
    {
      return;
    }

    int violationsCount = 0;

    for (SourceCode squidSourceFile : squidSourceFiles) {
      SourceFile squidFile = (SourceFile) squidSourceFile;
      File ioFile = new File(squidFile.getKey());
      InputFile inputFile = context.fileSystem().inputFile(context.fileSystem().predicates().is(ioFile));

      saveMeasures(inputFile, squidFile, context);
      violationsCount += saveViolations(inputFile, squidFile, context);
    }

    Metric<Integer> metric = this.language.getMetric(CxxMetricsFactory.Key.SQUID_SENSOR_ISSUES_KEY);
    context.<Integer>newMeasure()
      .forMetric(metric)
      .on(context.module())
      .withValue(violationsCount)
      .save();
  }

  private void saveMeasures(InputFile inputFile, SourceFile squidFile, SensorContext context) {
    // CORE METRICS
    context.<Integer>newMeasure().forMetric(CoreMetrics.FILES).on(inputFile)
      .withValue(squidFile.getInt(CxxMetric.FILES)).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.NCLOC).on(inputFile)
      .withValue(squidFile.getInt(CxxMetric.LINES_OF_CODE)).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.STATEMENTS).on(inputFile)
      .withValue(squidFile.getInt(CxxMetric.STATEMENTS)).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.FUNCTIONS).on(inputFile)
      .withValue(squidFile.getInt(CxxMetric.FUNCTIONS)).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.CLASSES).on(inputFile)
      .withValue(squidFile.getInt(CxxMetric.CLASSES)).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.COMPLEXITY).on(inputFile)
      .withValue(squidFile.getInt(CxxMetric.COMPLEXITY)).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.COGNITIVE_COMPLEXITY).on(inputFile)
        .withValue(squidFile.getInt(CxxMetric.COGNITIVE_COMPLEXITY)).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.COMMENT_LINES).on(inputFile)
      .withValue(squidFile.getInt(CxxMetric.COMMENT_LINES)).save();

    // CUSTOM METRICS
    // non-core metrics are not aggregated automatically,
    // see AggregateMeasureComputer

    // 1. PUBLIC API
    // PUBLIC_DOCUMENTED_API_DENSITY_KEY is calculated by means of
    // DensityMeasureComputer
    context.<Integer>newMeasure().forMetric(language.<Integer>getMetric(CxxMetricsFactory.Key.PUBLIC_API_KEY))
        .on(inputFile).withValue(squidFile.getInt(CxxMetric.PUBLIC_API)).save();
    context.<Integer>newMeasure()
        .forMetric(language.<Integer>getMetric(CxxMetricsFactory.Key.PUBLIC_UNDOCUMENTED_API_KEY)).on(inputFile)
        .withValue(squidFile.getInt(CxxMetric.PUBLIC_UNDOCUMENTED_API)).save();

    // 2. FUNCTION COMPLEXITY
    // COMPLEX_FUNCTIONS_PERC_KEY and COMPLEX_FUNCTIONS_LOC_PERC_KEY
    // are calculated by means of by means of DensityMeasureComputer
    context.<Integer>newMeasure().forMetric(language.<Integer>getMetric(CxxMetricsFactory.Key.COMPLEX_FUNCTIONS_KEY))
        .on(inputFile).withValue(squidFile.getInt(CxxMetric.COMPLEX_FUNCTIONS)).save();
    context.<Integer>newMeasure()
        .forMetric(language.<Integer>getMetric(CxxMetricsFactory.Key.COMPLEX_FUNCTIONS_LOC_KEY)).on(inputFile)
        .withValue(squidFile.getInt(CxxMetric.COMPLEX_FUNCTIONS_LOC)).save();

    // 2. FUNCTION SIZE
    // BIG_FUNCTIONS_PERC_KEY and BIG_FUNCTIONS_LOC_PERC_KEY
    // are calculated by means of by means of DensityMeasureComputer
    context.<Integer>newMeasure().forMetric(language.<Integer>getMetric(CxxMetricsFactory.Key.LOC_IN_FUNCTIONS_KEY))
    .on(inputFile).withValue(squidFile.getInt(CxxMetric.LOC_IN_FUNCTIONS)).save();
    context.<Integer>newMeasure().forMetric(language.<Integer>getMetric(CxxMetricsFactory.Key.BIG_FUNCTIONS_KEY))
        .on(inputFile).withValue(squidFile.getInt(CxxMetric.BIG_FUNCTIONS)).save();
    context.<Integer>newMeasure().forMetric(language.<Integer>getMetric(CxxMetricsFactory.Key.BIG_FUNCTIONS_LOC_KEY))
        .on(inputFile).withValue(squidFile.getInt(CxxMetric.BIG_FUNCTIONS_LOC)).save();
  }

  private int saveViolations(InputFile inputFile, SourceFile squidFile, SensorContext sensorContext) {
    int violationsCount = 0;
    if (squidFile.hasCheckMessages()) {
      for (CheckMessage message : squidFile.getCheckMessages()) {
        int line = 1;
        if (message.getLine() != null && message.getLine() > 0) {
          line = message.getLine();
        }

        NewIssue newIssue = sensorContext.newIssue().forRule(RuleKey.of(this.language.getRepositoryKey(),
            checks.ruleKey((SquidAstVisitor<Grammar>) message.getCheck()).rule()));
        NewIssueLocation location = newIssue.newLocation().on(inputFile).at(inputFile.selectLine(line))
            .message(message.getText(Locale.ENGLISH));

        newIssue.at(location);
        newIssue.save();
        ++violationsCount;
      }
    }

    if (MultiLocatitionSquidCheck.hasMultiLocationCheckMessages(squidFile)) {
      for (CxxReportIssue issue : MultiLocatitionSquidCheck.getMultiLocationCheckMessages(squidFile)) {
        final NewIssue newIssue = sensorContext.newIssue()
            .forRule(RuleKey.of(language.getRepositoryKey(), issue.getRuleId()));
        int locationNr = 0;
        for (CxxReportLocation location : issue.getLocations()) {
          final Integer line = Integer.valueOf(location.getLine());
          final NewIssueLocation newIssueLocation = newIssue.newLocation().on(inputFile).at(inputFile.selectLine(line))
              .message(location.getInfo());
          if (locationNr == 0) {
            newIssue.at(newIssueLocation);
          } else {
            newIssue.addLocation(newIssueLocation);
          }
          ++locationNr;
        }
        newIssue.save();
        ++violationsCount;
      }
      MultiLocatitionSquidCheck.eraseMultilineCheckMessages(squidFile);
    }

    return violationsCount;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
