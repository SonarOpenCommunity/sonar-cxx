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
import java.nio.charset.Charset;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.sonar.api.batch.AbstractSourceImporter;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.cxx.utils.CxxUtils;

public final class CxxSourceImporter extends AbstractSourceImporter {

  private final Project project;
  private final CxxLanguage language;
  private final String testPatterns;

  public CxxSourceImporter(CxxLanguage language, Project project, Settings settings) {
    super(language);
    this.project = project;
    this.language = language;
    this.testPatterns = settings.getString(CxxPlugin.TEST_DIRECTORY_PATTERN);
  }

  @Override
  protected Resource createResource(File file, List<File> sourceDirs, boolean unitTest) {

    org.sonar.api.resources.File resource = org.sonar.api.resources.File.fromIOFile(file, project);
    resource.setLanguage(language);

    if (CxxUtils.isATestFile(testPatterns, file.getPath())) {
      resource.setQualifier(Qualifiers.UNIT_TEST_FILE);
    }

    return resource;
  }

  @Override
  protected void parseDirs(SensorContext context, List<File> files, List<File> sourceDirs, boolean unitTest, Charset sourcesEncoding) {

    for (File file : files) {

      Resource resource = createResource(file, sourceDirs, CxxUtils.isATestFile(testPatterns, file.getPath()));
      if (resource != null) {
        try {
          context.index(resource);
          if (isEnabled(project)) {

            String source = FileUtils.readFileToString(file, sourcesEncoding.name());
            context.saveSource(resource, source);
          }
        } catch (Exception e) {
          throw new SonarException("Unable to read and import the source file : '" + file.getAbsolutePath() + "' with the charset : '"
                  + sourcesEncoding.name() + "'.", e);
        }
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}