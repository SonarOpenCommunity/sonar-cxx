/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2022 SonarOpenCommunity
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
package org.sonar.cxx.squidbridge.indexer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.sonar.cxx.squidbridge.api.Query;
import org.sonar.cxx.squidbridge.api.SourceCode;
import org.sonar.cxx.squidbridge.api.SourceCodeIndexer;
import org.sonar.cxx.squidbridge.api.SourceCodeSearchEngine;

public class SquidIndex implements SourceCodeIndexer, SourceCodeSearchEngine {

  private final Map<String, SourceCode> index = new TreeMap<>();

  @Override
  public Collection<SourceCode> search(Query... query) {
    Set<SourceCode> result = new HashSet<>();
    for (var unit : index.values()) {
      if (isSquidUnitMatchQueries(unit, query)) {
        result.add(unit);
      }
    }
    return result;
  }

  private boolean isSquidUnitMatchQueries(SourceCode unit, Query... queries) {
    boolean match;
    for (var query : queries) {
      match = query.match(unit);
      if (!match) {
        return false;
      }
    }
    return true;
  }

  @Override
  public SourceCode search(String key) {
    return index.get(key);
  }

  @Override
  public void index(SourceCode sourceCode) {
    sourceCode.setSourceCodeIndexer(this);
    index.put(sourceCode.getKey(), sourceCode);
  }
}
