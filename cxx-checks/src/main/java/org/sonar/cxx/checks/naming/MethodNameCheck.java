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
package org.sonar.cxx.checks.naming;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.tag.Tag;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.checks.SquidCheck;

/**
 * MethodNameCheck
 *
 */
@Rule(
  key = "MethodName",
  priority = Priority.MAJOR,
  name = "Method names should comply with a naming convention",
  tags = {Tag.CONVENTION})
@SqaleConstantRemediation("10min")
@ActivatedByDefault
public class MethodNameCheck extends SquidCheck<Grammar> {

  private static final String DEFAULT = "^[A-Z][A-Za-z0-9]{2,30}$";
  private Pattern pattern;

  /**
   * format
   */
  @RuleProperty(
    key = "format",
    defaultValue = "" + DEFAULT)
  public String format = DEFAULT;

  @Override
  public void init() {
    pattern = Pattern.compile(format);
    subscribeTo(CxxGrammarImpl.functionDefinition);
  }

  @Override
  public void visitNode(AstNode astNode) {
    AstNode idNode = getMethodName(astNode);
    if (idNode != null) {
      String identifier = idNode.getTokenValue();
      if (!pattern.matcher(identifier).matches()) {
        getContext().createLineViolation(this,
          "Rename method \"{0}\" to match the regular expression {1}.", idNode, identifier, format);
      }
    }
  }

  private static @Nullable
  AstNode getMethodName(AstNode functionDefinition) {
    AstNode declId = functionDefinition.getFirstDescendant(CxxGrammarImpl.declaratorId);
    AstNode result = null;
    if (declId != null) {
      // method inside of class
      result = getInsideMemberDeclaration(declId);
      if (result == null) {
        // a nested name - method outside of class
        result = getOutsideMemberDeclaration(declId);
      }
    }
    return result;
  }

  private static @Nullable
  AstNode getInsideMemberDeclaration(AstNode declId) {
    AstNode result = null;
    if (declId.hasAncestor(CxxGrammarImpl.memberDeclaration)) {
      AstNode idNode = declId.getLastChild(CxxGrammarImpl.className);
      if (idNode != null) {
        AstNode classSpecifier = declId.getFirstAncestor(CxxGrammarImpl.classSpecifier);
        if (classSpecifier != null) {
          AstNode classHeadName = classSpecifier.getFirstDescendant(CxxGrammarImpl.classHeadName);
          if (classHeadName != null) {
            AstNode className = classHeadName.getLastChild(CxxGrammarImpl.className);
            // if class name is equal to method name then it is a ctor or dtor
            if ((className != null) && !className.getTokenValue().equals(idNode.getTokenValue())) {
              result = idNode;
            }
          }
        }
      }
    }
    return result;
  }

  private static @Nullable
  AstNode getOutsideMemberDeclaration(AstNode declId) {
    AstNode nestedNameSpecifier = declId.getFirstDescendant(CxxGrammarImpl.nestedNameSpecifier);
    AstNode result = null;
    if (nestedNameSpecifier != null) {
      AstNode idNode = declId.getLastChild(CxxGrammarImpl.className);
      if (idNode != null) {
        AstNode className = nestedNameSpecifier.getFirstDescendant(CxxGrammarImpl.className);
        // if class name is equal to method name then it is a ctor or dtor
        if ((className != null) && !className.getTokenValue().equals(idNode.getTokenValue())) {
          result = idNode;
        }
      }
    }
    return result;
  }

}
