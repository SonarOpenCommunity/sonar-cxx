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
package org.sonar.cxx.sensors.squid;

import java.util.Arrays;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.cxx.CxxLanguage;
import org.sonar.squidbridge.annotations.AnnotationBasedRulesDefinition;

/**
 *
 * @author jocs
 */
@ScannerSide
public abstract class CustomCxxRulesDefinition implements RulesDefinition {

  @Override
  public void define(RulesDefinition.Context context) {
    RulesDefinition.NewRepository repo = context.createRepository(repositoryKey(), getLanguage().getKey())
      .setName(repositoryName());

    // Load metadata from check classes' annotations
    new AnnotationBasedRulesDefinition(repo, getLanguage().getKey()).addRuleClasses(false,
      Arrays.asList(checkClasses()));

    repo.done();
  }

  /**
   * Name of the custom rule repository.
   *
   * @return
   */
  public abstract CxxLanguage getLanguage();

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
