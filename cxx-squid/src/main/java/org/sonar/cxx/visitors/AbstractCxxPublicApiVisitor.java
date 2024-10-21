/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
package org.sonar.cxx.visitors;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.ast.AstXmlPrinter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.parser.CxxKeyword;
import org.sonar.cxx.parser.CxxPunctuator;
import org.sonar.cxx.squidbridge.checks.SquidCheck;

/**
 * Abstract visitor that visits public API items.<br>
 * Following items are considered as public API:
 * <ul>
 * <li>classes/structures</li>
 * <li>class members (public and protected)</li>
 * <li>structure members</li>
 * <li>union members</li>
 * <li>enumerations</li>
 * <li>enumeration values</li>
 * <li>typedefs</li>
 * <li>alias declaration (<code>using MyAlias = int;</code>)</li>
 * <li>functions</li>
 * <li>variables</li>
 * </ul>
 * <p>
 * Public API items are considered documented if they have Doxygen comments.<br>
 * Function arguments are not counted since they can be documented in function documentation and this visitor does not
 * parse Doxygen comments.<br>
 * This visitor should be applied only on header files.<br>
 * Currently, no filtering is applied using preprocessing directive, e.g <code>#define DLLEXPORT</code>.<br>
 * <p>
 * Limitation: only "in front of the declaration" comments are considered.
 *
 * @see <a href="http://www.stack.nl/~dimitri/doxygen/manual/docblocks.html">
 * Doxygen Manual: Documenting the code</a>
 *
 * @author Ludovic Cintrat
 *
 * @param <G>
 */
public abstract class AbstractCxxPublicApiVisitor<G extends Grammar> extends SquidCheck<G> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractCxxPublicApiVisitor.class);

  private static final String UNNAMED_CLASSIFIER_ID = "<unnamed class>";

  private static final String TOKEN_OVERRIDE = "override";
  private String[] headerFileSuffixes;
  protected boolean skipFile = true;

  private static boolean isTypedef(AstNode declaratorList) {
    var simpleDeclSpezifierSeq = declaratorList.getPreviousSibling();
    if (simpleDeclSpezifierSeq != null) {
      var firstDeclSpecifier = simpleDeclSpezifierSeq.getFirstChild(CxxGrammarImpl.declSpecifier);
      if (firstDeclSpecifier != null && firstDeclSpecifier.getToken().getType().equals(CxxKeyword.TYPEDEF)) {
        var classSpefifier = firstDeclSpecifier.getNextSibling();
        if (classSpefifier != null) {
          var type = classSpefifier.getToken().getType();
          if (type.equals(CxxKeyword.STRUCT)
            || type.equals(CxxKeyword.CLASS)
            || type.equals(CxxKeyword.UNION)
            || type.equals(CxxKeyword.ENUM)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static boolean isFriendDeclarationList(AstNode declaratorList) {
    AstNode simpleDeclNode = declaratorList
      .getFirstAncestor(CxxGrammarImpl.simpleDeclaration);

    if (simpleDeclNode == null) {
      // no simple declaration found for declarator list
      return false;
    }

    AstNode declSpecifierSeq = simpleDeclNode
      .getFirstChild(CxxGrammarImpl.declSpecifierSeq);

    if (declSpecifierSeq == null) {
      return false;
    }

    List<AstNode> declSpecifiers = declSpecifierSeq
      .getChildren(CxxGrammarImpl.declSpecifier);

    for (var declSpecifier : declSpecifiers) {
      var friendNode = declSpecifier.getFirstChild(CxxKeyword.FRIEND);
      if (friendNode != null) {
        return true;
      }
    }

    return false;
  }

  @CheckForNull
  private static AstNode getTypedefNode(AstNode classSpecifier) {
    var declSpecifier = classSpecifier.getFirstAncestor(CxxGrammarImpl.declSpecifier);
    if (declSpecifier != null) {
      declSpecifier = declSpecifier.getPreviousSibling();
      if (declSpecifier != null) {
        var typedef = declSpecifier.getFirstChild();
        if (typedef != null && typedef.getToken().getType().equals(CxxKeyword.TYPEDEF)) {
          return typedef;
        }
      }
    }
    return null;
  }

  private static boolean isFriendMemberDeclaration(AstNode memberDeclaration) {
    AstNode simpleDeclNode = memberDeclaration
      .getFirstDescendant(CxxGrammarImpl.simpleDeclaration);

    if (simpleDeclNode == null) {
      // no simple declaration found for declarator list
      return false;
    }

    AstNode declSpecifierSeq = simpleDeclNode
      .getFirstChild(CxxGrammarImpl.declSpecifierSeq);

    if (declSpecifierSeq != null) {
      List<AstNode> declSpecifiers = declSpecifierSeq
        .getChildren(CxxGrammarImpl.declSpecifier);

      for (var declSpecifier : declSpecifiers) {
        var friendNode = declSpecifier.getFirstChild(CxxKeyword.FRIEND);
        if (friendNode != null) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean isDefaultOrDeleteFunctionBody(AstNode functionBodyNode) {
    var defaultOrDelete = false;
    List<AstNode> functionBody = functionBodyNode.getChildren();

    // look for exact sub AST
    if ((functionBody.size() == 3)
      && functionBody.get(0).is(CxxPunctuator.ASSIGN)
      && functionBody.get(2).is(CxxPunctuator.SEMICOLON)) {

      AstNode bodyType = functionBody.get(1);

      if (bodyType.is(CxxKeyword.DELETE)
        || bodyType.is(CxxKeyword.DEFAULT)) {
        defaultOrDelete = true;
      }
    }
    return defaultOrDelete;
  }

  private static boolean isOverriddenMethod(AstNode memberDeclarator) {
    List<AstNode> modifiers = memberDeclarator.getDescendants(CxxGrammarImpl.virtSpecifier);

    for (var modifier : modifiers) {
      var modifierId = modifier.getFirstChild();

      if (TOKEN_OVERRIDE.equals(modifierId.getTokenValue())) {
        return true;
      }
    }

    return false;
  }

  // XXX may go to a utility class
  private static String getOperatorId(AstNode operatorFunctionId) {

    var builder = new StringBuilder(operatorFunctionId.getTokenValue());
    var operator = operatorFunctionId.getFirstDescendant(CxxGrammarImpl.operator);

    if (operator != null) {

      var opNode = operator.getFirstChild();
      while (opNode != null) {
        builder.append(opNode.getTokenValue());
        opNode = opNode.getNextSibling();
      }
    }

    return builder.toString();
  }

  private static List<Token> getDeclaratorInlineComment(AstNode declarator) {
    List<Token> comments;

    // inline comments are attached to the next AST node (not sibling, because the last attribute inline comment
    // is attached to the next node of the parent)
    var next = declarator.getNextAstNode();

    // inline documentation may be on the next definition token or next curly brace
    if (next != null) {
      // discard COMMA and SEMICOLON
      if (next.getToken().getType().equals(CxxPunctuator.COMMA)
        || next.getToken().getType().equals(CxxPunctuator.SEMICOLON)) {
        next = next.getNextAstNode();
      }

      comments = getInlineDocumentation(next.getToken());
    } else {
      // could happen on parse error ?
      comments = new ArrayList<>();
    }

    return comments;
  }

  private static boolean isPublicApiMember(AstNode node) {
    AstNode access = node;

    // retrieve the accessSpecifier
    do {
      access = access.getPreviousAstNode();
    } while (access != null
      && !access.getType().equals(CxxGrammarImpl.accessSpecifier));

    if (access != null) {
      return access.getToken().getType().equals(CxxKeyword.PUBLIC)
        || access.getToken().getType().equals(CxxKeyword.PROTECTED);
    } else {
      AstNode classSpecifier = node
        .getFirstAncestor(CxxGrammarImpl.classSpecifier);

      if (classSpecifier != null) {

        AstNode enclosingSpecifierNode = classSpecifier
          .getFirstDescendant(CxxKeyword.STRUCT, CxxKeyword.CLASS,
            CxxKeyword.ENUM, CxxKeyword.UNION);

        if (enclosingSpecifierNode != null) {
          var type = enclosingSpecifierNode.getToken().getType();
          if (type.equals(CxxKeyword.STRUCT) || type.equals(CxxKeyword.UNION)) {
            // struct and union members have public access, thus access level
            // is the access level of the enclosing classSpecifier
            return isPublicApiMember(classSpecifier);

          } else if (type.equals(CxxKeyword.CLASS)) {
            // default access in classes is private
            return false;

          } else {
            LOG.error("isPublicApiMember unhandled case: {} at {}", enclosingSpecifierNode.getType(),
              enclosingSpecifierNode.getTokenLine());
            return false;
          }
        } else {
          LOG.error("isPublicApiMember: failed to get enclosing classSpecifier for node at {}",
            node.getTokenLine());
          return false;
        }
      }

      if (node.is(CxxGrammarImpl.functionDefinition)) {
        // filter out function definitions with nested name specifier: should be documented inside of class
        var declarator = node.getFirstChild(CxxGrammarImpl.declarator);
        if ((declarator != null) && declarator.hasDescendant(CxxGrammarImpl.nestedNameSpecifier)) {
          return false;
        }
      }

      return true;
    }
  }

  /**
   * Check if inline Doxygen documentation is attached to the end of the line
   *
   * @param token the token to inspect
   * @return true if documentation is found for specified line, false otherwise
   */
  private static List<Token> getInlineDocumentation(Token token) {
    var comments = new ArrayList<Token>();

    for (var trivia : token.getTrivia()) {
      if (trivia.isComment()) {
        var triviaToken = trivia.getToken();
        if ((triviaToken != null) && (isDoxygenInlineComment(triviaToken.getValue()))) {
          comments.add(triviaToken);
        }
      }
    }
    return comments;
  }

  private static List<Token> getBlockDocumentation(AstNode node) {
    var commentTokens = new ArrayList<Token>();

    var token = node.getToken();
    for (var trivia : token.getTrivia()) {
      if (trivia.isComment()) {
        var triviaToken = trivia.getToken();
        if (triviaToken != null) {
          String comment = triviaToken.getValue();
          if (isDoxygenCommentBlock(comment)
            && !isDoxygenInlineComment(comment)) {
            commentTokens.add(triviaToken);
          }
        }
      }
    }

    return commentTokens;
  }

  private static boolean isDoxygenInlineComment(String comment) {

    return comment.startsWith("/*!<") || comment.startsWith("/**<")
      || comment.startsWith("//!<") || comment.startsWith("///<");
  }

  private static boolean isDoxygenCommentBlock(String comment) {

    return comment.startsWith("/**") || comment.startsWith("/*!")
      || comment.startsWith("///") || comment.startsWith("//!");
  }

  @Override
  public void init() {
    subscribeTo(
      CxxGrammarImpl.classSpecifier,
      CxxGrammarImpl.memberDeclaration,
      CxxGrammarImpl.functionDefinition,
      CxxGrammarImpl.enumSpecifier,
      CxxGrammarImpl.initDeclaratorList,
      CxxGrammarImpl.aliasDeclaration);
  }

  @Override
  public void visitFile(AstNode astNode) {
    skipFile = true;

    if (headerFileSuffixes != null) {
      for (var suffix : headerFileSuffixes) {
        if (getContext().getFile().getName().endsWith(suffix)) {
          skipFile = false;
          break;
        }
      }
    }

  }

  @Override
  public void visitNode(AstNode astNode) {
    if (skipFile) {
      return;
    }

    switch ((CxxGrammarImpl) astNode.getType()) {
      case classSpecifier:
        visitClassSpecifier(astNode);
        break;
      case memberDeclaration:
        visitMemberDeclaration(astNode);
        break;
      case functionDefinition:
        visitFunctionDefinition(astNode);
        break;
      case enumSpecifier:
        visitEnumSpecifier(astNode);
        break;
      case initDeclaratorList:
        visitDeclaratorList(astNode);
        break;
      case aliasDeclaration:
        visitAliasDeclaration(astNode);
        break;
      default:
        // should not happen
        LOG.error("visiting unknown node: {}", astNode.getType());
        break;
    }
  }

  private void visitPublicApi(AstNode node, String id, List<Token> comments) {
    var doxygenComments = new ArrayList<Token>();

    for (var token : comments) {
      String comment = token.getValue();
      if (isDoxygenInlineComment(comment) || isDoxygenCommentBlock(comment)) {
        doxygenComments.add(token);
      }
    }

    onPublicApi(node, id, doxygenComments);
  }

  private void visitDeclaratorList(AstNode declaratorList) {

    // do not handle declaration in function body
    AstNode functionBody = declaratorList
      .getFirstAncestor(CxxGrammarImpl.functionBody);

    if (functionBody != null) {
      return;
    }

    // do not handle member declarations here
    AstNode memberDeclaration = declaratorList
      .getFirstAncestor(CxxGrammarImpl.memberDeclaration);

    if (memberDeclaration != null) {
      return;
    }

    // ignore classSpefifier typedefs (classSpefifier at classSpefifier)
    if (isTypedef(declaratorList)) {
      return;
    }

    // ignore friend declarations
    if (isFriendDeclarationList(declaratorList)) {
      return;
    }

    AstNode declaration = declaratorList
      .getFirstAncestor(CxxGrammarImpl.declaration);

    List<AstNode> declarators = declaratorList
      .getChildren(CxxGrammarImpl.initDeclarator);

    if (declarators.size() == 1) {
      // a special handling is needed in case of single declarator
      // because documentation may be located on different places
      // depending on the declaration
      visitSingleDeclarator(declaration, declarators.get(0));
    } else {
      // with several declarators, documentation should be located
      // on each declarator
      for (var declarator : declarators) {
        visitDeclarator(declarator, declarator);
      }
    }
  }

  private void visitSingleDeclarator(AstNode declaration,
    AstNode declarator) {

    AstNode docNode;

    AstNode params = declaration
      .getFirstDescendant(CxxGrammarImpl.parametersAndQualifiers);

    // in case of function declaration,
    // the docNode is set on the declaration node
    if (params != null) {
      AstNode linkageSpecification = declaration
        .getFirstAncestor(CxxGrammarImpl.linkageSpecification);
      if (linkageSpecification != null) {
        if (linkageSpecification.hasDirectChildren(CxxPunctuator.CURLBR_LEFT)) {
          docNode = declaration; // <code>extern "C" { ... }</code>
        } else {
          docNode = linkageSpecification; // <code>extern "C" ...</code>
        }
      } else {
        docNode = declaration;
      }
    } else {
      AstNode classSpecifier = declaration
        .getFirstDescendant(CxxGrammarImpl.classSpecifier);

      // if a class is specified on the same declaration,
      // e.g. 'struct {} a;'
      // the documentation node is set on the declarator
      if (classSpecifier != null) {
        docNode = declarator;
      } else {
        docNode = declaration;
      }
    }

    visitDeclarator(declarator, docNode);
  }

  private void visitDeclarator(AstNode declarator, AstNode docNode) {

    // check if this is a template specification to adjust documentation node
    var templateDeclaration = declarator.getFirstAncestor(CxxGrammarImpl.templateDeclaration);
    if (templateDeclaration != null) {
      visitTemplateDeclaration(templateDeclaration);
    } else {
      // look for block documentation
      List<Token> comments = getBlockDocumentation(docNode);

      // documentation may be inlined
      if (comments.isEmpty()) {
        comments = getDeclaratorInlineComment(docNode);
      }

      AstNode declaratorId = declarator
        .getFirstDescendant(CxxGrammarImpl.declaratorId);

      if (declaratorId == null) {
        LOG.error("null declaratorId: {}", AstXmlPrinter.print(declarator));
      } else {
        visitPublicApi(declaratorId, declaratorId.getTokenValue(), comments);
      }
    }
  }

  private void visitClassSpecifier(AstNode classSpecifier) {

    // check if this is a template specification to adjust documentation node
    var docNode = classSpecifier.getFirstAncestor(CxxGrammarImpl.templateDeclaration);
    if (docNode == null) {
      // check if this is a typedef to adjust documentation node
      docNode = getTypedefNode(classSpecifier);
      if (docNode == null) {
        docNode = classSpecifier;
      }
    }

    // narrow the identifier search scope to classHead
    AstNode classHead = classSpecifier
      .getFirstDescendant(CxxGrammarImpl.classHead);

    if (classHead == null) {
      // classSpecifier does not embed a classHead
      return;
    }

    // look for the specifier id
    var id = classHead.getFirstDescendant(CxxGrammarImpl.className);

    AstNode idNode;
    String idName;

    // check if this is an unnamed specifier
    if (id == null) {
      idNode = classSpecifier;
      idName = UNNAMED_CLASSIFIER_ID;
    } else {
      idNode = id;
      idName = id.getTokenValue();
    }

    if (isPublicApiMember(classSpecifier)) {
      visitPublicApi(idNode, idName, getBlockDocumentation(docNode));
    }
  }

  private void visitMemberDeclaration(AstNode memberDeclaration) {

    // check if this is a template specification to adjust documentation node
    var templateDeclaration = memberDeclaration.getFirstDescendant(CxxGrammarImpl.templateDeclaration);
    AstNode declaratorList = null;
    if (templateDeclaration == null) {
      declaratorList = memberDeclaration.getFirstDescendant(CxxGrammarImpl.memberDeclaratorList);
    }

    if (!isPublicApiMember(memberDeclaration)) {
      // if not part of the API, nothing to measure
      return;
    }

    AstNode subclassSpecifier = memberDeclaration
      .getFirstDescendant(CxxGrammarImpl.classSpecifier);

    if (subclassSpecifier != null) {
      // sub classes are handled by subscription
      return;
    }

    AstNode functionDef = memberDeclaration
      .getFirstDescendant(CxxGrammarImpl.functionDefinition);

    if (functionDef != null) {
      // functionDef are handled by subscription
      return;
    }

    if (templateDeclaration != null) {
      visitTemplateDeclaration(templateDeclaration);
    } else if (declaratorList != null) {
      List<AstNode> declarators = declaratorList
        .getChildren(CxxGrammarImpl.memberDeclarator);

      // if only one declarator, the doc should be placed before the
      // memberDeclaration, or in-lined
      if (declarators.size() == 1) {
        visitMemberDeclarator(memberDeclaration);
      } else {
        // if several declarator, doc should be placed before each
        // declarator, or in-lined
        for (var declarator : declarators) {
          visitMemberDeclarator(declarator);
        }
      }
    }
  }

  private void visitTemplateDeclaration(AstNode templateDeclaration) {

    if (isFriendMemberDeclaration(templateDeclaration.getParent())) {
      return;
    }

    var declId = templateDeclaration.getFirstDescendant(CxxGrammarImpl.declaratorId);
    if (declId == null) {
      return;
    }

    String id = declId.getTokens().stream()
      .map(Token::getValue)
      .collect(Collectors.joining());

    // handle cascaded template declarations
    AstNode node = templateDeclaration;
    AstNode currNode = node;
    List<Token> comments;
    do {
      comments = getBlockDocumentation(node);
      if (!comments.isEmpty()) {
        currNode = node;
        break;
      }
      node = node.getFirstAncestor(CxxGrammarImpl.templateDeclaration);
    } while (node != null);

    visitPublicApi(currNode, id, comments);
  }

  private void visitFunctionDefinition(AstNode functionDef) {
    if (isPublicApiMember(functionDef)) {

      // filter out deleted and defaulted methods
      AstNode functionBodyNode = functionDef
        .getFirstChild(CxxGrammarImpl.functionBody);

      if ((functionBodyNode != null) && (isDefaultOrDeleteFunctionBody(functionBodyNode))) {
        return;
      }

      visitMemberDeclarator(functionDef);
    }
  }

  /**
   * Find documentation node, associated documentation, identifier of a
   * <em>public</em> member declarator and visit it as a public API.
   *
   * @param node the <em>public</em> member declarator to visit
   */
  private void visitMemberDeclarator(AstNode node) {

    if (isOverriddenMethod(node)) {
      // assume that ancestor method is documented
      // and do not count as public API
      return;
    }

    var container = node.getFirstAncestor(
      CxxGrammarImpl.templateDeclaration,
      CxxGrammarImpl.classSpecifier);

    AstNode docNode = node;
    List<Token> comments;

    if (container == null || container.getType().equals(CxxGrammarImpl.classSpecifier)) {
      comments = getBlockDocumentation(docNode);
    } else { // template
      do {
        comments = getBlockDocumentation(container);
        if (!comments.isEmpty()) {
          break;
        }
        container = container.getFirstAncestor(CxxGrammarImpl.templateDeclaration);
      } while (container != null);
    }

    // documentation may be inlined
    if (comments.isEmpty()) {
      comments = getDeclaratorInlineComment(node);
    }

    // find the identifier to present to concrete visitors
    String id = null;

    // first look for an operator function id
    var idNode = node.getFirstDescendant(CxxGrammarImpl.operatorFunctionId);

    if (idNode != null) {
      id = getOperatorId(idNode);
    } else {
      // look for a declarator id
      idNode = node.getFirstDescendant(CxxGrammarImpl.declaratorId);

      if (idNode != null) {
        id = idNode.getTokenValue();
      } else {
        // look for an identifier (e.g in bitfield declaration)
        idNode = node.getFirstDescendant(GenericTokenType.IDENTIFIER);

        if (idNode != null) {
          id = idNode.getTokenValue();
        } else {
          LOG.error("unsupported declarator at {}", node.getTokenLine());
        }
      }
    }

    if (idNode != null && id != null) {
      visitPublicApi(idNode, id, comments);
    }
  }

  private void visitAliasDeclaration(AstNode aliasDeclNode) {
    var parent = aliasDeclNode.getFirstAncestor(
      CxxGrammarImpl.functionDefinition,
      CxxGrammarImpl.classSpecifier);

    // An alias declaration inside a function is not part of the public API
    if (parent != null && parent.getType().equals(CxxGrammarImpl.functionDefinition)) {
      return;
    }

    if (isPublicApiMember(aliasDeclNode)) {
      AstNode aliasDeclIdNode = aliasDeclNode
        .getFirstDescendant(GenericTokenType.IDENTIFIER);

      if (aliasDeclIdNode == null) {
        LOG.error("no identifier found at {}", aliasDeclNode.getTokenLine());
      } else {
        // Check if this is a template specification to adjust
        // documentation node
        var container = aliasDeclNode.getFirstAncestor(
          CxxGrammarImpl.templateDeclaration,
          CxxGrammarImpl.classSpecifier);

        AstNode docNode;
        if (container == null
          || container.getType().equals(CxxGrammarImpl.classSpecifier)) {
          docNode = aliasDeclNode;
        } else {
          docNode = container;
        }

        // look for block documentation
        List<Token> comments = getBlockDocumentation(docNode);

        // documentation may be inlined
        if (comments.isEmpty()) {
          comments = getDeclaratorInlineComment(aliasDeclNode);
        }

        visitPublicApi(aliasDeclNode, aliasDeclIdNode.getTokenValue(),
          comments);
      }
    }
  }

  private static List<Token> getTypedefInlineComment(AstNode typedef) {
    var commentTokens = new ArrayList<Token>();
    var node = typedef.getFirstAncestor(CxxGrammarImpl.declaration);
    node = node.getNextAstNode();

    // search for first child with a comment
    while (node != null) {
      for (var trivia : node.getToken().getTrivia()) {
        if (trivia.isComment()) {
          commentTokens.add(trivia.getToken());
          return commentTokens;
        }
      }
      node = node.getFirstChild();
    }

    return commentTokens;
  }

  private void visitEnumSpecifier(AstNode enumSpecifierNode) {
    if (!isPublicApiMember(enumSpecifierNode)) {
      return; // not in public API
    }

    var docNode = getTypedefNode(enumSpecifierNode);
    AstNode idNode;
    if (docNode == null) {
      // enum ...
      idNode = enumSpecifierNode.getFirstDescendant(CxxGrammarImpl.enumHeadName);
      if (idNode != null) {
        visitPublicApi(enumSpecifierNode, idNode.getTokenValue(), getBlockDocumentation(enumSpecifierNode));
      }
    } else {
      // typedef enum ...
      var declaration = enumSpecifierNode.getFirstAncestor(CxxGrammarImpl.declaration);
      idNode = declaration.getFirstDescendant(CxxGrammarImpl.declaratorId);
      if (idNode != null) {
        List<Token> comments = getBlockDocumentation(docNode);
        if (comments.isEmpty()) { // documentation may be inlined
          comments = getTypedefInlineComment(docNode);
        }
        visitPublicApi(enumSpecifierNode, idNode.getTokenValue(), comments);
      }
    }

    // deal with enumeration values
    AstNode enumeratorList = enumSpecifierNode.getFirstDescendant(CxxGrammarImpl.enumeratorList);

    if (enumeratorList != null) {
      for (var definition : enumeratorList.getChildren(CxxGrammarImpl.enumeratorDefinition)) {

        // look for block documentation
        List<Token> comments = getBlockDocumentation(definition);

        // look for inlined doc
        if (comments.isEmpty()) {
          var next = definition.getNextAstNode();

          // inline documentation may be on the next definition token or next curly brace
          if (next != null) {
            // discard COMMA
            if (next.getToken().getType().equals(CxxPunctuator.COMMA)) {
              next = next.getNextAstNode();
            }

            comments = getInlineDocumentation(next.getToken());
          }
        }

        visitPublicApi(definition,
          definition.getFirstDescendant(GenericTokenType.IDENTIFIER).getTokenValue(),
          comments
        );
      }
    }
  }

  protected AbstractCxxPublicApiVisitor<G> withHeaderFileSuffixes(String[] headerFileSuffixes) {
    this.headerFileSuffixes = headerFileSuffixes.clone();
    return this;
  }

  protected abstract void onPublicApi(AstNode node, String id, List<Token> comments);

}
