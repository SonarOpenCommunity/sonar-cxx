/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
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
package org.sonar.cxx.preprocessor;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

public final class StandardDefinitions {
  private StandardDefinitions(){}

  public static Map<String, String> macros(){
    // This is a collection of standard macros according to
    // http://gcc.gnu.org/onlinedocs/cpp/Standard-Predefined-Macros.html

    return ImmutableMap.<String, String>builder()
      .put("__FILE__", "\"file\"")         // for now
      .put("__LINE__", "1")            // that should hopefully suffice
      .put("__DATE__", "\"??? ?? ????\"")  // indicates 'date unknown'. should suffice
      .put("__TIME__", "\"??:??:??\"")     // indicates 'time unknown'. should suffice
      .put("__STDC__", "1")
      .put("__STDC_HOSTED__", "1")
      .put("__cplusplus", "1")
      .build();
  }
}
