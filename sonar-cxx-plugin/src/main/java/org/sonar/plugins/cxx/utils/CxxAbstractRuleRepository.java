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
package org.sonar.plugins.cxx.utils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.rules.XMLRuleParser;
import org.sonar.plugins.cxx.CxxLanguage;

/**
 * {@inheritDoc}
 */
public abstract class CxxAbstractRuleRepository extends RuleRepository {

  private final ServerFileSystem fileSystem;
  private final XMLRuleParser xmlRuleParser;
  protected final String repositoryKey;

  /**
   * {@inheritDoc}
   */
  public CxxAbstractRuleRepository(ServerFileSystem fileSystem, XMLRuleParser xmlRuleParser, String key) {
    super(key, CxxLanguage.KEY);
    this.fileSystem = fileSystem;
    this.xmlRuleParser = xmlRuleParser;
    this.repositoryKey = key;
  }

  @Override
  public List<Rule> createRules() {
    List<Rule> rules = new ArrayList<Rule>();

    final XMLRuleParser xmlParser = new XMLRuleParser();
    final InputStream xmlStream = getClass().getResourceAsStream(fileName());
    rules.addAll(xmlParser.parse(xmlStream));

    for (File userExtensionXml : fileSystem.getExtensions(repositoryKey, "xml")) {
      rules.addAll(xmlRuleParser.parse(userExtensionXml));
    }

    return rules;
  }
  
  protected abstract String fileName();
}
