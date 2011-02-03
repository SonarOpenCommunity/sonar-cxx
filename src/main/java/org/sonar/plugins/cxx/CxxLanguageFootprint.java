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

import java.util.HashSet;
import java.util.Set;

import org.sonar.squid.recognizer.ContainsDetector;
import org.sonar.squid.recognizer.Detector;
import org.sonar.squid.recognizer.EndWithDetector;
import org.sonar.squid.recognizer.KeywordsDetector;
import org.sonar.squid.recognizer.LanguageFootprint;

public final class CxxLanguageFootprint implements LanguageFootprint{	
	
	public Set<Detector> getDetectors() {
	  final Set<Detector> detectors = new HashSet<Detector>();
	  
		detectors.add(new EndWithDetector(0.95, '}', ';', '{'));
		detectors.add(new KeywordsDetector(0.7, "||", "&&"));
		detectors.add(new KeywordsDetector(0.95, 
				"#define",
				"#endif",
				"#ifdef", "#ifndef", "#include"));
		detectors.add(new KeywordsDetector(0.3,
				"auto",
				"class", 
				"do", "double",
				"float", "for", 
				"int", 
				"long", 
				"mutable",
				"namespace",
				"operator",
				"private", "protected", "public",
				"return",
				"sizeof", "short", "static", "struct",
				"template", "throw", "typedef", "typename",
				"union",
				"void",
				"while"));
		detectors.add(new ContainsDetector(0.95, "++", "--"));
		return detectors;
	}
	
}
