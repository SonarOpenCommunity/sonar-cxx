/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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
package org.sonar.plugins.cxx;

import com.sonar.cxx.sslr.api.Grammar;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import org.sonar.api.PropertyType;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scanner.sensor.ProjectSensor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxMetrics;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.checks.CheckList;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.config.MsBuild;
import org.sonar.cxx.sensors.utils.CxxUtils;
import org.sonar.cxx.squidbridge.SquidAstVisitor;
import org.sonar.cxx.squidbridge.api.SourceCode;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.squidbridge.indexer.QueryByType;
import org.sonar.cxx.visitors.CxxCpdVisitor;
import org.sonar.cxx.visitors.CxxHighlighterVisitor;
import org.sonar.cxx.visitors.CxxPublicApiVisitor;
import org.sonar.cxx.visitors.MultiLocatitionSquidCheck;

/**
 * {@inheritDoc}
 */
public class CxxSquidSensor implements ProjectSensor {

  public static final String SQUID_DISABLED_KEY = "sonar.cxx.squid.disabled";
  public static final String DEFINES_KEY = "sonar.cxx.defines";
  public static final String INCLUDE_DIRECTORIES_KEY = "sonar.cxx.includeDirectories";
  public static final String ERROR_RECOVERY_KEY = "sonar.cxx.errorRecoveryEnabled";
  public static final String FORCE_INCLUDES_KEY = "sonar.cxx.forceIncludes";
  public static final String JSON_COMPILATION_DATABASE_KEY = "sonar.cxx.jsonCompilationDatabase";
  public static final String JSON_COMPILATION_DATABASE_ONLY_CONTAINED_FILES_KEY
                               = "sonar.cxx.jsonCompilationDatabase.analyzeOnlyContainedFiles";

  public static final String FUNCTION_COMPLEXITY_THRESHOLD_KEY = "sonar.cxx.metric.func.complexity.threshold";
  public static final String FUNCTION_SIZE_THRESHOLD_KEY = "sonar.cxx.metric.func.size.threshold";

  public static final String CPD_IGNORE_LITERALS_KEY = "sonar.cxx.metric.cpd.ignoreLiterals";
  public static final String CPD_IGNORE_IDENTIFIERS_KEY = "sonar.cxx.metric.cpd.ignoreIdentifiers";

  private static final Logger LOG = Loggers.get(CxxSquidSensor.class);

  private final FileLinesContextFactory fileLinesContextFactory;
  private final CxxChecks checks;
  private final NoSonarFilter noSonarFilter;

  private SensorContext context;

  /**
   * {@inheritDoc}
   */
  public CxxSquidSensor(FileLinesContextFactory fileLinesContextFactory,
                        CheckFactory checkFactory,
                        NoSonarFilter noSonarFilter) {
    this(fileLinesContextFactory, checkFactory, noSonarFilter, null);
  }

  /**
   * {@inheritDoc}
   */
  public CxxSquidSensor(FileLinesContextFactory fileLinesContextFactory,
                        CheckFactory checkFactory,
                        NoSonarFilter noSonarFilter,
                        @Nullable CustomCxxRulesDefinition[] customRulesDefinition) {
    this.checks = CxxChecks.createCxxCheck(checkFactory)
      .addChecks(CheckList.REPOSITORY_KEY, CheckList.getChecks())
      .addCustomChecks(customRulesDefinition);
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.noSonarFilter = noSonarFilter;
  }

  public static List<PropertyDefinition> properties() {
    return Collections.unmodifiableList(Arrays.asList(
      PropertyDefinition.builder(INCLUDE_DIRECTORIES_KEY)
        .multiValues(true)
        .name("(2.2) Include Directories")
        .description(
          "Comma-separated list of directories where the preprocessor looks for include files."
            + " The path may be either absolute or relative to the project base directory."
            + " In the SonarQube UI, enter one entry per field."
        )
        .category("CXX")
        .subCategory("(2) Preprocessor")
        .onQualifiers(Qualifiers.PROJECT)
        .build(),
      PropertyDefinition.builder(FORCE_INCLUDES_KEY)
        .multiValues(true)
        .category("CXX")
        .subCategory("(2) Preprocessor")
        .name("(2.3) Force Includes")
        .description(
          "Comma-separated list of include files implicitly inserted at the beginning of each source file."
            + " This has the same effect as specifying the file with double quotation marks in an `#include` directive"
            + " on the first line of every source file. If you add multiple files they are included in the order they"
            + " are listed from left to right. The path may be either absolute or relative to the"
            + " project base directory."
            + " In the SonarQube UI, enter one entry per field."
        )
        .onQualifiers(Qualifiers.PROJECT)
        .build(),
      PropertyDefinition.builder(SQUID_DISABLED_KEY)
        .defaultValue(Boolean.FALSE.toString())
        .name("Disable Squid sensor")
        .description(
          "Disable parsing of source code, syntax hightligthing and metric generation."
            + " The source files are still indexed, reports can be read and their results displayed."
            + " Turning off will speed up reading of source files."
        )
        .category("CXX")
        .subCategory("(1) General")
        .onQualifiers(Qualifiers.PROJECT)
        .type(PropertyType.BOOLEAN)
        .build(),
      PropertyDefinition.builder(DEFINES_KEY)
        .name("(2.1) Macros")
        .description(
          "List of macros to be used by the preprocessor during analysis. Enter one macro per line."
            + " The syntax is the same as `#define` directives, except for the `#define` keyword itself."
        )
        .category("CXX")
        .subCategory("(2) Preprocessor")
        .onQualifiers(Qualifiers.PROJECT)
        .type(PropertyType.TEXT)
        .build(),
      PropertyDefinition.builder(ERROR_RECOVERY_KEY)
        .defaultValue(Boolean.TRUE.toString())
        .name("Parse Error Recovery")
        .description(
          "Defines the mode for error handling of report files and parsing errors."
            + " `False` (strict) terminates after an error or `True` (tolerant) continues."
        )
        .category("CXX")
        .subCategory("(1) General")
        .onQualifiers(Qualifiers.PROJECT)
        .type(PropertyType.BOOLEAN)
        .build(),
      PropertyDefinition.builder(MsBuild.REPORT_PATH_KEY)
        .name("(2.6) Path(s) to MSBuild Log(s)")
        .description(
          "Read one ore more MSBuild .LOG files to automatically extract the required macros `sonar.cxx.defines`"
            + " and include directories `sonar.cxx.includeDirectories`. The path may be either absolute or relative"
            + " to the project base directory."
            + " In the SonarQube UI, enter one entry per field."
        )
        .category("CXX")
        .subCategory("(2) Preprocessor")
        .onQualifiers(Qualifiers.PROJECT)
        .multiValues(true)
        .build(),
      PropertyDefinition.builder(MsBuild.REPORT_ENCODING_DEF)
        .defaultValue(MsBuild.DEFAULT_ENCODING_DEF)
        .name("(2.7) MSBuild Log Encoding")
        .description(
          "Defines the encoding to be used to read the files from `sonar.cxx.msbuild.reportPaths` (default is `UTF-8`)."
        )
        .category("CXX")
        .subCategory("(2) Preprocessor")
        .onQualifiers(Qualifiers.PROJECT)
        .build(),
      PropertyDefinition.builder(JSON_COMPILATION_DATABASE_KEY)
        .category("CXX")
        .subCategory("(2) Preprocessor")
        .name("(2.4) JSON Compilation Database")
        .description(
          "Read a JSON Compilation Database file to automatically extract the required macros `sonar.cxx.defines`"
            + " and include directories `sonar.cxx.includeDirectories` from a file. The path may be either absolute"
            + " or relative to the project base directory."
        )
        .onQualifiers(Qualifiers.PROJECT)
        .build(),
      PropertyDefinition.builder(JSON_COMPILATION_DATABASE_ONLY_CONTAINED_FILES_KEY)
        .defaultValue(Boolean.FALSE.toString())
        .category("CXX")
        .subCategory("(2) Preprocessor")
        .name("(2.5) JSON Compilation Database analyze only contained files")
        .description(
          "If 'analyzeOnlyContainedFiles=True' is used, the analyzed files will be limited to the files contained"
            + " in the 'JSON Compilation Database' file - the intersection of the files configured via"
            + " 'sonar.projectBaseDir' and the files contained in the 'JSON Compilation Database' file"
            + " (default is False)."
        )
        .onQualifiers(Qualifiers.PROJECT)
        .type(PropertyType.BOOLEAN)
        .build(),
      PropertyDefinition.builder(CxxPublicApiVisitor.API_FILE_SUFFIXES_KEY)
        .defaultValue(CxxPublicApiVisitor.API_DEFAULT_FILE_SUFFIXES)
        .name("Public API File suffixes")
        .multiValues(true)
        .description(
          "Comma-separated list of suffixes for files to be searched for API comments and to create API metrics."
            + " In the SonarQube UI, enter one entry per field."
        )
        .category("CXX")
        .subCategory("(3) Metrics")
        .onQualifiers(Qualifiers.PROJECT)
        .build(),
      PropertyDefinition.builder(FUNCTION_COMPLEXITY_THRESHOLD_KEY)
        .defaultValue("10")
        .name("Complex Functions ...")
        .description(
          "The parameter defines the threshold for `Complex Functions ...`."
            + " Functions and methods with a higher cyclomatic complexity are classified as `complex`."
        )
        .category("CXX")
        .subCategory("(3) Metrics")
        .onQualifiers(Qualifiers.PROJECT)
        .type(PropertyType.INTEGER)
        .build(),
      PropertyDefinition.builder(FUNCTION_SIZE_THRESHOLD_KEY)
        .defaultValue("20")
        .name("Big Functions ...")
        .description(
          "The parameter defines the threshold for `Big Functions ...`."
            + " Functions and methods with more lines of code are classified as `big`."
        )
        .category("CXX")
        .subCategory("(3) Metrics")
        .onQualifiers(Qualifiers.PROJECT)
        .type(PropertyType.INTEGER)
        .build(),
      PropertyDefinition.builder(CPD_IGNORE_LITERALS_KEY)
        .defaultValue(Boolean.FALSE.toString())
        .name("Ignores Literal Value Differences")
        .description(
          "Configure the metrics `Duplications` (Copy Paste Detection). `True` ignores literal"
            + " (numbers, characters and strings) value differences when evaluating a duplicate block. This means"
            + " that e.g. `foo=42;` and `foo=43;` will be seen as equivalent."
        )
        .category("CXX")
        .subCategory("(4) Duplications")
        .onQualifiers(Qualifiers.PROJECT)
        .type(PropertyType.BOOLEAN)
        .build(),
      PropertyDefinition.builder(CPD_IGNORE_IDENTIFIERS_KEY)
        .defaultValue(Boolean.FALSE.toString())
        .name("Ignores Identifier Value Differences")
        .description(
          "Configure the metrics `Duplications` (Copy Paste Detection). `True` ignores identifier value differences"
            + " when evaluating a duplicate block e.g. variable names, methods names, and so forth."
        )
        .category("CXX")
        .subCategory("(4) Duplications")
        .onQualifiers(Qualifiers.PROJECT)
        .type(PropertyType.BOOLEAN)
        .build()
    ));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("CXX")
      .onlyOnLanguage("cxx")
      .onlyOnFileType(InputFile.Type.MAIN)
      .onlyWhenConfiguration(conf -> !conf.getBoolean(SQUID_DISABLED_KEY).orElse(false));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(SensorContext context) {
    this.context = context;

    // add visitor only if corresponding rule is active
    var visitors = new ArrayList<SquidAstVisitor<Grammar>>();
    for (var check : checks.all()) {
      RuleKey key = checks.ruleKey(check);
      if (key != null) {
        if (context.activeRules().find(key) != null) {
          visitors.add(check);
        }
      }
    }

    var squidConfig = createConfiguration();
    var scanner = CxxAstScanner.create(squidConfig, visitors.toArray(new SquidAstVisitor[visitors.size()]));

    Iterable<InputFile> inputFiles = getInputFiles(context, squidConfig);
    scanner.scanInputFiles(inputFiles);

    Collection<SourceCode> squidSourceFiles = scanner.getIndex().search(new QueryByType(SourceFile.class));
    save(squidSourceFiles);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  private String[] stripValue(String key, String regex) {
    Optional<String> value = context.config().get(key);
    if (value.isPresent()) {
      var PATTERN = Pattern.compile(regex);
      return PATTERN.split(value.get(), -1);
    }
    return new String[0];
  }

  private CxxSquidConfiguration createConfiguration() {
    var squidConfig = new CxxSquidConfiguration(context.fileSystem().baseDir().getAbsolutePath(),
                                                context.fileSystem().encoding());

    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.ERROR_RECOVERY_ENABLED,
                    context.config().get(ERROR_RECOVERY_KEY));
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.CPD_IGNORE_LITERALS,
                    context.config().get(CPD_IGNORE_LITERALS_KEY));
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.CPD_IGNORE_IDENTIFIERS,
                    context.config().get(CPD_IGNORE_IDENTIFIERS_KEY));
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.FUNCTION_COMPLEXITY_THRESHOLD,
                    context.config().get(FUNCTION_COMPLEXITY_THRESHOLD_KEY));
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.FUNCTION_SIZE_THRESHOLD,
                    context.config().get(FUNCTION_SIZE_THRESHOLD_KEY));
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.API_FILE_SUFFIXES,
                    context.config().getStringArray(CxxPublicApiVisitor.API_FILE_SUFFIXES_KEY));
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.JSON_COMPILATION_DATABASE,
                    context.config().get(JSON_COMPILATION_DATABASE_KEY));

    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.DEFINES,
                    stripValue(DEFINES_KEY, "\\R"));
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.FORCE_INCLUDES,
                    context.config().getStringArray(FORCE_INCLUDES_KEY));
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.INCLUDE_DIRECTORIES,
                    context.config().getStringArray(INCLUDE_DIRECTORIES_KEY));

    squidConfig.readJsonCompilationDb();

    if (context.config().hasKey(MsBuild.REPORT_PATH_KEY)) {
      List<File> logFiles = CxxUtils.getFiles(context, MsBuild.REPORT_PATH_KEY);
      squidConfig.readMsBuildFiles(logFiles, context.config().get(MsBuild.REPORT_ENCODING_DEF)
                                   .orElse(MsBuild.DEFAULT_ENCODING_DEF));
    }

    return squidConfig;
  }

  private Iterable<InputFile> getInputFiles(SensorContext context, CxxSquidConfiguration squidConfig) {
    Iterable<InputFile> inputFiles = context.fileSystem().inputFiles(
      context.fileSystem().predicates().and(
        context.fileSystem().predicates().hasLanguage("cxx"),
        context.fileSystem().predicates().hasType(InputFile.Type.MAIN)
      )
    );

    if (context.config().hasKey(JSON_COMPILATION_DATABASE_KEY)
          && context.config().getBoolean(JSON_COMPILATION_DATABASE_ONLY_CONTAINED_FILES_KEY).orElse(Boolean.FALSE)) {
      // if the source of the configuration is JSON Compilation Database and analyzeOnlyContainedFiles=True,
      // then analyze only the files contained in the db.
      var inputFilesInConfig = squidConfig.getFiles();
      var result = StreamSupport.stream(inputFiles.spliterator(), false)
        .filter(f -> inputFilesInConfig.contains(Path.of(f.uri())))
        .collect(Collectors.toList());
      inputFiles = result;

      LOG.info("Analyze only files contained in 'JSON Compilation Database': {} files", result.size());
      if (result.isEmpty()) {
        LOG.error(
          "No files are analyzed, check the settings of 'sonar.projectBaseDir' and 'sonar.cxx.jsonCompilationDatabase'."
        );
      }
    }

    return inputFiles;
  }

  private void save(Collection<SourceCode> sourceCodeFiles) {
    for (var sourceCodeFile : sourceCodeFiles) {
      try {
        var sourceFile = (SourceFile) sourceCodeFile;
        var ioFile = new File(sourceFile.getKey());
        InputFile inputFile = context.fileSystem().inputFile(context.fileSystem().predicates().is(ioFile));

        saveMeasures(inputFile, sourceFile);
        saveViolations(inputFile, sourceFile);
        saveFileLinesContext(inputFile, sourceFile);
        saveCpdTokens(inputFile, sourceFile);
        saveHighlighting(inputFile, sourceFile);
      } catch (IllegalStateException e) {
        var msg = "Cannot save all measures for file '" + sourceCodeFile.getKey() + "'";
        CxxUtils.validateRecovery(msg, e, context.config());
      }
    }
  }

  private void saveMeasures(InputFile inputFile, SourceFile sourceFile) {

    // NOSONAR
    noSonarFilter.noSonarInFile(inputFile, sourceFile.getNoSonarTagLines());

    // CORE METRICS
    saveMetric(inputFile, CoreMetrics.NCLOC, sourceFile.getInt(CxxMetric.LINES_OF_CODE));
    saveMetric(inputFile, CoreMetrics.STATEMENTS, sourceFile.getInt(CxxMetric.STATEMENTS));
    saveMetric(inputFile, CoreMetrics.FUNCTIONS, sourceFile.getInt(CxxMetric.FUNCTIONS));
    saveMetric(inputFile, CoreMetrics.CLASSES, sourceFile.getInt(CxxMetric.CLASSES));
    saveMetric(inputFile, CoreMetrics.COMPLEXITY, sourceFile.getInt(CxxMetric.COMPLEXITY));
    saveMetric(inputFile, CoreMetrics.COGNITIVE_COMPLEXITY, sourceFile.getInt(CxxMetric.COGNITIVE_COMPLEXITY));
    saveMetric(inputFile, CoreMetrics.COMMENT_LINES, sourceFile.getInt(CxxMetric.COMMENT_LINES));

    // CUSTOM METRICS
    //
    // non-core metrics are not aggregated automatically, see AggregateMeasureComputer
    // below metrics are calculated by means of DensityMeasureComputer
    //
    // 1. PUBLIC API
    saveMetric(inputFile, CxxMetrics.PUBLIC_API, sourceFile.getInt(CxxMetric.PUBLIC_API));
    saveMetric(inputFile, CxxMetrics.PUBLIC_UNDOCUMENTED_API, sourceFile.getInt(CxxMetric.PUBLIC_UNDOCUMENTED_API));

    // 2. FUNCTION COMPLEXITY
    saveMetric(inputFile, CxxMetrics.COMPLEX_FUNCTIONS, sourceFile.getInt(CxxMetric.COMPLEX_FUNCTIONS));
    saveMetric(inputFile, CxxMetrics.COMPLEX_FUNCTIONS_LOC, sourceFile.getInt(CxxMetric.COMPLEX_FUNCTIONS_LOC));

    // 3. FUNCTION SIZE
    saveMetric(inputFile, CxxMetrics.LOC_IN_FUNCTIONS, sourceFile.getInt(CxxMetric.LOC_IN_FUNCTIONS));
    saveMetric(inputFile, CxxMetrics.BIG_FUNCTIONS, sourceFile.getInt(CxxMetric.BIG_FUNCTIONS));
    saveMetric(inputFile, CxxMetrics.BIG_FUNCTIONS_LOC, sourceFile.getInt(CxxMetric.BIG_FUNCTIONS_LOC));
  }

  private void saveViolations(InputFile inputFile, SourceFile sourceFile) {
    if (sourceFile.hasCheckMessages()) {
      for (var message : sourceFile.getCheckMessages()) {
        var line = 1;
        if (message.getLine() != null && message.getLine() > 0) {
          line = message.getLine();
        }

        RuleKey ruleKey = checks.ruleKey((SquidAstVisitor<Grammar>) message.getCheck());
        if (ruleKey != null) {
          var newIssue = context.newIssue().forRule(RuleKey.of(CheckList.REPOSITORY_KEY, ruleKey.rule()));
          var location = newIssue.newLocation()
            .on(inputFile)
            .at(inputFile.selectLine(line))
            .message(message.getText(Locale.ENGLISH));

          newIssue.at(location);
          newIssue.save();
        } else {
          LOG.debug("Unknown rule key: %s", message);
        }
      }
    }

    if (MultiLocatitionSquidCheck.hasMultiLocationCheckMessages(sourceFile)) {
      for (var issue : MultiLocatitionSquidCheck.getMultiLocationCheckMessages(sourceFile)) {
        var newIssue = context.newIssue().forRule(RuleKey.of(CheckList.REPOSITORY_KEY, issue.getRuleId()));
        var locationNr = 0;
        for (var location : issue.getLocations()) {
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
      }
      MultiLocatitionSquidCheck.eraseMultilineCheckMessages(sourceFile);
    }
  }

  private void saveFileLinesContext(InputFile inputFile, SourceFile sourceFile) {
    // measures for the lines of file
    var fileLinesContext = fileLinesContextFactory.createFor(inputFile);
    List<Integer> linesOfCode = (List<Integer>) sourceFile.getData(CxxMetric.NCLOC_DATA);
    linesOfCode.stream().sequential().distinct().forEach((line) -> {
      try {
        fileLinesContext.setIntValue(CoreMetrics.NCLOC_DATA_KEY, line, 1);
      } catch (IllegalArgumentException | IllegalStateException e) {
        // ignore errors: parsing errors could lead to wrong location data
        LOG.debug("NCLOC error in file '{}' at line:{}", inputFile.filename(), line);
      }
    });
    List<Integer> executableLines = (List<Integer>) sourceFile.getData(CxxMetric.EXECUTABLE_LINES_DATA);
    executableLines.stream().sequential().distinct().forEach((line) -> {
      try {
        fileLinesContext.setIntValue(CoreMetrics.EXECUTABLE_LINES_DATA_KEY, line, 1);
      } catch (IllegalArgumentException | IllegalStateException e) {
        // ignore errors: parsing errors could lead to wrong location data
        LOG.debug("EXECUTABLE LINES error in file '{}' at line:{}", inputFile.filename(), line);
      }
    });
    fileLinesContext.save();
  }

  private void saveCpdTokens(InputFile inputFile, SourceFile sourceFile) {
    NewCpdTokens cpdTokens = context.newCpdTokens().onFile(inputFile);

    List<CxxCpdVisitor.CpdToken> data = (List<CxxCpdVisitor.CpdToken>) sourceFile.getData(CxxMetric.CPD_TOKENS_DATA);
    data.forEach((item) -> {
      try {
        TextRange range = inputFile.newRange(item.startLine, item.startCol, item.endLine, item.endCol);
        cpdTokens.addToken(range, item.token);
      } catch (IllegalArgumentException | IllegalStateException e) {
        // ignore range errors: parsing errors could lead to wrong location data
        LOG.debug("CPD error in file '{}' at line:{}, column:{}", inputFile.filename(), item.startLine, item.startCol);
      }
    });

    cpdTokens.save();
  }

  private void saveHighlighting(InputFile inputFile, SourceFile sourceFile) {
    NewHighlighting newHighlighting = context.newHighlighting().onFile(inputFile);

    List<CxxHighlighterVisitor.Highlight> data = (List<CxxHighlighterVisitor.Highlight>) sourceFile.getData(
      CxxMetric.HIGHLIGTHING_DATA);
    data.forEach((item) -> {
      try {
        newHighlighting.highlight(item.startLine, item.startLineOffset, item.endLine, item.endLineOffset,
                                  TypeOfText.forCssClass(item.typeOfText));
      } catch (IllegalArgumentException | IllegalStateException e) {
        // ignore highlight errors: parsing errors could lead to wrong location data
        LOG.debug("Highlighting error in file '{}' at start:{}:{} end:{}:{}", inputFile.filename(),
                  item.startLine, item.startLineOffset, item.endLine, item.endLineOffset);
      }
    });

    newHighlighting.save();
  }

  private <T extends Serializable> void saveMetric(InputFile file, Metric<T> metric, T value) {
    context.<T>newMeasure()
      .withValue(value)
      .forMetric(metric)
      .on(file)
      .save();
  }
}
