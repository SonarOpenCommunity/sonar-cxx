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

import java.util.ArrayList;
import java.util.List;

import org.sonar.plugins.cxx.ast.cpp.CxxClassMethod;
import org.sonar.plugins.cxx.ast.cpp.CxxClass;
import org.sonar.plugins.cxx.ast.cpp.CxxMethodArgument;
import org.sonar.plugins.cxx.ast.cpp.CxxMethodBody;
import org.sonar.plugins.cxx.ast.cpp.HasMethods;
import org.sonar.plugins.cxx.ast.cpp.impl.internal.CommonName;

/**
 * Cpp class method
 * @author Przemyslaw Kociolek
 */
public class CppClassMethod extends CommonName implements CxxClassMethod {

  private CxxClass ownerClass;
  private boolean implemented = false;
  private List<CxxMethodArgument> arguments = new ArrayList<CxxMethodArgument>();
  private CxxMethodBody methodBody  = new CppMethodBody();
  
  /**
   * Ctor
   * @param ownerClass cpp class that owns this method
   * @param methodName  method name
   */
  public CppClassMethod(CxxClass ownerClass, String methodName) {
    super(methodName);
    this.ownerClass = validateClass(ownerClass);
  }

  /**
   * @return class that owns this method
   */
  public HasMethods getOwnerClass() {
    return ownerClass;
  }

  /**
   * @return  full name, with namespaces
   */
  public String getFullName() {
    return ownerClass.getFullName() + CppNamespace.SEPARATOR + getName();
  }  

  /**
   * @return  method argument list
   */
  public List<CxxMethodArgument> getArguments() {
    return arguments;
  }

  /**
   * Adds method argument
   * @param argument new method argument
   */
  public void addArgument(CxxMethodArgument argument) {
    if(argument != null) {
      arguments.add(argument);
    }
  }
  
  public CxxMethodBody getBody() {
    return methodBody;
  }

  @Override
  public boolean equals(Object o) {
    if(!(o instanceof CxxClassMethod)) {
      return false;
    }
    
    CxxClassMethod another = (CxxClassMethod)o;
    if(another.getArguments().size() != arguments.size()) {
      return false;
    }
    
    for(int i = 0; i < arguments.size(); ++i) {
      if(!another.getArguments().get(i).equals(arguments.get(i))) {
        return false;
      }
    }
    
    return another.getBody().equals(methodBody);
  }
  
  @Override
  public int hashCode() {
    return getFullName().hashCode();
  }
  
  @Override
  public String toString() {
    return getFullName();
  }
  
  private CxxClass validateClass(CxxClass ownerClass) {
    if(ownerClass == null) {
      throw new IllegalArgumentException("Method owner class can't be null.");
    }
    return ownerClass;
  }

  public boolean isImplemented() {
    return implemented;
  }

  public void setImplemented(boolean value) {
    implemented = value;
  }

}
