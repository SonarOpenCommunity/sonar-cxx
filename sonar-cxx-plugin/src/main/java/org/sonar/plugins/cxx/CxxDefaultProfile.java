/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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

import org.sonar.api.profiles.AnnotationProfileParser;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.profiles.XMLProfileParser;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.cxx.checks.CheckList;

/**
 * {@inheritDoc}
 */
public class CxxDefaultProfile extends ProfileDefinition {

  private final XMLProfileParser xmlProfileParser;
  private final AnnotationProfileParser annotationProfileParser;
  private static final String NAME = "Sonar way";

  /**
   * {@inheritDoc}
   */
  public CxxDefaultProfile(XMLProfileParser xmlProfileParser, AnnotationProfileParser annotationProfileParser) {
    this.annotationProfileParser = annotationProfileParser;
    this.xmlProfileParser = xmlProfileParser;
  }

  @Override
  public RulesProfile createProfile(ValidationMessages messages) {
    RulesProfile profile = xmlProfileParser.parseResource(getClass().getClassLoader(),
      "default-profile.xml", messages);
    RulesProfile sonarRules = annotationProfileParser.parse(CheckList.REPOSITORY_KEY, NAME,
      CppLanguage.KEY, CheckList.getChecks(), messages);
    for (ActiveRule activeRule : sonarRules.getActiveRules()) {
      profile.addActiveRule(activeRule);
    }

    profile.setDefaultProfile(Boolean.TRUE);
    return profile;
  }
}
