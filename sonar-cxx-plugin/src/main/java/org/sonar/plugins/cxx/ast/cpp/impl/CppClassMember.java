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

import org.sonar.plugins.cxx.ast.cpp.CxxClassMember;
import org.sonar.plugins.cxx.ast.cpp.impl.internal.CommonType;

/**
 * Class member
 * @author Przemyslaw Kociolek
 */
public class CppClassMember extends CommonType implements CxxClassMember {

  /**
   * Ctor
   * @param name  member name
   */
  public CppClassMember(String name, String type) {
    super(name, type);
  }
    
  /**
   * @return member full name with type - "memberName:memberType"
   */
  public String getFullName() {
    return getName()+":"+getType();
  }
  
  @Override
  public boolean equals(Object o) {
    if(!(o instanceof CxxClassMember)) {
      return false;
    }
    
    CxxClassMember another = (CxxClassMember)o;
    return another.getFullName().equals(getFullName());
  }
  
  @Override
  public int hashCode() {
    return getFullName().hashCode();
  }
  
  @Override
  public String toString() {
     return getFullName();
  }
  
}
