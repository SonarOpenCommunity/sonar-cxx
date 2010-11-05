/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 ${name}
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.cxx;

//import java.io.IOException;
import java.io.File;
import java.util.List;

//import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang.StringUtils;

import org.sonar.api.batch.AbstractSourceImporter;
//import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;


public final class CxxSourceImporter extends AbstractSourceImporter {
	private Project project;
	
	public CxxSourceImporter(Project p)
	{
		super(CxxLanguage.INSTANCE);
		project = p;
	}
	
	protected Resource<CxxDir> createResource(File file, List<File> sourceDirs, boolean unitTest)
	{
	  return CxxFile.fromAbsolute(project, file.getAbsolutePath());
	}
	/*
	public void analyse(Project project, SensorContext context)
	{
		final List<File> sources = project.getFileSystem().getSourceFiles(CxxLanguage.INSTANCE);
		
		for(File file : sources)
		{
			final String absolutePath = StringUtils.substringBeforeLast(file.getAbsolutePath(), "/");									
			final CxxDir dir = CxxDir.fromAbsolute(project, absolutePath);
			
			if (dir != null)
			{				
				try
				{
					final String content = FileUtils.readFileToString(file, "UTF-8");
					CxxFile cxxFile = new CxxFile(dir, file, false);
					
					context.saveResource(cxxFile);
					context.saveSource(cxxFile, content);
					
				}
				catch(IOException ex)
				{
					//System.out.println("IO Eception");
				}
			}
		}
	}	*/
	@Override
	public String toString()
	{
	  return getClass().getSimpleName();
	}
}
