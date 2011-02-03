/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 François DORIN
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits réservés.
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
import java.util.List;

import net.sourceforge.pmd.cpd.CPPLanguage;
import net.sourceforge.pmd.cpd.Tokenizer;

import org.sonar.api.batch.CpdMapping;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;

public final class CxxCpdMapping implements CpdMapping {
	private final CPPLanguage language = new CPPLanguage();
	private Project project;
	
	public CxxCpdMapping(Project project)
	{
		this.project = project;
	}
		
	public Resource<CxxDir> createResource(File file, List<File> dirs) {
		return CxxFile.fromFileName(project, file.getAbsolutePath(), false);
	}

	public Language getLanguage() {
		return CxxLanguage.INSTANCE;
	}

	public Tokenizer getTokenizer() {
		return language.getTokenizer();
	}
}
