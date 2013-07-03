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

import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.rules.XMLRuleParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the external rules configuration file.
 */
public class CxxExternalRuleRepository extends RuleRepository {

  public static final String REPOSITORY_KEY = "cxxexternal";

  // for user extensions
  private final ServerFileSystem fileSystem;
  private final XMLRuleParser xmlRuleParser;

  public CxxExternalRuleRepository(ServerFileSystem fileSystem, XMLRuleParser xmlRuleParser) {
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
