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

import org.sonar.api.checks.AnnotationCheckFactory;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.cxx.CxxSourceCode;

/**
 * Abtract superclass for checks.
 *
 * @author Jorge Costa
 */
public abstract class CxxAbstractCheck {

  protected final void createViolation(Rule rule, CxxSourceCode cxxSource, Integer linePosition) {
    createViolation(rule, cxxSource, linePosition, rule.getDescription(), null);
  }
  
  protected final void createViolation(Rule rule, CxxSourceCode cxxSource, Integer linePosition, String message) {
    createViolation(rule, cxxSource, linePosition, message, null);
  }  

  protected final void createViolation(Rule rule, CxxSourceCode cxxSource, Integer linePosition, String message, Double cost) {
    Violation violation = Violation.create(rule, cxxSource.getResource());
    violation.setMessage(message);
    violation.setLineId(linePosition);
    if(cost != null) {
      violation.setCost(cost.doubleValue());
    }
    cxxSource.addViolation(violation);
  }

  public static void validateRule(AnnotationCheckFactory annotationCheckFactory, CxxSourceCode sourceCode, org.sonar.api.rules.Rule rule) {
    CxxAbstractCheck check = CxxChecksRepository.getCheck(annotationCheckFactory, rule);    
    if(check != null) {
      check.validate(annotationCheckFactory.getActiveRule(check).getRule(), sourceCode);  
    }
  } 
  
  public abstract void validate(Rule rule, CxxSourceCode cxxSourceCode);  
}
