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

package org.sonar.plugins.cxx.cppncss;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.cxx.TestUtils;
import org.sonar.plugins.cxx.checks.CxxMaximumComplexity;
import static org.junit.Assert.assertEquals;

public class CxxCppNcssSensorTest {
  private CxxCppNcssSensor sensor;
  private SensorContext context;
  private Project project;
  private RulesProfile rulesProfile;  


  public RulesProfile createStandardRulesProfile() {
    ProfileDefinition profileDefinition = TestUtils.getProfileDefinition();

    ValidationMessages messages = ValidationMessages.create();
    RulesProfile profile = profileDefinition.createProfile(messages);
    profile.activateRule(CxxMaximumComplexity.getMyself(), RulePriority.INFO);
    
    assertEquals(0, messages.getErrors().size());
    assertEquals(0, messages.getWarnings().size());
    assertEquals(0, messages.getInfos().size());
    return profile;
  }
  
  @Before
  public void setUp() {
    project = TestUtils.mockProject();
    rulesProfile = createStandardRulesProfile();
    sensor = new CxxCppNcssSensor(new Settings(), rulesProfile);
    
    context = mock(SensorContext.class);
    Resource resourceMock = mock(Resource.class);
    when(context.getResource((Resource)anyObject())).thenReturn(resourceMock);
  }

  @Test
  public void shouldReportCorrectViolations() {
    sensor.analyse(project, context);

    verify(context, times(5)).saveMeasure((Resource) anyObject(),
                                          eq(CoreMetrics.FUNCTIONS), anyDouble());
    verify(context, times(5)).saveMeasure((Resource) anyObject(),
                                          eq(CoreMetrics.COMPLEXITY), anyDouble());
    verify(context, times(15)).saveMeasure((Resource) anyObject(), any(Measure.class));
    
    verify(context, times(1)).saveViolation(any(Violation.class));
  }
}
