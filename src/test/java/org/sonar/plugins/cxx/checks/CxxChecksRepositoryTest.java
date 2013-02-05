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

import static org.junit.Assert.*;
import org.junit.Test;
import org.sonar.api.checks.AnnotationCheckFactory;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.plugins.cxx.TestUtils;
/**
 *
 * @author Jorge Costa
 */
public class CxxChecksRepositoryTest {

  @Test
  public void properties() {
    assertEquals(CxxChecksRepository.REPOSITORY_KEY, "CxxCommon-c++");
    assertEquals(CxxChecksRepository.REPOSITORY_NAME, "Cxx Sonar");
    assertEquals(CxxChecksRepository.SONAR_WAY_PROFILE_NAME, "Sonar way");
  }
  
  @Test
  public void getCheckClasses() {
    assertEquals(CxxChecksRepository.getCheckClasses().size(), CxxChecksRepository.getChecks().size());
  }   
  
  @Test
  public void getCheckNoActiveRules() {
    AnnotationCheckFactory annotationCheckFactory = AnnotationCheckFactory.create(TestUtils.createStandardEmptyRulesProfile(), CxxChecksRepository.REPOSITORY_KEY, CxxChecksRepository.getChecks());
    assertEquals(null, CxxChecksRepository.getCheck(annotationCheckFactory, CxxMaximumComplexity.getMyself()));
  }     
  
}
