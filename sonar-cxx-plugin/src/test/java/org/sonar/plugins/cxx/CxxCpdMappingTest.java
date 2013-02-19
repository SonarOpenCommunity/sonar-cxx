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

package org.sonar.plugins.cxx;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sonar.api.resources.ProjectFileSystem;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class CxxCpdMappingTest {
  @Test
  public void testMapping() {
    CxxLanguage language = mock(CxxLanguage.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    CxxCpdMapping mapping = new CxxCpdMapping(language, fs);
    assertThat(mapping.getLanguage()).isSameAs(language);
    assertThat(mapping.getTokenizer()).isInstanceOf(CxxTokenizer.class);
  }
}
