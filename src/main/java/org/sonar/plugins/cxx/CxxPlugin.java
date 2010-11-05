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

import org.sonar.api.Plugin;
import org.sonar.api.Extension;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.plugins.cxx.CxxLanguage;


import java.util.ArrayList;
import java.util.List;


@Properties( {
  @Property(key = CxxPlugin.KEY,      
      name = "CXX Core Plugin",
      description = "CXX Core Plugin for Sonar",
      project = false, global = true) })

      
/**
 * This class is the entry point for all extensions
 */
public final class CxxPlugin implements Plugin {
  public static final String KEY = "c++";
  /**
   * @deprecated this is not used anymore
   */
  public String getKey() {
    return KEY;
  }

  /**
   * @deprecated this is not used anymore
   */
  public String getName() {
    return "C++ plugin";
  }

  /**
   * @deprecated this is not used anymore
   */
  public String getDescription() {
    return "Add support for C++ language.";
  }

  // This is where you're going to declare all your Sonar extensions
  public List<Class<? extends Extension>> getExtensions() {
	  List<Class<? extends Extension>> l;
	  l = new ArrayList<Class<? extends Extension>>();
	  l.add(CxxLanguage.class);
	  l.add(CxxSourceImporter.class);
	  l.add(CxxColorizer.class);
	  l.add(CxxLineCounter.class);
	  l.add(CxxCpdMapping.class);
	  l.add(CxxRatsRuleRepository.class);
	  l.add(CxxRatsSensor.class);
	  l.add(CxxRatsProfile.class);
    return l;
  }

  @Override  
  public String toString() {
    return getClass().getSimpleName();
  }
}


