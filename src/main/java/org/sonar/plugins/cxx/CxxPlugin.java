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

import org.sonar.api.Plugin;
import org.sonar.api.Extension;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.xunit.CxxXunitSensor;
import org.sonar.plugins.cxx.gcovr.CxxGcovrSensor;
import org.sonar.plugins.cxx.cppcheck.CxxCppCheckSensor;
import org.sonar.plugins.cxx.cppcheck.CxxCppCheckRuleRepository;
import org.sonar.plugins.cxx.cppcheck.CxxCppCheckProfile;
import org.sonar.plugins.cxx.cppncss.CxxCppNcssSensor;
import org.sonar.plugins.cxx.rats.CxxRatsProfile;
import org.sonar.plugins.cxx.rats.CxxRatsSensor;
import org.sonar.plugins.cxx.rats.CxxRatsRuleRepository;
import org.sonar.plugins.cxx.valgrind.CxxValgrindProfile;
import org.sonar.plugins.cxx.valgrind.CxxValgrindRuleRepository;
import org.sonar.plugins.cxx.valgrind.CxxValgrindSensor;
import org.sonar.plugins.cxx.veraxx.CxxVeraxxSensor;
import org.sonar.plugins.cxx.veraxx.CxxVeraxxRuleRepository;
import org.sonar.plugins.cxx.veraxx.CxxVeraxxProfile;

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
	  l.add(CxxXunitSensor.class);
	  l.add(CxxGcovrSensor.class);
	  l.add(CxxCppCheckRuleRepository.class);
	  l.add(CxxCppCheckSensor.class);
	  l.add(CxxCppCheckProfile.class);
	  l.add(CxxCppNcssSensor.class);
	  l.add(CxxVeraxxRuleRepository.class);
	  l.add(CxxVeraxxSensor.class);
	  l.add(CxxVeraxxProfile.class);
	  l.add(CxxValgrindRuleRepository.class);
	  l.add(CxxValgrindSensor.class);
	  l.add(CxxValgrindProfile.class);
    return l;
  }

  @Override  
  public String toString() {
    return getClass().getSimpleName();
  }
}


