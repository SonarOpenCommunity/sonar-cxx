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

import java.io.InputStream;
import java.util.List;

import org.sonar.api.rules.Rule;
import org.sonar.api.rules.XMLRuleParser;
import org.sonar.api.rules.RuleRepository;
//import org.sonar.api.rules.RulesCategory;
import org.sonar.plugins.cxx.CxxLanguage;

public final class CxxRatsRuleRepository extends RuleRepository {
  public static final String REPOSITORY_KEY = CxxPlugin.KEY;
  private static final String XML_FILE = "/rats.xml";
  
  
  public CxxRatsRuleRepository()
  {
    super(REPOSITORY_KEY, CxxLanguage.KEY);
    setName("rats");
  }
  
  @Override
  public List<Rule> createRules() {
    final XMLRuleParser xmlParser = new XMLRuleParser();
    final InputStream xmlStream = getClass().getResourceAsStream(XML_FILE);
    return xmlParser.parse(xmlStream);
    
  }

}
