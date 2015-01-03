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
package org.sonar.plugins.cxx.utils;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.sonar.api.utils.WildcardPattern;

import java.io.File;
import java.util.List;

public class DirectoryScanner {

  private final File baseDir;
  private final WildcardPattern pattern;

  public DirectoryScanner(File baseDir, WildcardPattern pattern) {
    this.baseDir = baseDir;
    this.pattern = pattern;
  }

  public List<File> getIncludedFiles() {
    final String baseDirAbsolutePath = baseDir.getAbsolutePath();
    IOFileFilter fileFilter = new IOFileFilter() {

      @Override
      public boolean accept(File dir, String name) {
        return accept(new File(dir, name));
      }

      @Override
      public boolean accept(File file) {
        String path = file.getAbsolutePath();
        path = path.substring(Math.min(baseDirAbsolutePath.length(), path.length()));
        return pattern.match(FilenameUtils.separatorsToUnix(path));
      }
    };
    return Lists.newArrayList(FileUtils.listFiles(baseDir, fileFilter, TrueFileFilter.INSTANCE));
  }
}
