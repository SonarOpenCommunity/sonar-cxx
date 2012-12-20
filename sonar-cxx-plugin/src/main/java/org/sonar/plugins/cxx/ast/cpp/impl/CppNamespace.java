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
package org.sonar.plugins.cxx.ast.cpp.impl;

import java.util.HashSet;
import java.util.Set;

import org.sonar.plugins.cxx.ast.cpp.CxxClass;
import org.sonar.plugins.cxx.ast.cpp.CxxNamespace;
import org.sonar.plugins.cxx.ast.cpp.impl.internal.CommonNamespace;

/**
 * Cpp namespace class. Holds information about classes in that namespace.
 * @author Przemyslaw Kociolek
 */
public class CppNamespace extends CommonNamespace implements CxxNamespace {

  public static final CppNamespace  DEFAULT_NAMESPACE = new CppNamespace();
  public static final String        DEFAULT_NAME      = "global";
  public static final String        SEPARATOR         = "::"; 

  private Set<CxxClass>   classes  = new HashSet<CxxClass>();

  /**
   * Default ctor, set everything to default values
   */
  public CppNamespace() {
    super(DEFAULT_NAME, null);
  }

  /**
   * Ctor
   * @param name  namespace name
   */
  public CppNamespace(String name) {
    super(name, DEFAULT_NAME, null);
  }

  /**
   * @return  set of classes in that namespace, empty set if no classes are present
   */
  public Set<CxxClass> getClasses() {
    return classes;
  }

  /**
   * @param cppClass  class to add to namespace
   * @remark cppClass namespace will be automatically set to this namespace!
   */
  public void addClass(CxxClass cppClass) {
    cppClass.setNamespace(this);
    classes.add(cppClass);
  }

  @Override
  public boolean equals(Object o) {
    if(!(o instanceof CxxNamespace)) {
      return false;
    }

    return ((CxxNamespace)o).getFullName().equals( getFullName() );
  }

  @Override
  public int hashCode() {
    return getFullName().hashCode();
  }

  /**
   * @return  full name, with parents namespace names and '::' qualifiers
   */
  public String getFullName() {
    if(getNamespace() != null) {
      return getNamespace().getFullName() + CppNamespace.SEPARATOR + getName();
    }

    return getName();
  }

}
