/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022 SonarOpenCommunity
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
 * fork of SonarSource Language Recognizer: https://github.com/SonarSource/sslr
 * Copyright (C) 2010-2021 SonarSource SA / mailto:info AT sonarsource DOT com / license: LGPL v3
 */
package org.sonar.cxx.sslr.internal.ast;

import com.sonar.cxx.sslr.api.AstNodeType;
import java.util.Collections;
import java.util.function.Predicate;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.sonar.cxx.sslr.ast.AstSelect;
import org.sonar.cxx.sslr.internal.ast.select.EmptyAstSelect;

public class EmptyAstSelectTest {

  private final AstSelect select = new EmptyAstSelect();

  @Test
  public void test_children() {
    assertThat((Object) select.children()).isSameAs(select);
    assertThat((Object) select.children(mock(AstNodeType.class))).isSameAs(select);
    assertThat((Object) select.children(mock(AstNodeType.class), mock(AstNodeType.class))).isSameAs(select);
  }

  @Test
  public void test_nextSibling() {
    assertThat((Object) select.nextSibling()).isSameAs(select);
  }

  @Test
  public void test_previousSibling() {
    assertThat((Object) select.previousSibling()).isSameAs(select);
  }

  @Test
  public void test_parent() {
    assertThat((Object) select.parent()).isSameAs(select);
  }

  @Test
  public void test_firstAncestor() {
    assertThat((Object) select.firstAncestor(mock(AstNodeType.class))).isSameAs(select);
    assertThat((Object) select.firstAncestor(mock(AstNodeType.class), mock(AstNodeType.class))).isSameAs(select);
  }

  @Test
  public void test_descendants() {
    assertThat((Object) select.descendants(mock(AstNodeType.class))).isSameAs(select);
    assertThat((Object) select.descendants(mock(AstNodeType.class), mock(AstNodeType.class))).isSameAs(select);
  }

  @Test
  public void test_isEmpty() {
    assertThat(select.isEmpty()).isTrue();
  }

  @Test
  public void test_isNotEmpty() {
    assertThat(select.isNotEmpty()).isFalse();
  }

  @Test
  public void test_filter() {
    assertThat((Object) select.filter(mock(AstNodeType.class))).isSameAs(select);
    assertThat((Object) select.filter(mock(AstNodeType.class), mock(AstNodeType.class))).isSameAs(select);
    assertThat((Object) select.filter(mock(Predicate.class))).isSameAs(select);
  }

  @Test
  public void test_get_non_existing() {
    var thrown = catchThrowableOfType(
      () -> select.get(0),
      IndexOutOfBoundsException.class);
    assertThat(thrown).isExactlyInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  public void test_size() {
    assertThat(select.size()).isEqualTo(0);
  }

  @Test
  public void test_iterator() {
    assertThat((Object) select.iterator()).isSameAs(Collections.emptyIterator());
  }

}
