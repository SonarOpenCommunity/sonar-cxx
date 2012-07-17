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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.sonar.plugins.cxx.ast.cpp.CxxClass;
import org.sonar.plugins.cxx.ast.cpp.HasClasses;

/**
 * Holds C++ classes
 * @author Przemyslaw Kociolek
 */
public class ClassHolder implements HasClasses {

  private Set<CxxClass> classes = new HashSet<CxxClass>();

  public ClassHolder() {
    super();
  }

  public Set<CxxClass> getClasses() {
    return classes;
  }

  public void addClass(CxxClass newClass) {
    if(newClass != null) {
      classes.add(newClass);
    }
  }

  public CxxClass findClassByName(String className) {
    Iterator<CxxClass> it = classes.iterator();
    while(it.hasNext()) {
      CxxClass clazz = it.next();
      if(clazz.getFullName().equals(className) || clazz.getName().equals(className)) {
        return clazz;
      }
    }
    
    return null;
  }

}