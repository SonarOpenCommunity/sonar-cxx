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

import javax.annotation.Nullable;
import org.sonar.cxx.squidbridge.api.Query;
import org.sonar.cxx.squidbridge.api.SourceCode;

public class QueryByName implements Query {

  private final String resourceName;

  public QueryByName(@Nullable String resourceName) {
    if (resourceName == null) {
      throw new IllegalStateException("The name can't be null !");
    }
    this.resourceName = resourceName;
  }

  @Override
  public boolean match(SourceCode unit) {
    if (unit.getName() != null) {
      return unit.getName().equals(resourceName);
    }
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }

    var other = (QueryByName) obj;

    if (resourceName != null ? !resourceName.equals(other.resourceName) : other.resourceName != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return resourceName.hashCode();
  }
}
