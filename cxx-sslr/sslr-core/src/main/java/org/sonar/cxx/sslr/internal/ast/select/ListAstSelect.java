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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import org.sonar.cxx.sslr.ast.AstSelect;

/**
 * {@link AstSelect} which contains more than one element.
 */
public class ListAstSelect implements AstSelect {

  private final List<AstNode> list;

  public ListAstSelect(List<AstNode> list) {
    this.list = list;
  }

  @Override
  public AstSelect children() {
    List<AstNode> result = new ArrayList<>();
    for (var node : list) {
      result.addAll(node.getChildren());
    }
    return AstSelectFactory.create(result);
  }

  @Override
  public AstSelect children(AstNodeType type) {
    List<AstNode> result = new ArrayList<>();
    for (var node : list) {
      // Don't use "getChildren(type)", because under the hood it will create an array of types and new List to keep the result
      for (var child : node.getChildren()) {
        // Don't use "is(type)", because under the hood it will create an array of types
        if (child.getType() == type) {
          result.add(child);
        }
      }
    }
    return AstSelectFactory.create(result);
  }

  @Override
  public AstSelect children(AstNodeType... types) {
    List<AstNode> result = new ArrayList<>();
    for (var node : list) {
      // Don't use "getChildren(type)", because it will create new List to keep the result
      for (var child : node.getChildren()) {
        if (child.is(types)) {
          result.add(child);
        }
      }
    }
    return AstSelectFactory.create(result);
  }

  @Override
  public AstSelect nextSibling() {
    List<AstNode> result = new ArrayList<>();
    for (var node : list) {
      node = node.getNextSibling();
      if (node != null) {
        result.add(node);
      }
    }
    return AstSelectFactory.create(result);
  }

  @Override
  public AstSelect previousSibling() {
    List<AstNode> result = new ArrayList<>();
    for (var node : list) {
      node = node.getPreviousSibling();
      if (node != null) {
        result.add(node);
      }
    }
    return AstSelectFactory.create(result);
  }

  @Override
  public AstSelect parent() {
    List<AstNode> result = new ArrayList<>();
    for (var node : list) {
      node = node.getParent();
      if (node != null) {
        result.add(node);
      }
    }
    return AstSelectFactory.create(result);
  }

  @Override
  public AstSelect firstAncestor(AstNodeType type) {
    List<AstNode> result = new ArrayList<>();
    for (var node : list) {
      node = node.getParent();
      while (node != null && node.getType() != type) {
        node = node.getParent();
      }
      if (node != null) {
        result.add(node);
      }
    }
    return AstSelectFactory.create(result);
  }

  @Override
  public AstSelect firstAncestor(AstNodeType... types) {
    List<AstNode> result = new ArrayList<>();
    for (var node : list) {
      node = node.getParent();
      while (node != null && !node.is(types)) {
        node = node.getParent();
      }
      if (node != null) {
        result.add(node);
      }
    }
    return AstSelectFactory.create(result);
  }

  @Override
  public AstSelect descendants(AstNodeType type) {
    List<AstNode> result = new ArrayList<>();
    for (var node : list) {
      result.addAll(node.getDescendants(type));
    }
    return AstSelectFactory.create(result);
  }

  @Override
  public AstSelect descendants(AstNodeType... types) {
    List<AstNode> result = new ArrayList<>();
    for (var node : list) {
      result.addAll(node.getDescendants(types));
    }
    return AstSelectFactory.create(result);
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean isNotEmpty() {
    return true;
  }

  @Override
  public AstSelect filter(AstNodeType type) {
    List<AstNode> result = new ArrayList<>();
    for (var node : list) {
      if (node.getType() == type) {
        result.add(node);
      }
    }
    return AstSelectFactory.create(result);
  }

  @Override
  public AstSelect filter(AstNodeType... types) {
    List<AstNode> result = new ArrayList<>();
    for (var node : list) {
      if (node.is(types)) {
        result.add(node);
      }
    }
    return AstSelectFactory.create(result);
  }

  @Override
  public AstSelect filter(Predicate<AstNode> predicate) {
    List<AstNode> result = new ArrayList<>();
    for (var node : list) {
      if (predicate.test(node)) {
        result.add(node);
      }
    }
    return AstSelectFactory.create(result);
  }

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public AstNode get(int index) {
    return list.get(index);
  }

  @Override
  public Iterator<AstNode> iterator() {
    return list.iterator();
  }

}
