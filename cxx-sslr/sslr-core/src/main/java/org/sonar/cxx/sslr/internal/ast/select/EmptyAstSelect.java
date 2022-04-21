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
package org.sonar.cxx.sslr.internal.ast.select;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstNodeType;
import org.sonar.cxx.sslr.ast.AstSelect;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * {@link AstSelect} which contains no elements.
 */
public class EmptyAstSelect implements AstSelect {

  @Override
  public AstSelect children() {
    return this;
  }

  @Override
  public AstSelect children(AstNodeType type) {
    return this;
  }

  @Override
  public AstSelect children(AstNodeType... types) {
    return this;
  }

  @Override
  public AstSelect nextSibling() {
    return this;
  }

  @Override
  public AstSelect previousSibling() {
    return this;
  }

  @Override
  public AstSelect parent() {
    return this;
  }

  @Override
  public AstSelect firstAncestor(AstNodeType type) {
    return this;
  }

  @Override
  public AstSelect firstAncestor(AstNodeType... types) {
    return this;
  }

  @Override
  public AstSelect descendants(AstNodeType type) {
    return this;
  }

  @Override
  public AstSelect descendants(AstNodeType... types) {
    return this;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public boolean isNotEmpty() {
    return false;
  }

  @Override
  public AstSelect filter(AstNodeType type) {
    return this;
  }

  @Override
  public AstSelect filter(AstNodeType... types) {
    return this;
  }

  @Override
  public AstSelect filter(Predicate<AstNode> predicate) {
    return this;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public AstNode get(int index) {
    throw new IndexOutOfBoundsException();
  }

  @Override
  public Iterator<AstNode> iterator() {
    return Collections.emptyIterator();
  }

}
