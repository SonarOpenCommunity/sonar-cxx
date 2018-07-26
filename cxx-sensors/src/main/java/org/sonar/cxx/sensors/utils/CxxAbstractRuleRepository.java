/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.cxx.sensors.utils;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.cxx.CxxLanguage;

/**
 * {@inheritDoc}
 */
public abstract class CxxAbstractRuleRepository implements RulesDefinition {
  protected final String repositoryKey;
  protected final String repositoryName;
  protected final String customRepositoryKey;

  /**
   * {@inheritDoc}
   */
  public CxxAbstractRuleRepository(
          String key,
          String name,
          String customKey) {
    this.repositoryKey = key;
    this.repositoryName = name;
    this.customRepositoryKey = customKey;
  }

  public static String getRepositoryKey(String key, CxxLanguage lang) {
    return key + lang.getRepositorySuffix();
  }

  @Override
  public void define(Context context) {
    CreateRuleRepository(context, "c++", "cpp-" + this.repositoryKey);
    CreateRuleRepository(context, "c", "c-" + this.repositoryKey);
  }

  private void CreateRuleRepository(Context context, String languageKey, String repoKey) {
    Charset charset = StandardCharsets.UTF_8;
    NewRepository repository = context.createRepository(repoKey, languageKey).setName(this.repositoryName);

    RulesDefinitionXmlLoader xmlLoader = new RulesDefinitionXmlLoader();
    if (!"".equals(fileName())) {
      InputStream xmlStream = getClass().getResourceAsStream(fileName());
      xmlLoader.load(repository, xmlStream, charset);
    }

    repository.done();
  }

  protected abstract String fileName();
}
