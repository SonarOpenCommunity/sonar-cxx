/*
 * SonarCxxPlugin, open source software for C++ quality management tool.
 * Copyright (C) 2010 François DORIN, Franck Bonin
 *
 * SonarCxxPlugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarCxxPlugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with SonarCxxPlugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.cxx.utils;

import java.io.File;
import java.util.List;

import org.apache.maven.model.FileSet;
import org.apache.tools.ant.DirectoryScanner;

public class FileSetManager {

	
	private DirectoryScanner scan( FileSet fileSet )
	  {
		  File basedir = new File( fileSet.getDirectory() );

		  if ( !basedir.exists() || !basedir.isDirectory() )
		  {
			  return null;
		  }

		  DirectoryScanner scanner = new DirectoryScanner();

		  List<String> includesList = fileSet.getIncludes();
		  List<String> excludesList = fileSet.getExcludes();

		  if ( includesList.size() > 0 )
		  {
			  scanner.setIncludes( includesList.toArray(new String[0]) );
		  }

		  if ( excludesList.size() > 0 )
		  {
			  scanner.setExcludes( excludesList.toArray(new String[0]) );
		  }

		  if ( true)//fileSet.isUseDefaultExcludes() )
		  {
			  scanner.addDefaultExcludes();
		  }

		  scanner.setBasedir( basedir );
		  scanner.setFollowSymlinks( true );//fileSet.isFollowSymlinks() );

		  scanner.scan();

		  return scanner;
	  }
	  
	  private static final String[] EMPTY_STRING_ARRAY = new String[0];

	  public String[] getIncludedFiles( FileSet fileSet )
	      {
	           DirectoryScanner scanner = scan( fileSet );
	   
	           if ( scanner != null )
	           {
	               return scanner.getIncludedFiles();
	          }
	   
	           return EMPTY_STRING_ARRAY;
	       }

	  public String[] getIncludedDirectories( FileSet fileSet )
	  {
		  DirectoryScanner scanner = scan( fileSet );

		  if ( scanner != null )
		  {
			  return scanner.getIncludedDirectories();
		  }

		  return EMPTY_STRING_ARRAY;
	  }
}
