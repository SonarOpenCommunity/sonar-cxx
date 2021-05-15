/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/**
 * fork of SSLR Squid Bridge: https://github.com/SonarSource/sslr-squid-bridge/tree/2.6.1
 * Copyright (C) 2010 SonarSource / mailto: sonarqube@googlegroups.com / license: LGPL v3
 */
package org.sonar.cxx.squidbridge.annotations;

/**
 * @deprecated since 2.6. Each plugin should define its own list of tags.
 */
@Deprecated
public class Tags {

  public static final String ACCESSIBILITY = "accessibility";
  public static final String ASSEMBLER = "assembler";
  public static final String BRAIN_OVERLOAD = "brain-overload";
  public static final String BUG = "bug";
  public static final String CERT = "cert";
  public static final String CLUMSY = "clumsy";
  public static final String CONVENTION = "convention";
  public static final String CROSS_BROWSER = "cross-browser";
  public static final String CWE = "cwe";
  public static final String ERROR_HANDLING = "error-handling";
  public static final String HTML5 = "html5";
  public static final String JAVA7 = "java7";
  public static final String JAVA8 = "java8";
  public static final String MISRA = "misra";
  public static final String MISRA_C = "misra-c";
  public static final String MISRA_CPP = "misra-c++";
  public static final String MULTI_THREADING = "multi-threading";
  public static final String OBSOLETE = "obsolete";
  public static final String OWASP_TOP10 = "owasp-top10";
  public static final String PERFORMANCE = "performance";
  public static final String PITFALL = "pitfall";
  public static final String PREPROCESSOR = "preprocessor";
  public static final String PSR1 = "psr1";
  public static final String PSR2 = "psr2";
  public static final String SECURITY = "security";
  public static final String SQL = "sql";
  public static final String UNUSED = "unused";
  public static final String USER_EXPERIENCE = "user-experience";

  private Tags() {
    // This class only defines constants
  }

}
