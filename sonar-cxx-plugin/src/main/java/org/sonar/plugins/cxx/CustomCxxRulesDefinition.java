/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.plugins.cxx;

import java.util.Arrays;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.cxx.squidbridge.annotations.AnnotationBasedRulesDefinition;

/**
 *
 * @author jocs
 */
@ScannerSide
public abstract class CustomCxxRulesDefinition implements RulesDefinition {

  @Override
  public void define(RulesDefinition.Context context) {
    var repo = context.createRepository(repositoryKey(), CxxLanguage.KEY)
      .setName(repositoryName());

    // Load metadata from check classes' annotations
    new AnnotationBasedRulesDefinition(repo, CxxLanguage.KEY).addRuleClasses(false,
                                                                             Arrays.asList(checkClasses()));

    repo.done();
  }

  /**
   * Name of the custom rule repository.
   *
   * @return
   */
  public abstract String repositoryName();

  /**
   * Key of the custom rule repository.
   *
   * @return
   */
  public abstract String repositoryKey();

  /**
   * Array of the custom rules classes.
   */
  @SuppressWarnings("rawtypes")
  public abstract Class[] checkClasses();

}
