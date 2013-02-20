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
package org.sonar.plugins.cxx.utils;


public class CxxOsValidator {
  
  public static CxxOsType getOSType() {
    if(isWindows()) {
      return CxxOsType.WINDOWS;
    } else if(isMac()) {
      return CxxOsType.MAC;
    } else if(isUnix()) {
      return CxxOsType.UNIX;
    } else if(isSolaris()) {
      return CxxOsType.SOLARIS;
    } else {
      return CxxOsType.UNKNOWN;
    }
  }
  
  public static boolean isWindows() {    
    String os = System.getProperty("os.name").toLowerCase();
    return (os.indexOf("win") >= 0); 
  }
 
  public static boolean isMac() { 
    String os = System.getProperty("os.name").toLowerCase();
    return (os.indexOf("mac") >= 0); 
  }
 
  public static boolean isUnix() {
    String os = System.getProperty("os.name").toLowerCase();
    return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
  }
 
  public static boolean isSolaris() {
    String os = System.getProperty("os.name").toLowerCase();
    return (os.indexOf("sunos") >= 0);
  }
}
