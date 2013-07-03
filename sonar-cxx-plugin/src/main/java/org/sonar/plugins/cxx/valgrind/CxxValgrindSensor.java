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
package org.sonar.plugins.cxx.valgrind;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.RuleFinder;
import org.sonar.plugins.cxx.utils.CxxReportSensor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.sonar.api.resources.InputFile;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.CxxPlugin;
import org.sonar.plugins.cxx.utils.CxxUtils;
import org.sonar.squid.api.SourceClass;
import org.sonar.squid.api.SourceCode;
import org.sonar.squid.api.SourceFile;
import org.sonar.squid.api.SourceFunction;

/**
 * {@inheritDoc}
 */
public class CxxValgrindSensor extends CxxReportSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.valgrind.reportPath";
  private static final String DEFAULT_REPORT_PATH = "valgrind-reports/valgrind-result-*.xml";
  private RulesProfile profile;
  private final Settings conf;
  private Map<String,List<String>> functionLookupTable = new TreeMap<String, List<String>>();;

  /**
   * {@inheritDoc}
   */
  public CxxValgrindSensor(RuleFinder ruleFinder, Settings conf, RulesProfile profile) {
    super(ruleFinder, conf);
    this.profile = profile;
    this.conf = conf;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void analyse(Project project, SensorContext context) {
    lookupFiles(project);
    super.analyse(project, context);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return super.shouldExecuteOnProject(project)
      && !profile.getActiveRulesByRepository(CxxValgrindRuleRepository.KEY).isEmpty();
  }

  @Override
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected String defaultReportPath() {
    return DEFAULT_REPORT_PATH;
  }

  @Override
  protected void processReport(final Project project, final SensorContext context, File report)
      throws javax.xml.stream.XMLStreamException
  {
    ValgrindReportParser parser = new ValgrindReportParser();
    saveErrors(project, context, parser.parseReport(report));
  }

  void saveErrors(Project project, SensorContext context, Set<ValgrindError> valgrindErrors) {
    for (ValgrindError error : valgrindErrors) {
      List<ValgrindFrame> frames = error.getLastOwnFrame(project.getFileSystem().getBasedir(), functionLookupTable);
      for(ValgrindFrame frame : frames)
      {
        saveViolation(project, context, CxxValgrindRuleRepository.KEY,
            frame.getPath(), frame.getLine(), error.getKind(), error.toString());        
      }
    }
  }
  
  void recursiveFunctionSearch(InputFile file, SourceCode childParent)
  {
    for (SourceCode child : childParent.getChildren()) {
      if (child instanceof  SourceFunction) {
        String[] childelems = child.getKey().split("::");
        String functionName = childelems.length == 2 ? 
        childelems[1].split(":")[0] : childelems[0].split(":")[0];

        if(functionLookupTable.containsKey(functionName)) {
          List<String> elems = functionLookupTable.get(functionName);
          elems.add(file.getFile().getPath());
          functionLookupTable.put(functionName, elems);
        } else {
          List<String> newElems = new ArrayList<String>();
          newElems.add(file.getFile().getPath());
          functionLookupTable.put(functionName, newElems);
        }
      } else {
        if(child.hasChildren()) {
          recursiveFunctionSearch(file, child);
        }
      }
    }
  }
  
  void lookupFiles(Project project) {
    List<InputFile> files = project.getFileSystem().testFiles(CxxLanguage.KEY);
    files.addAll(project.getFileSystem().mainFiles(CxxLanguage.KEY));

    CxxConfiguration cxxConf = new CxxConfiguration(project.getFileSystem().getSourceCharset());
    cxxConf.setBaseDir(project.getFileSystem().getBasedir().getAbsolutePath());
    cxxConf.setDefines(conf.getStringArray(CxxPlugin.DEFINES_KEY));
    cxxConf.setIncludeDirectories(conf.getStringArray(CxxPlugin.INCLUDE_DIRECTORIES_KEY));
    
    for (InputFile file : files) {
      SourceFile source = CxxAstScanner.scanSingleFileConfig(file.getFile(), cxxConf);
      if(source.hasChildren()) {
        recursiveFunctionSearch(file, source);
      }
    }    
  }  
}
