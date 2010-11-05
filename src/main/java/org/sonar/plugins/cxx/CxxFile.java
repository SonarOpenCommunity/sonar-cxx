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

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.WildcardPattern;

public final class CxxFile extends Resource<CxxDir>{
	private CxxDir   directory;
	private String   name;
	private String   description;
	private String   qualifier;
	private String   scope;
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
	public CxxDir getParent() {
		return directory;
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
		return "File " + name;
	}
	
	public CxxFile(CxxDir folder, File file, boolean testUnit)
	{
		/*
		 * @note Resource.SCOPE_FILE is deprecated. Use SCOPE_ENTITY instead.
		 */
		scope = Resource.SCOPE_ENTITY;
		qualifier = testUnit ? Resource.QUALIFIER_UNIT_TEST_CLASS : Resource.QUALIFIER_FILE;
		language  = CxxLanguage.INSTANCE;
		directory = folder;
		name      = file.getName();
		setKey((folder.getAbsolutePath() + "/" + file.getName()).replace('/', ':'));
	}
	
	public static CxxFile fromAbsolute(Project project, String filename)
	{
		final String path = StringUtils.substringBeforeLast(filename, "/");
		final String file = StringUtils.substringAfterLast(filename, "/");
		
		final CxxDir dir = CxxDir.fromAbsolute(project, path);
		if (dir != null)
		{
			return new CxxFile(dir, new File(file), false);
		}
		return null;
	}
}
