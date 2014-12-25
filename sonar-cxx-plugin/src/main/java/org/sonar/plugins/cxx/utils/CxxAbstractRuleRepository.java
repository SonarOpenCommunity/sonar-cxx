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
package org.sonar.plugins.cxx.utils;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Settings;
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
  public final Settings settings;
  private final XMLRuleParser xmlRuleParser;
  protected final String repositoryKey;
  protected final String customRepositoryKey;

  /**
   * {@inheritDoc}
   */
  public CxxAbstractRuleRepository(ServerFileSystem fileSystem, XMLRuleParser xmlRuleParser, Settings settings, String key, String customKey) {
    super(key, CxxLanguage.KEY);
    this.fileSystem = fileSystem;
    this.xmlRuleParser = xmlRuleParser;
    this.repositoryKey = key;
    this.customRepositoryKey = customKey;
    this.settings = settings;
  }

  @Override
  public List<Rule> createRules() {
    List<Rule> rules = new ArrayList<Rule>();

    final XMLRuleParser xmlParser = new XMLRuleParser();
    if(!"".equals(fileName())) {
      final InputStream xmlStream = getClass().getResourceAsStream(fileName());
      rules.addAll(xmlParser.parse(xmlStream));

      for (File userExtensionXml : fileSystem.getExtensions(repositoryKey, "xml")) {
        rules.addAll(xmlRuleParser.parse(userExtensionXml));
      }
    }

    String customRules = settings.getString(this.customRepositoryKey);
    if (StringUtils.isNotBlank(customRules)) {
      rules.addAll(xmlRuleParser.parse(new StringReader(customRules)));
    }

    return rules;
  }

  protected abstract String fileName();
}
