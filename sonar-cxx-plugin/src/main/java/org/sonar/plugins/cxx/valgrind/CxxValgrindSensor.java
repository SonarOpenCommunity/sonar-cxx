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

import java.io.File;
import java.util.Set;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.cxx.utils.CxxMetrics;
import org.sonar.plugins.cxx.utils.CxxReportSensor;
import org.sonar.plugins.cxx.utils.CxxUtils;
import org.sonar.api.batch.bootstrap.ProjectReactor;

/**
 * {@inheritDoc}
 */
public class CxxValgrindSensor extends CxxReportSensor {
  public static final String REPORT_PATH_KEY = "sonar.cxx.valgrind.reportPath";
  private RulesProfile profile;

  /**
   * {@inheritDoc}
   */
  public CxxValgrindSensor(ResourcePerspectives perspectives, Settings conf, FileSystem fs, RulesProfile profile, ProjectReactor reactor) {
    super(perspectives, conf, fs, reactor, CxxMetrics.VALGRIND);
    this.profile = profile;
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
  protected void processReport(final Project project, final SensorContext context, File report)
      throws javax.xml.stream.XMLStreamException
  {
    CxxUtils.LOG.info("Parsing 'Valgrind' format"); 
    ValgrindReportParser parser = new ValgrindReportParser();
    saveErrors(project, context, parser.processReport(project, context, report));
  }

  void saveErrors(Project project, SensorContext context, Set<ValgrindError> valgrindErrors) {
    for (ValgrindError error : valgrindErrors) {
      ValgrindFrame frame = error.getLastOwnFrame(fs.baseDir().getPath());
      if (frame != null) {
        saveUniqueViolation(project, context, CxxValgrindRuleRepository.KEY,
                            frame.getPath(), frame.getLine(), error.getKind(), error.toString());
      }
      else{
        CxxUtils.LOG.warn("Cannot find a project file to assign the valgrind error '{}' to", error);
      }
    }
  }
}
