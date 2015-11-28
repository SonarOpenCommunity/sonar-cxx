/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * sonarqube@googlegroups.com
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
package org.sonar.plugins.cxx.api;

import org.sonar.api.BatchExtension;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.squidbridge.annotations.AnnotationBasedRulesDefinition;

import com.google.common.collect.ImmutableList;

public abstract class CustomCxxRulesDefinition implements RulesDefinition, BatchExtension {
    
  @Override
  public void define(RulesDefinition.Context context) {
    RulesDefinition.NewRepository repo = context.createRepository(repositoryKey(), CxxLanguage.KEY)
          .setName(repositoryName());

    // Load metadata from check classes' annotations
    new AnnotationBasedRulesDefinition(repo, CxxLanguage.KEY).addRuleClasses(false,
          ImmutableList.copyOf(checkClasses()));

    repo.done();
  }

  /**
   * Name of the custom rule repository.
   */
  public abstract String repositoryName();

  /**
   * Key of the custom rule repository.
   */
  public abstract String repositoryKey();

  /**
   * Array of the custom rules classes.
   */
  @SuppressWarnings("rawtypes")
  public abstract Class[] checkClasses();

}
