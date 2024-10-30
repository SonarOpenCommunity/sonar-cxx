/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2024 SonarOpenCommunity
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
package org.sonar.cxx.squidbridge.api;

import java.util.Collection;

/**
 * Interface to defines classes to search for SourceCode objects in the SourceCode tree.
 *
 * @see SourceCode
 */
public interface SourceCodeSearchEngine {

  /**
   * Search for a SourceCode object with the given key in the SourceCode tree.
   *
   * @param key unique key of a SourceCode object
   * @return found SourceCode object or null
   */
  SourceCode search(String key);

  /**
   * Search for a SourceCode objects with the given Query objects in the SourceCode tree. At least one of the queries
   * must be positive in order to insert the SourceCode object into the collection.
   *
   * @param query Query objects to use for the search
   * @return collection of found SourceCode objects
   *
   * @see Query
   */
  Collection<SourceCode> search(Query... query);
}
