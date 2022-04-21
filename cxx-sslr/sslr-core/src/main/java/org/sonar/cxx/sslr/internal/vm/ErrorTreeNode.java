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
package org.sonar.cxx.sslr.internal.vm;

import java.util.ArrayList;
import java.util.List;
import org.sonar.cxx.sslr.internal.matchers.MatcherPathElement;

public class ErrorTreeNode {

  public MatcherPathElement pathElement;
  public List<ErrorTreeNode> children = new ArrayList<>();

  public static ErrorTreeNode buildTree(List<List<MatcherPathElement>> paths) {
    var root = new ErrorTreeNode();
    root.pathElement = paths.get(0).get(0);
    for (var path : paths) {
      addToTree(root, path);
    }
    return root;
  }

  private static void addToTree(ErrorTreeNode root, List<MatcherPathElement> path) {
    var current = root;
    int i = 1;
    var found = true;
    while (found && i < path.size()) {
      found = false;
      for (var child : current.children) {
        if (child.pathElement.equals(path.get(i))) {
          current = child;
          i++;
          found = true;
          break;
        }
      }
    }
    while (i < path.size()) {
      var child = new ErrorTreeNode();
      child.pathElement = path.get(i);
      current.children.add(child);
      current = child;
      i++;
    }
  }

}
