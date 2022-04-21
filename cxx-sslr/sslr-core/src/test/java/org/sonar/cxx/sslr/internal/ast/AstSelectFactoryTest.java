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

import com.sonar.cxx.sslr.api.AstNode;
import java.util.Arrays;
import java.util.Collections;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.sonar.cxx.sslr.internal.ast.select.AstSelectFactory;
import org.sonar.cxx.sslr.internal.ast.select.EmptyAstSelect;
import org.sonar.cxx.sslr.internal.ast.select.ListAstSelect;
import org.sonar.cxx.sslr.internal.ast.select.SingleAstSelect;

public class AstSelectFactoryTest {

  @Test
  public void test_select() {
    assertThat((Object) AstSelectFactory.select(null)).isInstanceOf(EmptyAstSelect.class);
    assertThat((Object) AstSelectFactory.select(mock(AstNode.class))).isInstanceOf(SingleAstSelect.class);
  }

  @Test
  public void test_create() {
    var node1 = mock(AstNode.class);
    var node2 = mock(AstNode.class);
    assertThat((Object) AstSelectFactory.create(Collections.emptyList())).isSameAs(AstSelectFactory.empty());
    assertThat((Object) AstSelectFactory.create(Arrays.asList(node1))).isInstanceOf(SingleAstSelect.class);
    assertThat((Object) AstSelectFactory.create(Arrays.asList(node1, node2))).isInstanceOf(ListAstSelect.class);
  }

  @Test
  public void test_empty() {
    assertThat((Object) AstSelectFactory.empty()).isInstanceOf(EmptyAstSelect.class);
    assertThat((Object) AstSelectFactory.empty()).isSameAs(AstSelectFactory.empty());
  }

}
