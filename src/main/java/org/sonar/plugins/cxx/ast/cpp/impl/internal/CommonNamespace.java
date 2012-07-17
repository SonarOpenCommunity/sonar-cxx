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

import org.sonar.plugins.cxx.ast.cpp.HasNamespace;
import org.sonar.plugins.cxx.ast.cpp.CxxNamespace;
import org.sonar.plugins.cxx.ast.cpp.impl.CppNamespace;

public abstract class CommonNamespace extends CommonName implements HasNamespace {

  protected CxxNamespace namespace = null;

  /**
   * @param name  name
   * @param defaultName default name
   * @param namespace
   */
  public CommonNamespace(String name, String defaultName, CxxNamespace namespace) {
    super(name, defaultName);
    this.namespace = namespace;
  }
  
  /**
   * Ctor
   * @param name  element name
   * @param namespace element namespace (can be null)
   */
  public CommonNamespace(String name, CxxNamespace namespace) {
    super(name);
    this.namespace = namespace;
  }
  
  /**
   * Sets the owner namespace to default
   * @param name  element name
   */
  public CommonNamespace(String name) {
    this(name, CppNamespace.DEFAULT_NAMESPACE);
  }

  /**
   * @return owner namespace
   */
  public CxxNamespace getNamespace() {
    return namespace;
  }

  /**
   * @param namespace new namespace to set
   * @throws IllegalArgumentException if you try to set current namespace as it's own parent 
   */
  public void setNamespace(CxxNamespace namespace) {
    if(namespace == this) {
      throw new IllegalArgumentException("Can't set namespace parent to self!");
    }
    this.namespace = namespace;
  }

}