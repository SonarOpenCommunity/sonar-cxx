/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * sonarqube@googlegroups.com
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

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Settings;

public class CxxSettingsTest {

  private CxxSettings settings;

  @Before
  public void setUp() {
    // setting values must be before creating a CxxSettings object
    System.setProperty("cxx.test.key1", "value");
    System.setProperty("cxx.test.path1", "C:\\test\\reports\\a.xml");
    // clone CxxSettings from a Settings instance
    Settings base = new Settings();
    settings = new CxxSettings(base);
  }

  @Test
  public void replaceString() {
    settings.setProperty("key1", "${cxx.test.key1}");
    String value = settings.getString("key1");
    assertThat(value).isEqualTo("value");
  }

  @Test
  public void replaceStringList1() {
    settings.setProperty("key2", "${cxx.test.key1}, ${cxx.test.key1}");
    String value = settings.getString("key2");
    assertThat(value).isEqualTo("value, value");
  }

  @Test
  public void replaceStringList2() {
    settings.setProperty("key2", "${cxx.test.key1}, ${undefined}, xxx");
    String value = settings.getString("key2");
    assertThat(value).isEqualTo("value, ${undefined}, xxx");
  }

  @Test
  public void replaceStringBackslash() {
    settings.setProperty("key2", "${cxx.test.path1}");
    String value = settings.getString("key2");
    assertThat(value).isEqualTo("C:/test/reports/a.xml");
  }
}
