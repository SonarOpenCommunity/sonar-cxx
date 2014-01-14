/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
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
package org.sonar.plugins.cxx;

import org.junit.Test;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class CxxCpdMappingTest {
  @Test
  public void testMapping() {
    CxxLanguage language = mock(CxxLanguage.class);
    ModuleFileSystem fs = mock(ModuleFileSystem.class);
    CxxCpdMapping mapping = new CxxCpdMapping(language, fs);
    assertThat(mapping.getLanguage()).isSameAs(language);
    assertThat(mapping.getTokenizer()).isInstanceOf(CxxTokenizer.class);
  }
}
