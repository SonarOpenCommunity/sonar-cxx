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
package org.sonar.cxx.cxxlint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sonar.sslr.api.Grammar;
import java.beans.Statement;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.CxxVCppBuildLogParser;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.api.CheckMessage;
import org.sonar.squidbridge.api.SourceFile;

/**
 *
 * @author jocs
 */
public class CxxLint {

  private static final Logger LOG = Loggers.get(CxxLint.class);

  private static Options createCommandLineOptions() {
    Options options = new Options();
    options.addOption("s", true, "settings file");
    options.addOption("f", true, "file to analyse - required");
    options.addOption("e", true, "file encoding");
    return options;
  }

  public static String readFile(String filename) {
    Path path = Paths.get(filename);
    String content = null;
    try (Stream<String> lines = Files.lines(path)) {
      content = lines.collect(Collectors.joining());
    } catch (IOException ex) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("CxxLint exception in readFile {}", ex);
      }
    }

    return content;
  }

  /**
   * @param args the command line arguments
   * @throws IOException
   * @throws UnsupportedEncodingException
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws java.Exception
   */
  public static void main(String[] args) {

    CommandLineParser commandlineParser = new DefaultParser();
    Options options = createCommandLineOptions();
    CommandLine parsedArgs = null;
    String settingsFile = "";
    String encodingOfFile = "UTF-8";
    File targetFile = null;
    boolean isNotOptionS = true;

    try {
      parsedArgs = commandlineParser.parse(createCommandLineOptions(), args);
      if (!parsedArgs.hasOption("f")) {
        throw new ParseException("f option mandatory");
      } else {
        targetFile = new File(parsedArgs.getOptionValue("f"));
        if (!targetFile.exists()) {
          throw new ParseException("file to analysis not found");
        }
      }

      if (parsedArgs.hasOption("s")) {
        isNotOptionS = false;
        settingsFile = parsedArgs.getOptionValue("s");
        File f = new File(settingsFile);
        if (!f.exists()) {
          throw new ParseException("optional settings file given with -s, however file was not found");
        }
      }

      if (parsedArgs.hasOption("e")) {
        encodingOfFile = parsedArgs.getOptionValue("e");
      }

    } catch (ParseException exp) {
      LOG.error("Parsing Command line Failed.  Reason: " + exp);
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("java -jar CxxLint-<sersion>.jar -f filetoanalyse", options);
      return;
    }


    try {
      File parent = targetFile.getParentFile();
      SensorContextTester sensorContext = SensorContextTester.create(parent.toPath());
      CxxConfiguration configuration = new CxxConfiguration(Charset.forName(encodingOfFile));

      sensorContext.fileSystem().add(TestInputFileBuilder.create("", parent, targetFile).build());
      InputFile cxxFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates()
        .hasPath(targetFile.getName()));
      List<CheckerData> rulesData = new ArrayList<>();
      if (!"".equals(settingsFile)) {
        JsonParser parser = new JsonParser();
        String fileContent = readFile(settingsFile);

        // get basic information
        String platformToolset = getJsonStringValue(parser, fileContent, "platformToolset");
        String platform = getJsonStringValue(parser, fileContent, "platform");
        String projectFile = getJsonStringValue(parser, fileContent, "projectFile");

        JsonElement rules = parser.parse(fileContent).getAsJsonObject().get("rules");
        if (rules != null) {
          createCheckerRules(rulesData, rules);
        }

        JsonElement includes = parser.parse(fileContent).getAsJsonObject().get("includes");
        if (includes != null) {
          for (JsonElement include : includes.getAsJsonArray()) {
            configuration.addOverallIncludeDirectory(include.getAsString());
          }
        }

        JsonElement defines = parser.parse(fileContent).getAsJsonObject().get("defines");
        if (defines != null) {
          for (JsonElement define : defines.getAsJsonArray()) {
            configuration.addOverallDefine(define.getAsString());
          }
        }

        JsonElement additionalOptions = parser.parse(fileContent).getAsJsonObject().get("additionalOptions");
        StringBuilder elementsOfAdditionalOptions = new StringBuilder();
        if (additionalOptions != null) {
          for (JsonElement option : additionalOptions.getAsJsonArray()) {
            elementsOfAdditionalOptions.append(' ');
            elementsOfAdditionalOptions.append(option.getAsString());
          }
        }

        handleVCppAdditionalOptions(platformToolset, platform, elementsOfAdditionalOptions + " ",
          projectFile, targetFile.getName(), configuration);
      }

      List<Class> checks = CxxCheckList.getChecks();
      List<SquidAstVisitor<Grammar>> visitors = new ArrayList<>();

      if (isNotOptionS) {
        for (Class<?> check : checks) {
          SquidAstVisitor<Grammar> element = (SquidAstVisitor<Grammar>) check.newInstance();
          visitors.add(element);
        }
      } else {
        for (CheckerData checkDefined : rulesData) {

          // get check from list
          Class<?> check = getRuleFromChecks(checkDefined, checks);
          if (check == null) {
            continue;
          }

          // and update the key according with profile
          // this ensures the template rules have correct key
          SquidAstVisitor<Grammar> element = (SquidAstVisitor<Grammar>) check.newInstance();
          annotateRule(checkDefined, check);
          for (Field f : check.getDeclaredFields()) {
            annotateField(checkDefined, element, f);
          }
          visitors.add(element);
        }
      }
      SourceFile file = CxxAstScanner.scanSingleFileConfig(
        new CppLanguage(sensorContext.config()),
        cxxFile,
        configuration,
        visitors.toArray(new SquidAstVisitor[visitors.size()]));
      LOG.info("Analyse with : " + visitors.size() + " checks");

      for (CheckMessage message : file.getCheckMessages()) {
        Object check = message.getCheck();
        String key = getRuleKey(check);

        // E:\TSSRC\Core\Common\libtools\tool_archive.cpp(390): Warning : sscanf can be ok, 
        // but is slow and can overflow buffers.  [runtime/printf-5] [1]
        LOG.info(message.getSourceCode() + "(" + message.getLine() + "): Warning : "
          + message.formatDefaultMessage() + " [" + key + "]");
      }

      LOG.info("LOC: {}", file.getInt(CxxMetric.LINES_OF_CODE));
      LOG.info("COMPLEXITY: {}", file.getInt(CxxMetric.COMPLEXITY));

    } catch (InstantiationException | IllegalAccessException |NullPointerException ex) {
      LOG.error("{}", ex);
    }

  }

  /**
   * @param check
   * @return
   */
  private static String getRuleKey(Object check) {
    String key = "";
    for (Annotation a : check.getClass().getAnnotations()) {
      try {
        Rule rule = (Rule) a;
        if (rule != null) {
          key = rule.key();
          break;
        }
      } catch (RuntimeException ex) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("{}", ex);
        }
      }
    }
    return key;
  }

  /**
   * @param checkDefined
   * @param element
   * @param f
   * @throws IllegalAccessException
   */
  private static void annotateField(CheckerData checkDefined, SquidAstVisitor<Grammar> element, Field f)
    throws IllegalAccessException {
    for (Annotation a : f.getAnnotations()) {
      RuleProperty ruleProp = (RuleProperty) a;
      if ((ruleProp != null)
        && (checkDefined.getParameterData().containsKey(ruleProp.key()))) {
        if (f.getType().equals(int.class)) {
          String cleanData = checkDefined.getParameterData().get(ruleProp.key());
          int value = Integer.parseInt(cleanData);
          if (f.toString().startsWith("public ")) {
            f.set(element, value);
          } else {
            char first = Character.toUpperCase(ruleProp.key().charAt(0));
            Statement stmt = new Statement(element, "set" + first + ruleProp.key().substring(1),
              new Object[]{value});
            try {
              stmt.execute();
            } catch (Exception ex) {
              LOG.error("{}", ex);
            }
          }
        }

        if (f.getType().equals(String.class)) {
          String cleanData = checkDefined.getParameterData().get(ruleProp.key());

          if (f.toString().startsWith("public ")) {
            f.set(element, cleanData);
          } else {
            char first = Character.toUpperCase(ruleProp.key().charAt(0));
            Statement stmt = new Statement(element, "set" + first + ruleProp.key().substring(1),
              new Object[]{cleanData});
            try {
              stmt.execute();
            } catch (Exception ex) {
              LOG.error("{}", ex);
            }
          }
        }
      }
    }
  }

  /**
   * @param rulesData
   * @param rules
   */
  private static void createCheckerRules(List<CheckerData> rulesData, JsonElement rules) {
    for (JsonElement rule : rules.getAsJsonArray()) {
      JsonObject data = rule.getAsJsonObject();
      String ruleId = data.get("ruleId").getAsString();

      String templateKey = "";
      if (data.has("templateKeyId")) {
        templateKey = data.get("templateKeyId").getAsString();
      }

      String enabled = data.get("status").getAsString();

      CheckerData check = new CheckerData();
      check.setId(ruleId);
      check.setTemplateId(templateKey);

      check.setEnable("Enabled".equals(enabled));
      JsonElement region = data.get("properties");
      if (region != null) {
        for (Entry<?, ?> parameter : region.getAsJsonObject().entrySet()) {
          JsonElement elem = (JsonElement) parameter.getValue();
          check.getParameterData().put(parameter.getKey().toString(), elem.getAsString());
        }
      }

      rulesData.add(check);
    }
  }

  /**
   * @param checkDefined
   * @param check
   */
  private static void annotateRule(CheckerData checkDefined, Class<?> check) {
    for (Annotation a : check.getAnnotations()) {
      try {
        Rule rule = (Rule) a;
        if (rule != null) {
          changeAnnotationValue(a, "key", checkDefined.getId());
          break;
        }
      } catch (IllegalStateException ex) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("{}", ex);
        }
        break;
      }
    }
  }

  /**
   * Changes the annotation value for the given key of the given annotation to newValue and returns the previous value.
   * from: http://stackoverflow.com/questions/14268981/modify-a-class-definitions-annotation-string-parameter-at-runtime
   *
   * @return updated or old value
   * @param annotation
   * @param key
   * @param newValue
   */
  @SuppressWarnings("unchecked")
  public static Object changeAnnotationValue(Annotation annotation, String key, Object newValue) {
    Object handler = Proxy.getInvocationHandler(annotation);
    Field f;
    try {
      f = handler.getClass().getDeclaredField("memberValues");
    } catch (NoSuchFieldException | SecurityException e) {
      throw new IllegalStateException(e);
    }
    f.setAccessible(true);
    Map<String, Object> memberValues;
    try {
      memberValues = (Map<String, Object>) f.get(handler);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
    Object oldValue = memberValues.get(key);
    if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
      throw new IllegalArgumentException();
    }
    memberValues.put(key, newValue);
    return oldValue;
  }

  private static String getJsonStringValue(JsonParser parser, String fileContent, String id) {
    JsonElement element = parser.parse(fileContent).getAsJsonObject().get(id);
    if (element != null) {
      return element.getAsString();
    }
    return "";
  }

  private static void handleVCppAdditionalOptions(String platformToolset, String platform,
    String elementsOfAdditionalOptions, String project, String fileToAnalyse,
    CxxConfiguration configuration) {
    if ("V100".equals(platformToolset)
      || "V110".equals(platformToolset)
      || "V120".equals(platformToolset)
      || "V140".equals(platformToolset)) {
      HashMap<String, List<String>> uniqueIncludes = new HashMap<>();
      HashMap<String, Set<String>> uniqueDefines = new HashMap<>();
      uniqueDefines.put(fileToAnalyse, new HashSet<String>());
      uniqueIncludes.put(fileToAnalyse, new ArrayList<String>());
      CxxVCppBuildLogParser lineOptionsParser = new CxxVCppBuildLogParser(uniqueIncludes, uniqueDefines);
      lineOptionsParser.setPlatform(platform);
      lineOptionsParser.setPlatformToolset(platformToolset);
      lineOptionsParser.parseVCppLine(elementsOfAdditionalOptions, project, fileToAnalyse);
      for (String define : uniqueDefines.get(fileToAnalyse)) {
        configuration.addOverallDefine(define);
      }
    }
  }

  private static Class<?> getRuleFromChecks(CheckerData checkDefined, List<Class> checks) {
    for (Class<?> check : checks) {
      Rule rule = check.getAnnotation(Rule.class);
      if (rule == null) {
        continue;
      }

      if (checkDefined.getId().equals(rule.key())
        || checkDefined.getTemplateId().equals(rule.key())
        && checkDefined.isEnabled()) {
        return check;
      }
    }

    return null;
  }
}
