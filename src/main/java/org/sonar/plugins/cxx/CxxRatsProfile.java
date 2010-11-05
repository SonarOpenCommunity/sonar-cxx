/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 ${name}
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.cxx;

import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.profiles.XMLProfileParser;
import org.sonar.api.utils.ValidationMessages;


public final class CxxRatsProfile extends ProfileDefinition {
  private static final String PROFILE_FILE = "rats-profile.xml";  
  private XMLProfileParser xmlProfileParser;
  
  public CxxRatsProfile(XMLProfileParser xmlProfileParser)
  {
    this.xmlProfileParser = xmlProfileParser;
  }
  
  @Override
  public RulesProfile createProfile(ValidationMessages messages) {
    RulesProfile profile = xmlProfileParser.parseResource(getClass().getClassLoader(), PROFILE_FILE, messages);
    profile.setDefaultProfile(true);
    profile.setProvided(true);
    return profile;
  }
}
