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
package org.sonar.plugins.cxx.xunit;

import java.io.File;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.resources.Project;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.cxx.CxxLanguage;

public class DefaultResourceFinder implements ResourceFinder {

  public org.sonar.api.resources.File findInSonar(File file, SensorContext context, FileSystem fs, Project project) {
    FilePredicates p = fs.predicates();
    Iterable<File> unitTestFiles = fs.files(file.isAbsolute()
      ? p.and(p.hasLanguage(CxxLanguage.KEY), p.hasType(InputFile.Type.TEST), p.hasAbsolutePath(file.getPath()))
      : p.and(p.hasLanguage(CxxLanguage.KEY), p.hasType(InputFile.Type.TEST), p.hasRelativePath(file.getPath()))
    );
    org.sonar.api.resources.File unitTestFile = null;
    if (unitTestFiles.iterator().hasNext()) {
      unitTestFile = org.sonar.api.resources.File.fromIOFile(unitTestFiles.iterator().next(), project);
      if (context.getResource(unitTestFile) == null) {
        unitTestFile = null;
      }
    }
    return unitTestFile;
  }

}
