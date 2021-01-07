/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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

import com.google.common.io.Resources;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.cxx.checks.CheckList;
import org.sonarsource.api.sonarlint.SonarLintSide;

/**
 * define built-in profile
 */
@SonarLintSide
public class CxxSonarWayProfile implements BuiltInQualityProfilesDefinition {

  private static String readResource(URL resource) {
    try {
      return Resources.toString(resource, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read: " + resource, e);
    }
  }

  static Profile readProfile() {
    URL resource = CxxSonarWayProfile.class.getResource("/org/sonar/l10n/cxx/rules/cxx/Sonar_way_profile.json");
    return new Gson().fromJson(readResource(resource), Profile.class);
  }

  @Override
  public void define(Context context) {
    NewBuiltInQualityProfile sonarWay = context.createBuiltInQualityProfile("Sonar way", CxxLanguage.KEY);
    Profile jsonProfile = readProfile();
    jsonProfile.ruleKeys.forEach((key) -> {
      sonarWay.activateRule(CheckList.REPOSITORY_KEY, key);
    });

    sonarWay.done();
  }

  static class Profile {

    public String name;
    public List<String> ruleKeys;
  }

}
