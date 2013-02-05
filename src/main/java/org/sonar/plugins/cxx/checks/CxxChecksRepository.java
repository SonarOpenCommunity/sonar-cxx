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

package org.sonar.plugins.cxx.checks;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import org.sonar.api.checks.AnnotationCheckFactory;
import org.sonar.api.rules.Rule;
import org.sonar.plugins.cxx.CxxLanguage;

public final class CxxChecksRepository {

  public static final String REPOSITORY_KEY = "CxxCommon-" + CxxLanguage.KEY;
  public static final String REPOSITORY_NAME = "Cxx Sonar";
  public static final String SONAR_WAY_PROFILE_NAME = "Sonar way";

  private CxxChecksRepository() {
  }

  public static List<CxxAbstractCheck> getChecks() {
    return ImmutableList.<CxxAbstractCheck> of(
        new CxxMaximumComplexity());
  }

  public static CxxAbstractCheck getCheck(AnnotationCheckFactory annotationCheckFactory, Rule key) {
    Collection<CxxAbstractCheck> checks = annotationCheckFactory.getChecks();
    for (CxxAbstractCheck check : checks) {
      if (annotationCheckFactory.getActiveRule(check).getRule().getName().equals(key.getName())) {       
        return check;            
      }
    }
    return null;
  }
        
  public static List<Class> getCheckClasses() {
    ImmutableList.Builder<Class> builder = ImmutableList.builder();

    for (CxxAbstractCheck check : getChecks()) {
      builder.add(check.getClass());
    }

    return builder.build();
  }

}
