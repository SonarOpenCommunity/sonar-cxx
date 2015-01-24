/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
package org.sonar.plugins.cxx.squid;

import org.sonar.api.resources.Language;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.Scopes;

/**
 * @author metselaara
 *
 */
public class Method extends Resource {
  private Language language;
  private Resource parent;
  public Method(String key, Language language, Resource parent) {
    super.setKey(key);
    this.language = language;
    this.parent = parent;
  }

  /* (non-Javadoc)
   * @see org.sonar.api.resources.Resource#getName()
   */
  @Override
  public String getName() {
    return getKey();
  }

  /* (non-Javadoc)
   * @see org.sonar.api.resources.Resource#getLongName()
   */
  @Override
  public String getLongName() {
    return getKey();
  }

  /* (non-Javadoc)
   * @see org.sonar.api.resources.Resource#getDescription()
   */
  @Override
  public String getDescription() {
    return language.getName() + " method or function " + getKey() + " in " + parent.getName();
  }

  @Override
  public String toString(){
    return getClass().getSimpleName() + ":" + getKey();
  }
  
  /* (non-Javadoc)
   * @see org.sonar.api.resources.Resource#getLanguage()
   */
  @Override
  public Language getLanguage() {
    return language;
  }

  /* (non-Javadoc)
   * @see org.sonar.api.resources.Resource#getScope()
   */
  @Override
  public String getScope() {
    return Scopes.BLOCK_UNIT;
  }

  /* (non-Javadoc)
   * @see org.sonar.api.resources.Resource#getQualifier()
   */
  @Override
  public String getQualifier() {
    return Qualifiers.METHOD;
  }

  /* (non-Javadoc)
   * @see org.sonar.api.resources.Resource#getParent()
   */
  @Override
  public Resource getParent() {
    return parent;
  }

  /* (non-Javadoc)
   * @see org.sonar.api.resources.Resource#matchFilePattern(java.lang.String)
   */
  @Override
  public boolean matchFilePattern(String antPattern) {
    // TODO Auto-generated method stub
    return false;
  }

  public static Method createMethod(String key, Language language, Resource parent) {
    Method result = new Method(key, language, parent);
    return result;
  }

}
