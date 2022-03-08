/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021 SonarOpenCommunity
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
/**
 * fork of SSLR Squid Bridge: https://github.com/SonarSource/sslr-squid-bridge/tree/2.6.1
 * Copyright (C) 2010 SonarSource / mailto: sonarqube@googlegroups.com / license: LGPL v3
 */
package org.sonar.cxx.squidbridge.annotations;

import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.AnnotationUtils;
import org.sonar.api.utils.ValidationMessages;

/**
 * Utility class to build an instance of {@link RulesProfile} based on a list of classes annotated
 * with {@link ActivatedByDefault}.
 *
 * @since 2.5
 */
public class AnnotationBasedProfileBuilder {

  private final RuleFinder ruleFinder;

  public AnnotationBasedProfileBuilder(RuleFinder ruleFinder) {
    this.ruleFinder = ruleFinder;
  }

  public RulesProfile build(String repositoryKey, String profileName, String language, Iterable<Class> annotatedClasses,
                            ValidationMessages messages) {
    var profile = RulesProfile.create(profileName, language);
    for (var ruleClass : annotatedClasses) {
      addRule(ruleClass, profile, repositoryKey, messages);
    }
    return profile;
  }

  private void addRule(Class<?> ruleClass, RulesProfile profile, String repositoryKey, ValidationMessages messages) {
    if (AnnotationUtils.getAnnotation(ruleClass, ActivatedByDefault.class) != null) {
      var ruleAnnotation = AnnotationUtils.getAnnotation(ruleClass, org.sonar.check.Rule.class);
      if (ruleAnnotation == null) {
        messages.addWarningText("Class " + ruleClass + " has no Rule annotation");
        return;
      }
      String ruleKey = ruleAnnotation.key();
      var rule = ruleFinder.findByKey(repositoryKey, ruleKey);
      if (rule == null) {
        messages.addWarningText("Rule not found: [repository=" + repositoryKey + ", key=" + ruleKey + "]");
      } else {
        profile.activateRule(rule, null);
      }
    }
  }

}
