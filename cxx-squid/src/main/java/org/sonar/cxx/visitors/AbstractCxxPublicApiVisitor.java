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
     * Dump the AST of the file is true.
     */
    private static final boolean DUMP = false;

    public interface PublicApiHandler {
        void onPublicApi(AstNode node, String id, List<Token> comments);
    }

    private List<String> headerFileSuffixes;

    private boolean skipFile = true;

    @Override
    public void init() {
        subscribeTo(CxxGrammarImpl.classSpecifier);
        subscribeTo(CxxGrammarImpl.memberDeclaration);
        subscribeTo(CxxGrammarImpl.functionDefinition);
        subscribeTo(CxxGrammarImpl.enumSpecifier);
        subscribeTo(CxxGrammarImpl.initDeclaratorList);
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
        if (declaratorList.getFirstAncestor(CxxGrammarImpl.functionBody) != null)
            return;

        AstNode declaration = declaratorList
                .getFirstAncestor(CxxGrammarImpl.declaration);

        LOG.trace("declaration: " + declaration);

        List<AstNode> declarators = declaratorList
                .getChildren(CxxGrammarImpl.initDeclarator);

        if (declarators.size() == 1) {
            visitDeclarator(declaration);
        } else {
            for (AstNode declarator : declarators) {
                visitDeclarator(declarator);
            }
        }
    }

    private void visitDeclarator(AstNode declarator) {
        // look for block documentation
        List<Token> comments = getBlockDocumentation(declarator);

        // documentation may be inlined
        if (comments.isEmpty()) {
            comments = getDeclaratorInlineComment(declarator);
        }

        AstNode declaratorId = declarator
                .getFirstDescendant(CxxGrammarImpl.declaratorId);

        if (declaratorId == null) {
            LOG.error("null declaratorId: " + AstXmlPrinter.print(declarator));
        } else
            visitPublicApi(declaratorId, declaratorId.getTokenValue(), comments);
    }

    private void visitClassSpecifier(AstNode classSpecifier) {

        if (!isPublicApiMember(classSpecifier)) {
            logDebug(classSpecifier
                    .getFirstDescendant(CxxGrammarImpl.className)
                    .getTokenValue()
                    + " not in public API");
            return;
        }

        AstNode template = classSpecifier
                .getFirstAncestor(CxxGrammarImpl.templateDeclaration);
        AstNode docNode = (template != null) ? template : classSpecifier;

        AstNode id = classSpecifier
                .getFirstDescendant(CxxGrammarImpl.className);

        visitPublicApi(id, id.getTokenValue(), getBlockDocumentation(docNode));
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
            } else {
            // if several declarators, doc should be placed before each
            // declarator, or inlined
                for (AstNode declarator : declarators) {
                    visitMemberDeclarator(declarator);
                }
            }
        }
    }

    private void visitFunctionDefinition(AstNode functionDef) {
        visitMemberDeclarator(functionDef);
    }

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

    private void visitEnumSpecifier(AstNode enumSpecifierNode) {
        if (!isPublicApiMember(enumSpecifierNode)) {
            logDebug(enumSpecifierNode.getFirstDescendant(
                    GenericTokenType.IDENTIFIER).getTokenValue()
                    + " not in public API");
            return;
        }

        visitPublicApi(
                enumSpecifierNode,
                enumSpecifierNode.getFirstDescendant(
                        GenericTokenType.IDENTIFIER).getTokenValue(),
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
                                + enclosingSpecifierNode.getType());
                        return false;
                    }
                } else {
                    LOG.error("isPublicApiMember: not a member");
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
