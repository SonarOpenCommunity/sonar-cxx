/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.ast.cpp.impl.internal;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.cxx.ast.cpp.HasFullName;

public abstract class CommonName implements HasFullName {

  private String name = null;

  /**
   * Ctor
   * @param name  name
   */
  public CommonName(String name) {
    this.name = validateString(name, "Can't set empty/null name.");
  }
  
  /**
   * Ctor
   * @param name  name
   * @param defaultName default name if the first one is invalid
   */
  public CommonName(String name, String defaultName) {
    try {
      this.name = validateString(name, "Can't set empty/null name.");
    } catch (IllegalArgumentException e) {
      this.name = validateString(defaultName, "Can't set empty/null name from default name.");
    }
  }

  /**
   * @return element name
   */
  public String getName() {
    return name;
  }
  
  /**
   * @return newName element name
   * @throws IllegalArgumentException if name is empty or null
   */
  public void setName(String newName) {
    name = validateString(newName, "Can't set empty/null name."); 
  }
  
  protected final String validateString(String str, String msg) {
    if(StringUtils.isEmpty( StringUtils.trimToEmpty(str) )) {
      throw new IllegalArgumentException(msg);
    }
    return StringUtils.trimToEmpty(str);
  }
  
  @Override
  public String toString() {
    return getFullName();
  }

}
