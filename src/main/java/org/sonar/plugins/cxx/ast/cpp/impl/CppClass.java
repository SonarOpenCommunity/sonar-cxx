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
import java.util.Iterator;
import java.util.Set;

import org.sonar.plugins.cxx.ast.cpp.CxxClass;
import org.sonar.plugins.cxx.ast.cpp.CxxClassMember;
import org.sonar.plugins.cxx.ast.cpp.CxxClassMethod;
import org.sonar.plugins.cxx.ast.cpp.CxxNamespace;
import org.sonar.plugins.cxx.ast.cpp.impl.internal.CommonNamespace;

/**
 * CppClass holds information about class members and methods
 * @author Przemyslaw Kociolek
 */
public class CppClass extends CommonNamespace implements CxxClass {
    
  public static final String DEFAULT_NAME = "CxxCppDefaultClassName";  
  private Set<CxxClassMember> members = new HashSet<CxxClassMember>();
  private Set<CxxClassMethod> methods = new HashSet<CxxClassMethod>();
  private Set<CxxClass> ancestors = new HashSet<CxxClass>();
  
  /**
   * Default ctor, sets everything to default values (name, namespace)
   */
  public CppClass() {
    super(DEFAULT_NAME);
  }
  
  /**
   * Ctor
   * @param name  Class name
   */
  public CppClass(String name) {
    super(name);
  }

  /**
   * Ctor
   * @param namespace class namespace
   * @param name  class name
   */
  public CppClass(CxxNamespace namespace, String name) {
    super(name);
    this.namespace = validateNamespace(namespace);
  }

  private CxxNamespace validateNamespace(CxxNamespace namespace) {
    return namespace == null ? CppNamespace.DEFAULT_NAMESPACE : namespace;
  }

  public String getFullName() {
    return namespace.getFullName() + CppNamespace.SEPARATOR + getName();
  }
  
  public Set<CxxClassMember> getMembers() {
    return members;
  }

  public void addMember(CxxClassMember classMember) {
    if(classMember != null) {
      members.add(classMember);
    }
  }

  public Set<CxxClassMethod> getMethods() {
    return methods;
  }

  public void addMethod(CxxClassMethod newMethod) {
    if(newMethod != null) {
      methods.add(newMethod);
    }
  }
  
  public CxxClassMethod findMethodByName(String methodName) {
    Iterator<CxxClassMethod> it = methods.iterator();
    while(it.hasNext()) {
      CxxClassMethod method = it.next();
      if(method.getName().equals(methodName)) {
        return method;
      }
    }
    return null;
  }

  public CxxClassMember findMemberByName(String memberName) {
    Iterator<CxxClassMember> it = members.iterator();
    while(it.hasNext()) {
      CxxClassMember member = it.next();
      if(member.getName().equals(memberName)) {
        return member;
      }
    }
    return null;
  }

  public Set<CxxClass> getAncestors() {
    return ancestors;
  }

  public void addAncestor(CxxClass ancestor) {
    if(ancestor != null && !this.equals(ancestor)) {
      ancestors.add(ancestor);
    }
  }
    
  
  @Override
  public boolean equals(Object o) {
    if(!(o instanceof CxxClass)) {
      return false;
    }
    
    CxxClass other = (CxxClass) o;
    boolean nameOk = other.getFullName().equals( getFullName() );
    boolean ancestorsOk = other.getAncestors().equals(getAncestors());
  
    return nameOk && ancestorsOk;
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