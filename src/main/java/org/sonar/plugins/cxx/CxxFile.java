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
package org.sonar.plugins.cxx;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.plugins.cxx.cppcheck.CxxCppCheckSensor;

public final class CxxFile extends Resource<CxxDir> {

  private CxxDir directory;
  private String name;
  private String description;
  private String qualifier;
  private String scope;
  private Language language;

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public Language getLanguage() {
    return language;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getLongName() {
    return name;
  }

  @Override
  public CxxDir getParent() {
    return directory;
  }

  @Override
  public String getQualifier() {
    return qualifier;
  }

  public boolean isUnitTest() {
    return qualifier.equals(Resource.QUALIFIER_UNIT_TEST_CLASS);
  }

  @Override
  public String getScope() {
    return scope;
  }

  @Override
  public boolean matchFilePattern(String pattern) {
    logger.debug("matchFilePattern call with {} on {}", pattern, getKey());
    String patternWithoutFileSuffix = StringUtils.substringBeforeLast(pattern, ".");
    WildcardPattern matcher = WildcardPattern.create(patternWithoutFileSuffix, "/");
    return matcher.match(getKey());
  }

  private static Logger logger = LoggerFactory.getLogger(CxxCppCheckSensor.class);

  private String CanonicalizeAbsoluteFilePath(String filePath) {
    filePath = filePath.replaceAll("/./", "/").replaceAll("/+", "/");
    // logger.info("Build FilePath 2 = {}", filePath);
    File tmp = new File(filePath);
    try {
      filePath = tmp.getCanonicalPath();
    } catch (IOException e) {
      filePath = tmp.getAbsolutePath();
    }
    return filePath;
  }

  private void initIdentifierFromAbsolutFilePath(Project project, String absoluteFilePath) {
    directory = CxxDir.fromAbsolute(project, StringUtils.substringBeforeLast(absoluteFilePath, "/"));
    name = StringUtils.substringAfterLast(absoluteFilePath, "/");
    setKey((directory.getProjectPath() + "/" + name).replace('/', ':'));
  }

  private CxxFile(Project project, String fileName, String[] includeSearchPath, boolean unitTest) {
    if (fileName == null) {
      throw new IllegalArgumentException("fileName is null");
    }
    if (includeSearchPath == null || includeSearchPath.length == 0) {
      throw new IllegalArgumentException("includeSearchPath is null or empty");
    }
    logger.debug("FileName received : {}", fileName);

    scope = Resource.SCOPE_ENTITY;
    qualifier = unitTest ? Resource.QUALIFIER_UNIT_TEST_CLASS : Resource.QUALIFIER_CLASS;// Resource.QUALIFIER_FILE;
    language = CxxLanguage.INSTANCE;

    String realFileName = StringUtils.trim(fileName).replace('\\', '/');
    if (new File(realFileName).isAbsolute()) { // realFileName.startsWith("/")) {
      logger.debug("we got an absolute path");
      realFileName = CanonicalizeAbsoluteFilePath(realFileName);
      initIdentifierFromAbsolutFilePath(project, realFileName);
    } else if (realFileName.contains("/")) {
      logger.debug("we got a relative path");
      for (String includePath : includeSearchPath) {
        String pathToTest = null;
        if (new File(includePath).isAbsolute())
          pathToTest = includePath + "/" + realFileName;
        else pathToTest = project.getFileSystem().getBasedir().toString() + "/" + includePath + "/" + realFileName;
        // logger.info("Build FilePath 1 = {}", pathToTest);
        pathToTest = CanonicalizeAbsoluteFilePath(pathToTest);
        if (new File(pathToTest).exists()) {
          realFileName = pathToTest;
          break;
        }
      }
      initIdentifierFromAbsolutFilePath(project, realFileName);
    } else {
      logger.debug("we got something else");
      directory = CxxDir.fromAbsolute(project, realFileName);
      name = realFileName;
      setKey(realFileName);
    }

    Object t[] = { realFileName, getKey(), name, directory.getKey() };
    logger.debug("CxxFile created from fileName {},  key = {}, Name = {}, directorykey = {}", t);
  }

  public static CxxFile fromFileName(Project project, String filename, boolean unitTest) {
    String includePath[] = { "" };
    return new CxxFile(project, filename, includePath, unitTest);
  }

  public static CxxFile fromFileName(Project project, String filename, String[] includeSearchPath, boolean unitTest) {
    ArrayList<String> aTmp = new ArrayList<String>();
    aTmp.addAll(Arrays.asList(includeSearchPath));
    aTmp.add(0, "");
    return new CxxFile(project, filename, aTmp.toArray(new String[0]), unitTest);
  }
}
