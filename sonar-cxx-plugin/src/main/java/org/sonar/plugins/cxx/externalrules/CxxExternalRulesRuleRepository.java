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
package org.sonar.plugins.cxx.externalrules;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.rules.XMLRuleParser;

/**
 * Loads the external rules configuration file.
 */
public class CxxExternalRulesRuleRepository extends RuleRepository {

  public static final String REPOSITORY_KEY = "cxxexternal";

  // for user extensions
  private final ServerFileSystem fileSystem;
  private final XMLRuleParser xmlRuleParser;

  public CxxExternalRulesRuleRepository(ServerFileSystem fileSystem, XMLRuleParser xmlRuleParser) {
    super(REPOSITORY_KEY, "c++");
    setName(REPOSITORY_KEY);
    this.fileSystem = fileSystem;
    this.xmlRuleParser = xmlRuleParser;
  }

  @Override
  public List<Rule> createRules() {
    List<Rule> rules = new ArrayList<Rule>();
    for (File userExtensionXml : fileSystem.getExtensions(REPOSITORY_KEY, "xml")) {
      rules.addAll(xmlRuleParser.parse(userExtensionXml));
    }
    return rules;
  }
}
