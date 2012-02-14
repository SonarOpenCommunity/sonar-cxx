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

import org.apache.commons.lang.StringUtils;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;

public final class CxxDir extends Resource<Project>{
	private Project  project;
	private String   name;
	private String   description;
	private String   qualifier;
	private String   scope;
	private Language language;
	private String   absolutePath;
	private String   relativePath;
	
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
	public Project getParent() {
		return project;
	}

	@Override
	public String getQualifier() {
		return qualifier;
	}
	
	@Override
	public String getScope() {
		return scope;
	}
	
	@Override
	public boolean matchFilePattern(String pattern) {
		String patternWithoutFileSuffix = StringUtils.substringBeforeLast(pattern, ".");
		WildcardPattern matcher = WildcardPattern.create(patternWithoutFileSuffix, "/");
		return matcher.match(getKey());
	}

	@Override
	public String getLongName() {
		return "Directory " + name;
	}
	
	private CxxDir(Project p, File dir, boolean testUnit)
	{
		/*
		 * @note Resource.SCOPE_DIRECTORY is deprecated. Use SCOPE_SPACE instead.
		 */		
		scope     = Resource.SCOPE_SPACE;
		qualifier = testUnit ? Resource.QUALIFIER_UNIT_TEST_CLASS : Resource.QUALIFIER_DIRECTORY;
		language  = CxxLanguage.INSTANCE;
		project   = p;
		name      = dir.getName();
		
		absolutePath = dir.getAbsoluteFile().toString();//.replaceFirst(p.getFileSystem().getBasedir().toString(), "");
		relativePath = absolutePath.replaceFirst(p.getFileSystem().getBasedir().toString(), ""); 
		setKey(relativePath.replace('/', '.'));
	}
	
	public static CxxDir fromAbsolute(Project p, String path)
	{		
		return new CxxDir(p, new File(path), false);
	}
	
	public static CxxDir fromProjectDir(Project p, String path)
	{
		return new CxxDir(p, new File(p.getFileSystem().getBasedir().toString() + path), false);
	}
	
	public String getAbsolutePath()
	{
		return absolutePath;
	}
	
	public String getProjectPath()
	{
	  return relativePath;
	}
	
	@Override
	public String toString()
	{
	  return getClass().getSimpleName();
	}
}
