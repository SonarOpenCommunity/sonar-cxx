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
