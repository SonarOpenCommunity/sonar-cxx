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
package org.sonar.plugins.cxx.externalrules;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Settings;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.rules.XMLRuleParser;
import org.sonar.plugins.cxx.CxxLanguage;

/**
 * Loads the external rules configuration file.
 */
public class CxxExternalRuleRepository extends RuleRepository {

  public static final String KEY = "cxxexternal";
  public static final String CUSTOM_RULES_KEY = "sonar.cxx.customRules.cxxexternal";
  public static final String CUSTOM_RULES_PROFILE_KEY = "sonar.cxx.customRules.cxxexternal.profile";
  public final Settings settings;
  private final XMLRuleParser xmlRuleParser;
  
  public CxxExternalRuleRepository(XMLRuleParser xmlRuleParser, Settings settings) {
    super(KEY, CxxLanguage.KEY);
    this.xmlRuleParser = xmlRuleParser;
    this.settings = settings;    
  }
  
  @Override
  public List<Rule> createRules() {
    List<Rule> rules = new ArrayList<Rule>();
        
    for(String key : settings.getStringArray(CUSTOM_RULES_KEY))
    {
      if (StringUtils.isNotBlank(key)) {
        String data = settings.getString(GetCombinedKey(key));
        if (StringUtils.isNotBlank(data)) {
          rules.addAll(xmlRuleParser.parse(new StringReader(data)));
        }
      }      
    }

    return rules;
  }  
  
  public String GetCombinedKey(String id) {
    return CUSTOM_RULES_KEY + "." + id + "." + CUSTOM_RULES_PROFILE_KEY;
  }
}
