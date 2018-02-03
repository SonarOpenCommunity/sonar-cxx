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
package org.sonar.cxx.cxxlint;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jocs
 */
public class CheckerData {

  private String id = "";
  private String templateId = "";
  private boolean enabled = true;
  private HashMap<String, String> parameterData = new HashMap<>();

  public Map<String, String> getParameterData() {
    return parameterData;
  }

  public void setParameterData(Map<String, String> parameterData) {
    this.parameterData = (HashMap<String, String>) parameterData;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTemplateId() {
    return templateId;
  }

  public void setTemplateId(String templateId) {
    this.templateId = templateId;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnable(boolean active) {
    this.enabled = active;
  }
}
