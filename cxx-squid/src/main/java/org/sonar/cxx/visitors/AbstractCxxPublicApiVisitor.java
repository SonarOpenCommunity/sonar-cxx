/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.cxx.visitors;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.api.CxxPunctuator;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.squidbridge.checks.SquidCheck;

import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import com.sonar.sslr.impl.ast.AstXmlPrinter;

/**
 * Abstract visitor that visits public API items.<br>
 * Following items are considered as public API:
 * <ul>
 * <li>classes/structures</li>
 * <li>class members (public and protected)</li>
 * <li>structure members</li>
 * <li>enumerations</li>
 * <li>enumeration values</li>
 * <li>typedefs</li>
 * <li>alias declaration (<code>using MyAlias = int;</code>)</li>
 * <li>functions</li>
 * <li>variables</li>
 * </ul>
 * <p>
 * Public API items are considered documented if they have Doxygen comments.<br>
 * Function arguments are not counted since they can be documented in function
 * documentation and this visitor does not parse Doxygen comments.<br>
 * This visitor should be applied only on header files.<br>
 * Currently, no filtering is applied using preprocessing directive,
 * e.g <code>#define DLLEXPORT</code>.<br>
 * <p>
 * Limitation: only "in front of the declaration" comments are considered.
 *
 * @see <a href="http://www.stack.nl/~dimitri/doxygen/manual/docblocks.html">
 *      Doxygen Manual: Documenting the code</a>
 *
 * @author Ludovic Cintrat
 *
 * @param <GRAMMAR>
 */
// @Rule(key = "UndocumentedApi", description =
// "All public APIs should be documented", priority = Priority.MINOR)
public abstract class AbstractCxxPublicApiVisitor<GRAMMAR extends Grammar>
        extends SquidCheck<Grammar> implements AstAndTokenVisitor {

    abstract protected void onPublicApi(AstNode node, String id,
            List<Token> comments);

    private static final Logger LOG = LoggerFactory
            .getLogger("AbstractCxxPublicApiVisitor");

    private static final boolean DEBUG = false;
    /**
     * Dump the AST of the file if true.
     */
    private static final boolean DUMP = false;

    private static final String UNNAMED_CLASSIFIER_ID = "<unnamed class>";
    private static final String UNNAMED_ENUM_ID       = "<unnamed enumeration>";

    public interface PublicApiHandler {
        void onPublicApi(AstNode node, String id, List<Token> comments);
    };

    private List<String> headerFileSuffixes;

    private boolean skipFile = true;

    @Override
    public void init() {
        subscribeTo(CxxGrammarImpl.classSpecifier);
        subscribeTo(CxxGrammarImpl.memberDeclaration);
        subscribeTo(CxxGrammarImpl.functionDefinition);
        subscribeTo(CxxGrammarImpl.enumSpecifier);
        subscribeTo(CxxGrammarImpl.initDeclaratorList);
        subscribeTo(CxxGrammarImpl.aliasDeclaration);
    }

    @Override
    public void visitFile(AstNode astNode) {
        logDebug("API File: " + getContext().getFile().getName());

        logDebug("Header file suffixes: " + headerFileSuffixes);

        skipFile = true;

        if (headerFileSuffixes != null)
            for (String suffix : headerFileSuffixes) {
                if (getContext().getFile().getName().endsWith(suffix)) {
                    skipFile = false;
                    break;
                }
            }

        if (DUMP) {
            System.out.println(AstXmlPrinter.print(astNode));
        }

    }

    public void visitToken(Token token) {
    }

    @Override
    public void visitNode(AstNode astNode) {
        if (skipFile)
            return;

        logDebug("***** Node: " + astNode);

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
            LOG.error("Visiting unknown node: " + astNode.getType());
            break;
        }
    }

    private void visitPublicApi(AstNode node, String id, List<Token> comments) {
        List<Token> doxygenComments = new ArrayList<Token>();

        for (Token token : comments) {
            String comment = token.getValue();
            if (isDoxygenInlineComment(comment)
                    || isDoxygenCommentBlock(comment)) {
                doxygenComments.add(token);
                logDebug("Doc: " + comment.replace("\r\n", ""));
            }
        }

        logDebug("Public API: " + id);
        onPublicApi(node, id, doxygenComments);
    }

    private void visitDeclaratorList(AstNode declaratorList) {

        // do not handle declaration in function body
        AstNode functionBody = declaratorList
                .getFirstAncestor(CxxGrammarImpl.functionBody);

        if (functionBody != null) {
            return;
        }

        // ignore friend declarations
        if (isFriendDeclaration(declaratorList)) {
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
            for (AstNode declarator : declarators) {
                visitDeclarator(declarator, declarator);
            }
        }
    }

    private boolean isFriendDeclaration(AstNode declaratorList) {
        AstNode simpleDeclNode = declaratorList
                .getFirstAncestor(CxxGrammarImpl.simpleDeclaration);

        if (simpleDeclNode == null) {
            LOG.warn("No simple declaration found for declarator list at {}",
                    declaratorList.getTokenLine());
            return false;
        }

        AstNode simpleDeclSpecifierSeq = simpleDeclNode
                .getFirstChild(CxxGrammarImpl.simpleDeclSpecifierSeq);

        if (simpleDeclSpecifierSeq == null) {
            return false;
        }

        List<AstNode> declSpecifiers = simpleDeclSpecifierSeq
                .getChildren(CxxGrammarImpl.declSpecifier);

        for (AstNode declSpecifier : declSpecifiers) {
            AstNode friendNode = declSpecifier.getFirstChild(CxxKeyword.FRIEND);
            if (friendNode != null) {
                return true;
            }
        }

        return false;
    }

    private void visitSingleDeclarator(AstNode declaration,
            AstNode declarator) {

        AstNode docNode;

        AstNode params = declaration
                .getFirstDescendant(CxxGrammarImpl.parametersAndQualifiers);

        // in case of function declaration,
        // the docNode is set on the declaration node
        if (params != null) {
            docNode = declaration;
        }
        else {
            AstNode classSpecifier = declaration
                    .getFirstDescendant(CxxGrammarImpl.classSpecifier);

            // if a class is specified on the same declaration,
            // e.g. 'struct {} a;'
            // the documentation node is set on the declarator
            if (classSpecifier != null) {
                docNode = declarator;
            }
            else {
                docNode = declaration;
            }
        }

        visitDeclarator(declarator, docNode);
    }

    private void visitDeclarator(AstNode declarator, AstNode docNode) {
        // look for block documentation
        List<Token> comments = getBlockDocumentation(docNode);

        // documentation may be inlined
        if (comments.isEmpty()) {
            comments = getDeclaratorInlineComment(docNode);
        }

        AstNode declaratorId = declarator
                .getFirstDescendant(CxxGrammarImpl.declaratorId);

        if (declaratorId == null) {
            LOG.error("null declaratorId: " + AstXmlPrinter.print(declarator));
        } else {
            visitPublicApi(declaratorId, declaratorId.getTokenValue(), comments);
        }
    }

    private void visitClassSpecifier(AstNode classSpecifier) {

        // check if this is a template specification to adjust
        // documentation node
        AstNode template = classSpecifier
                .getFirstAncestor(CxxGrammarImpl.templateDeclaration);
        AstNode docNode = (template != null) ? template : classSpecifier;

        // narrow the identifier search scope to classHead
        AstNode classHead = classSpecifier
                .getFirstDescendant(CxxGrammarImpl.classHead);

        if (classHead == null) {
            LOG.warn("classSpecifier does not embed a classHead at line " +
                    classSpecifier.getTokenLine());
            return;
        }

        // look for the specifier id
        AstNode id = classHead.getFirstDescendant(CxxGrammarImpl.className);

        AstNode idNode;
        String idName;

        // check if this is an unnamed specifier
        if (id == null) {
            idNode = classSpecifier;
            idName = UNNAMED_CLASSIFIER_ID;
        }
        else {
            idNode = id;
            idName = id.getTokenValue();
        }

        if (!isPublicApiMember(classSpecifier)) {
            logDebug(idName + " not in public API");
        }
        else {
            visitPublicApi(idNode, idName, getBlockDocumentation(docNode));
        }
    }

    private void visitMemberDeclaration(AstNode memberDeclaration) {

        AstNode declaratorList = memberDeclaration
                .getFirstDescendant(CxxGrammarImpl.memberDeclaratorList);

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

        if (declaratorList != null) {
            List<AstNode> declarators = declaratorList
                    .getChildren(CxxGrammarImpl.memberDeclarator);

            // if only one declarator, the doc should be placed before the
            // memberDeclaration, or inlined
            if (declarators.size() == 1) {
                visitMemberDeclarator(memberDeclaration);
            }
            // if several declarators, doc should be placed before each
            // declarator, or inlined
            else {
                for (AstNode declarator : declarators) {
                    visitMemberDeclarator(declarator);
                }
            }
        }
    }

    private void visitFunctionDefinition(AstNode functionDef) {
        if (isPublicApiMember(functionDef)) {
            // filter out deleted and defaulted methods
            AstNode functionBodyNode = functionDef
                    .getFirstChild(CxxGrammarImpl.functionBody);

            if (functionBodyNode != null) {
                if (isDefaultOrDeleteFunctionBody(functionBodyNode)) {
                    return;
                }
            }

            visitMemberDeclarator(functionDef);
        }
    }

    private boolean isDefaultOrDeleteFunctionBody(AstNode functionBodyNode) {
        boolean defaultOrDelete = false;
        List<AstNode> functionBody = functionBodyNode.getChildren();

        // look for exact sub AST
        if (functionBody.size() == 3) {
            if (functionBody.get(0).is(CxxPunctuator.ASSIGN)
                    && functionBody.get(2).is(CxxPunctuator.SEMICOLON)) {

                AstNode bodyType = functionBody.get(1);

                if (bodyType.is(CxxKeyword.DELETE)
                        || bodyType.is(CxxKeyword.DEFAULT)) {
                    defaultOrDelete = true;
                }
            }
        }

        return defaultOrDelete;
    }
    
    /**
     * Find documentation node, associated documentation,
     * identifier of a <em>public</em> member declarator and visit it
     * as a public API. 
     * @param node the <em>public</em> member declarator to visit
     */
    private void visitMemberDeclarator(AstNode node) {

        AstNode container = node.getFirstAncestor(
                CxxGrammarImpl.templateDeclaration,
                CxxGrammarImpl.classSpecifier);

        AstNode docNode;

        if (container == null
                || container.getType() == CxxGrammarImpl.classSpecifier) {
            docNode = node;
        } else {
            docNode = container;
        }

        // look for block documentation
        List<Token> comments = getBlockDocumentation(docNode);

        // documentation may be inlined
        if (comments.isEmpty()) {
            comments = getDeclaratorInlineComment(node);
        }

        // find the identifier to present to concrete visitors
        String id = null;
        AstNode idNode = null;

        // first look for an operator function id
        idNode = node.getFirstDescendant(CxxGrammarImpl.operatorFunctionId);

        if (idNode != null) {
            id = getOperatorId(idNode);
        }
        else {
            // look for a declarator id
            idNode = node.getFirstDescendant(CxxGrammarImpl.declaratorId);

            if (idNode != null) {
                id = idNode.getTokenValue();
            }
            else {
                // look for an identifier (e.g in bitfield declaration)
                idNode = node.getFirstDescendant(GenericTokenType.IDENTIFIER);

                if (idNode != null) {
                    id = idNode.getTokenValue();
                }
                else {
                    LOG.error("Unsupported declarator at " + node.getTokenLine());
                }
            }
        }

        if (idNode != null && id != null) {
            visitPublicApi(idNode, id, comments);
        }
    }

    private void visitAliasDeclaration(AstNode aliasDeclNode) {
        if (isPublicApiMember(aliasDeclNode)) {
            logDebug("AliasDeclaration");

            AstNode aliasDeclIdNode = aliasDeclNode
                    .getFirstDescendant(GenericTokenType.IDENTIFIER);

            if (aliasDeclIdNode == null) {
                LOG.error("No identifier found at " + aliasDeclNode.getTokenLine());
            }
            else {
                // look for block documentation
                List<Token> comments = getBlockDocumentation(aliasDeclNode);

                // documentation may be inlined
                if (comments.isEmpty()) {
                    comments = getDeclaratorInlineComment(aliasDeclNode);
                }

                visitPublicApi(aliasDeclNode, aliasDeclIdNode.getTokenValue(),
                        comments);
            }
        }
    }

    private void visitEnumSpecifier(AstNode enumSpecifierNode) {
        AstNode enumIdNode = enumSpecifierNode.getFirstDescendant(
                GenericTokenType.IDENTIFIER);

        String enumId = (enumIdNode == null)?
                UNNAMED_ENUM_ID : enumIdNode.getTokenValue();

        if (!isPublicApiMember(enumSpecifierNode)) {
            logDebug(enumId + " not in public API");
            return;
        }

        visitPublicApi(
                enumSpecifierNode, enumId,
                getBlockDocumentation(enumSpecifierNode));

        // deal with enumeration values
        AstNode enumeratorList = enumSpecifierNode
                .getFirstDescendant(CxxGrammarImpl.enumeratorList);

        if (enumeratorList != null) {
            for (AstNode definition : enumeratorList
                    .getChildren(CxxGrammarImpl.enumeratorDefinition)) {

                // look for block documentation
                List<Token> comments = getBlockDocumentation(definition);

                // look for inlined doc
                if (comments.isEmpty()) {
                    AstNode next = definition.getNextAstNode();

                    // inline documentation may be on the next definition token
                    // or next curly brace
                    if (next != null) {
                        // discard COMMA
                        if (next.getToken().getType() == CxxPunctuator.COMMA) {
                            next = next.getNextAstNode();
                        }

                        comments = getInlineDocumentation(next.getToken(),
                                definition.getTokenLine());
                    }
                }

                visitPublicApi(
                        definition,
                        definition.getFirstDescendant(
                                GenericTokenType.IDENTIFIER).getTokenValue(),
                        comments);
            }
        }
    }

    // XXX may go to a utility class
    private String getOperatorId(AstNode operatorFunctionId) {

        StringBuilder builder = new StringBuilder(
                operatorFunctionId.getTokenValue());
        AstNode operator = operatorFunctionId
                .getFirstDescendant(CxxGrammarImpl.operator);

        if (operator != null) {

            AstNode opNode = operator.getFirstChild();
            while (opNode != null) {
                builder.append(opNode.getTokenValue());
                opNode = opNode.getNextSibling();
            }
        }

        return builder.toString();
    }

    private static List<Token> getDeclaratorInlineComment(AstNode declarator) {
        List<Token> comments;

        // inline comments are attached to the next AST node (not sibling,
        // because the last attribute inline comment is attached to the next
        // node of the parent)
        AstNode next = declarator.getNextAstNode();

        // inline documentation may be on the next definition token
        // or next curly brace
        if (next != null) {
            // discard COMMA and SEMICOLON
            if (next.getToken().getType() == CxxPunctuator.COMMA
                    || next.getToken().getType() == CxxPunctuator.SEMICOLON) {
                next = next.getNextAstNode();
            }

            comments = getInlineDocumentation(next.getToken(),
                    declarator.getTokenLine());
        } else {
            // could happen on parse error ?
            comments = new ArrayList<Token>();
        }

        return comments;
    }

    private static boolean isPublicApiMember(AstNode node) {
        AstNode access = node;

        // retrieve the accessSpecifier
        do {
            access = access.getPreviousAstNode();
        } while (access != null
                && access.getType() != CxxGrammarImpl.accessSpecifier);

        if (access != null) {
            return access.getToken().getType() == CxxKeyword.PUBLIC
                    || access.getToken().getType() == CxxKeyword.PROTECTED;
        } else {
            AstNode classSpecifier = node
                    .getFirstAncestor(CxxGrammarImpl.classSpecifier);

            if (classSpecifier != null) {

                AstNode enclosingSpecifierNode = classSpecifier
                        .getFirstDescendant(CxxKeyword.STRUCT,
                                CxxKeyword.CLASS, CxxKeyword.ENUM);

                if (enclosingSpecifierNode != null) {
                    switch ((CxxKeyword) enclosingSpecifierNode.getToken()
                            .getType()) {
                    case STRUCT:
                        // struct members have public access, thus access level
                        // is the access level of the enclosing classSpecifier
                        return isPublicApiMember(classSpecifier);
                    case CLASS:
                        // default access in classes is private
                        return false;
                    default:
                        LOG.error("isPublicApiMember unhandled case: "
                                + enclosingSpecifierNode.getType() 
                                + " at " + enclosingSpecifierNode.getTokenLine());
                        return false;
                    }
                } else {
                    LOG.error("isPublicApiMember: failed to get enclosing "
                            + "classSpecifier for node at " + node.getTokenLine());
                    return false;
                }
            }

            // global member
            return true;
        }
    }

    /**
     * Check if inline Doxygen documentation is attached to the given token at
     * specified line
     *
     * @param token
     *            the token to inspect
     * @param line
     *            line of the inlined documentation
     * @return true if documentation is found for specified line, false
     *         otherwise
     */
    private static List<Token> getInlineDocumentation(Token token, int line) {
        List<Token> comments = new ArrayList<Token>();

        for (Trivia trivia : token.getTrivia()) {
            if (trivia.isComment()) {
                Token triviaToken = trivia.getToken();
                if (triviaToken != null)
                    if (triviaToken.getLine() == line) {
                        if (isDoxygenInlineComment(triviaToken.getValue())) {
                            comments.add(triviaToken);
                            LOG.trace("Inline doc: " + triviaToken.getValue());
                        }
                    }
            }
        }
        return comments;
    }

    private static List<Token> getBlockDocumentation(AstNode node) {
        List<Token> commentTokens = new ArrayList<Token>();

        Token token = node.getToken();
        for (Trivia trivia : token.getTrivia()) {
            if (trivia.isComment()) {
                Token triviaToken = trivia.getToken();
                if (triviaToken != null) {
                    String comment = triviaToken.getValue();
                    LOG.trace("Doc: {}\n", comment);
                    if (isDoxygenCommentBlock(comment)
                            && !isDoxygenInlineComment(comment))
                        commentTokens.add(triviaToken);
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

    private void logDebug(String msg) {
        if (DEBUG)
            LOG.debug(msg);
    }

    public AbstractCxxPublicApiVisitor<GRAMMAR> withHeaderFileSuffixes(
            List<String> headerFileSuffixes) {
        this.headerFileSuffixes = headerFileSuffixes;
        return this;
    }
}
