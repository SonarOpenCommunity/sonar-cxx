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
package org.sonar.plugins.c;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.cxx.CxxLanguage;
import org.sonar.squidbridge.annotations.AnnotationBasedRulesDefinition;

public class CRuleRepository implements RulesDefinition {

  private static final String REPOSITORY_NAME = "c SonarQube";
  private final CxxLanguage language;

  public CRuleRepository(CxxLanguage language) {
    this.language = language;
  }

  @Override
  public void define(Context context) {
    NewRepository repository = context.
      createRepository(this.language.getRepositoryKey(), CLanguage.KEY).
      setName(REPOSITORY_NAME);
    new AnnotationBasedRulesDefinition(repository, CLanguage.KEY).addRuleClasses(false, this.language.getChecks());
    repository.done();
  }
}
