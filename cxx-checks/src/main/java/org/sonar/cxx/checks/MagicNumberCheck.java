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
package org.sonar.cxx.checks;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import java.util.HashSet;
import java.util.Set;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.api.CxxTokenType;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.tag.Tag;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.checks.SquidCheck;

/**
 * Find Magic numbers in source code
 *
 */
@Rule(
  key = "MagicNumber",
  name = "Magic number should not be used",
  tags = {Tag.CONVENTION},
  priority = Priority.MINOR)
@ActivatedByDefault
@SqaleConstantRemediation("5min")
public class MagicNumberCheck extends SquidCheck<Grammar> {

  private static final String DEFAULT_EXCEPTIONS
    = "0,1,0x0,0x00,.0,.1,0.0,1.0,0u,1u,0ul,1ul,1.0f,0.0f,0LL,1LL,0ULL,1ULL";

  @RuleProperty(
    key = "exceptions",
    description = "Comma separated list of allowed values (excluding '-' and '+' signs)",
    defaultValue = DEFAULT_EXCEPTIONS)
  public String exceptions = DEFAULT_EXCEPTIONS;

  private final Set<String> exceptionsSet = new HashSet<>();

  @Override
  public void init() {
    subscribeTo(CxxTokenType.NUMBER);
    for (String magicNumber : exceptions.split(",")) {
      magicNumber = magicNumber.trim();
      if (!magicNumber.isEmpty()) {
        exceptionsSet.add(magicNumber);
      }
    }
  }

  @Override
  public void visitNode(AstNode node) {
    if (!isConstexpr(node)
      && !isConst(node)
      && !isExcluded(node)
      && !isInEnum(node)
      && !isArrayInitializer(node)
      && !isGenerated(node)
      && !isNullPtr(node)) {
      getContext().createLineViolation(this, "Extract this magic number '" + node.getTokenOriginalValue()
        + "' into a constant, variable declaration or an enum.", node);
    }
  }

  private static boolean isConstexpr(AstNode node) {
    AstNode decl = null;

    if (node.hasAncestor(CxxGrammarImpl.initDeclarator)) {
      decl = node.getFirstAncestor(CxxGrammarImpl.simpleDeclaration);
    } else if (node.hasAncestor(CxxGrammarImpl.memberDeclarator)) {
      decl = node.getFirstAncestor(CxxGrammarImpl.memberDeclaration);
    }

    if (null != decl) {
      return null != decl.getFirstDescendant(CxxKeyword.CONSTEXPR);
    } else {
      return false;
    }
  }

  private static boolean isConst(AstNode node) {
    AstNode decl = null;
    if (node.hasAncestor(CxxGrammarImpl.initDeclarator)) {
      decl = node.getFirstAncestor(CxxGrammarImpl.simpleDeclaration);
    } else if (node.hasAncestor(CxxGrammarImpl.memberDeclarator)) {
      decl = node.getFirstAncestor(CxxGrammarImpl.memberDeclaration);
    }
    if (decl != null) {
      for (AstNode qualifier : decl.getDescendants(CxxGrammarImpl.cvQualifier)) {
        if (qualifier.getToken().getType().equals(CxxKeyword.CONST)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isExcluded(AstNode node) {
    return exceptionsSet.contains(node.getTokenOriginalValue());
  }

  private static boolean isInEnum(AstNode node) {
    return node.hasAncestor(CxxGrammarImpl.enumeratorList);
  }

  private static boolean isArrayInitializer(AstNode node) {
    return node.hasAncestor(CxxGrammarImpl.bracedInitList);
  }

  private static boolean isGenerated(AstNode node) {
    return node.getToken().isGeneratedCode();
  }

  private static boolean isNullPtr(AstNode node) {
    return "nullptr".equals(node.getTokenValue());
  }

}
