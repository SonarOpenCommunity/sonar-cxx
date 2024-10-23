/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2024 SonarOpenCommunity
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
package org.sonar.cxx.squidbridge.rules;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;

@Beta
public class ExternalDescriptionLoader {

  private final String resourceBasePath;

  public ExternalDescriptionLoader(String resourceBasePath) {
    this.resourceBasePath = resourceBasePath;
  }

  public static void loadHtmlDescriptions(NewRepository repository, String languageKey) {
    var loader = new ExternalDescriptionLoader(languageKey);
    for (var newRule : repository.rules()) {
      loader.addHtmlDescription(newRule);
    }
  }

  public void addHtmlDescription(NewRule rule) {
    URL resource = ExternalDescriptionLoader.class.getResource(resourceBasePath + "/" + rule.key() + ".html");
    if (resource != null) {
      addHtmlDescription(rule, resource);
    }
  }

  @VisibleForTesting
  void addHtmlDescription(NewRule rule, URL resource) {
    try {
      rule.setHtmlDescription(Resources.toString(resource, StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read: " + resource, e);
    }
  }

}
