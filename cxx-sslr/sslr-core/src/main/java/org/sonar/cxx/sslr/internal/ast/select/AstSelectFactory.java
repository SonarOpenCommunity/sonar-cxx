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
package org.sonar.cxx.sslr.internal.ast.select; // cxx: in use

import org.sonar.cxx.sslr.ast.AstSelect;

import com.sonar.cxx.sslr.api.AstNode;

import javax.annotation.Nullable;

import java.util.List;

public final class AstSelectFactory {

  private static final AstSelect EMPTY = new EmptyAstSelect();

  private AstSelectFactory() {
  }

  public static AstSelect select(@Nullable AstNode node) {
    return node == null ? EMPTY : new SingleAstSelect(node);
  }

  public static AstSelect create(List<AstNode> list) {
    if (list.size() == 1) {
      return new SingleAstSelect(list.get(0));
    } else if (!list.isEmpty()) {
      return new ListAstSelect(list);
    } else {
      return EMPTY;
    }
  }

  public static AstSelect empty() {
    return EMPTY;
  }

}
