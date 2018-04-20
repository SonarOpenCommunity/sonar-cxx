/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.cxx.sensors.valgrind;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class ValgrindErrorTest {

  ValgrindError error;
  ValgrindError equalError;
  ValgrindError otherError;

  @Before
  public void setUp() {
    error = new ValgrindError("kind", "text", Collections.singletonList(new ValgrindStack()));
    equalError = new ValgrindError("kind", "text", Collections.singletonList(new ValgrindStack()));
    otherError = new ValgrindError("otherkind", "othertext", Collections.singletonList(new ValgrindStack()));
  }

  @Test
  public void errorDoesntEqualsNull() {
    assertThat(error).isNotNull();
  }

  @Test
  public void errorDoesntEqualsMiscObject() {
    assertThat(error).isNotEqualTo("string");
  }

  @Test
  public void errorEqualityIsReflexive() {
    assertThat(error).isEqualTo(error);
    assertThat(otherError).isEqualTo(otherError);
    assertThat(equalError).isEqualTo(equalError);
  }

  @Test
  public void errorEqualityWorksAsExpected() {
    assertThat(error).isEqualTo(equalError);
    assertThat(error).isNotEqualTo(otherError);
  }

  @Test
  public void errorHashWorksAsExpected() {
    assertThat(error.hashCode() == equalError.hashCode()).isTrue();
    assertThat(error.hashCode() != otherError.hashCode()).isTrue();
  }

  @Test
  public void getKindWorks() {
    String KIND = "kind";
    assertEquals(new ValgrindError(KIND, "", Collections.singletonList(new ValgrindStack())).getKind(), KIND);
  }
}
