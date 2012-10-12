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
package org.sonar.plugins.cxx.ast;

import java.io.File;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider;
import org.sonar.plugins.cxx.utils.CxxUtils;

/**
 * A custom IncludeFileContentProvider class is used. We could use org.eclipse.cdt.internal.core.parser.SavedFilesProvider
 * class, but it would require adding new external CDT jar dependencies.
 * @author Przemyslaw Kociolek
 */
public class CxxCppIncludeFileContentProvider extends InternalFileContentProvider {

  @Override
  public InternalFileContent getContentForInclusion(String path) {
    File includeFile = new File(path);
    if(includeFile.isAbsolute() && !includeFile.exists()) {
      CxxUtils.LOG.debug("No include file: " + path);
      return null;
    }
     
    return (InternalFileContent) FileContent.createForExternalFileLocation(path);
  }

  @Override
  public InternalFileContent getContentForInclusion(IIndexFileLocation ifl, String astPath) {
    return getContentForInclusion(ifl.getFullPath());
  }

}
