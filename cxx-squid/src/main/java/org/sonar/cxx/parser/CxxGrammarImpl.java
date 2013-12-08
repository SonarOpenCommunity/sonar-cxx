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
package org.sonar.cxx.parser;

import com.sonar.sslr.impl.matcher.GrammarFunctions;
import org.sonar.cxx.api.CxxGrammar;
import org.sonar.cxx.api.CxxKeyword;

import static com.sonar.sslr.api.GenericTokenType.EOF;
import static com.sonar.sslr.api.GenericTokenType.IDENTIFIER;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Predicate.next;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Predicate.not;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.and;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.o2n;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.one2n;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.opt;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.or;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced.isFalse;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced.anyToken;
import static org.sonar.cxx.api.CxxTokenType.CHARACTER;
import static org.sonar.cxx.api.CxxTokenType.NUMBER;
import static org.sonar.cxx.api.CxxTokenType.STRING;

/**
 * Based on the C++ Standard, Appendix A
 */
public class CxxGrammarImpl extends CxxGrammar {
  private boolean error_recovery = true;
  
  public CxxGrammarImpl() {
    toplevel();
    expressions();
    statements();
    declarations();
    declarators();
    classes();
    derivedClasses();
    specialMemberFunctions();
    overloading();
    templates();
    exceptionHandling();

    misc();

    test.is("debugging asset");

    GrammarFunctions.enableMemoizationOfMatchesForAllRules(this);
  }

  private void misc() {
    // C++ Standard, Section 2.14.6 "Boolean literals"
    bool.is(
        or(
            CxxKeyword.TRUE,
            CxxKeyword.FALSE
        )
        );

    literal.is(
        or(
            CHARACTER,
            STRING,
            NUMBER,
            bool
        )
        );
  }

  private void toplevel() {
    translationUnit.is(o2n(declaration), EOF);
  }

  private void expressions() {
    primaryExpression.is(
        or(
            literal,
            CxxKeyword.THIS,
            and("(", expression, ")"),
            idExpression,
            lambdaExpression
        )
        ).skipIfOneChild();

    idExpression.is(
        or(
            qualifiedId,
            unqualifiedId
        )
        );

    unqualifiedId.is(
        or(
            templateId,
            operatorFunctionId,
            conversionFunctionId,
            literalOperatorId,
            and("~", className),
            and("~", decltypeSpecifier),
            IDENTIFIER
        )
        );

    qualifiedId.is(
        or(
            and(nestedNameSpecifier, opt(CxxKeyword.TEMPLATE), unqualifiedId),
            and("::", IDENTIFIER),
            and("::", operatorFunctionId),
            and("::", literalOperatorId),
            and("::", templateId)
        )
        );

    nestedNameSpecifier.is(
        or(
            and(opt("::"), typeName, "::"),
            and(opt("::"), namespaceName, "::"),
            and(decltypeSpecifier, "::")
        ),
        o2n(
        or(
            and(IDENTIFIER, "::"),
            and(opt(CxxKeyword.TEMPLATE), simpleTemplateId, "::")
        )
        )
        );

    lambdaExpression.is(lambdaIntroducer, opt(lambdaDeclarator), compoundStatement);

    lambdaIntroducer.is("[", opt(lambdaCapture), "]");

    lambdaCapture.is(
        or(
            and(captureDefault, ",", captureList),
            captureList,
            captureDefault
        ));

    captureDefault.is(
        or(
            "&",
            "="
        ));

    captureList.is(and(capture, opt("...")), o2n(",", and(capture, opt("..."))));

    capture.is(
        or(
            IDENTIFIER,
            and("&", IDENTIFIER),
            CxxKeyword.THIS
        ));

    lambdaDeclarator.is(
        "(", parameterDeclarationClause, ")", opt(CxxKeyword.MUTABLE),
        opt(exceptionSpecification), opt(attributeSpecifierSeq), opt(trailingReturnType)
        );

    postfixExpression.is(
        or(
            and(simpleTypeSpecifier, "(", opt(expressionList), ")"),
            and(simpleTypeSpecifier, bracedInitList),
            and(typenameSpecifier, "(", opt(expressionList), ")"),
            and(typenameSpecifier, bracedInitList),

            primaryExpression,

            and(CxxKeyword.DYNAMIC_CAST, "<", typeId, ">", "(", expression, ")"),
            and(CxxKeyword.STATIC_CAST, "<", typeId, ">", "(", expression, ")"),
            and(CxxKeyword.REINTERPRET_CAST, "<", typeId, ">", "(", expression, ")"),
            and(CxxKeyword.CONST_CAST, "<", typeId, ">", "(", expression, ")"),
            and(CxxKeyword.TYPEID, "(", expression, ")"),
            and(CxxKeyword.TYPEID, "(", typeId, ")")
        ),

        // postfixExpression [ expression ]
        // postfixExpression [ bracedInitList ]
        // postfixExpression ( expressionListopt )
        // postfixExpression . templateopt idExpression
        // postfixExpression -> templateopt idExpression
        // postfixExpression . pseudoDestructorName
        // postfixExpression -> pseudoDestructorName
        // postfixExpression ++
        // postfixExpression --

        // should replace the left recursive stuff above

        o2n(
        or(
            and("[", expression, "]"),
            and("(", opt(expressionList), ")"),
            and(or(".", "->"),
                or(and(opt(CxxKeyword.TEMPLATE), idExpression),
                    pseudoDestructorName)),
            "++",
            "--"
        )
        )
        ).skipIfOneChild();

    expressionList.is(initializerList);

    pseudoDestructorName.is(
        or(
            and(opt(nestedNameSpecifier), typeName, "::", "~", typeName),
            and(nestedNameSpecifier, CxxKeyword.TEMPLATE, simpleTemplateId, "::", "~", typeName),
            and(opt(nestedNameSpecifier), "~", typeName),
            and("~", decltypeSpecifier)
        )
        );

    unaryExpression.is(
        or(
            and(unaryOperator, castExpression),
            postfixExpression,
            and("++", castExpression),
            and("--", castExpression),
            and(CxxKeyword.SIZEOF, unaryExpression),
            and(CxxKeyword.SIZEOF, "(", typeId, ")"),
            and(CxxKeyword.SIZEOF, "...", "(", IDENTIFIER, ")"),
            and(CxxKeyword.ALIGNOF, "(", typeId, ")"),
            noexceptExpression,
            newExpression,
            deleteExpression
        )
        ).skipIfOneChild();

    unaryOperator.is(
        or("*", "&", "+", "-", "!", "~")
        );

    newExpression.is(
        or(
            and(opt("::"), CxxKeyword.NEW, opt(newPlacement), newTypeId, opt(newInitializer)),
            and(opt("::"), CxxKeyword.NEW, newPlacement, "(", typeId, ")", opt(newInitializer)),
            and(opt("::"), CxxKeyword.NEW, "(", typeId, ")", opt(newInitializer))
        )
        );

    newPlacement.is("(", expressionList, ")");

    newTypeId.is(typeSpecifierSeq, opt(newDeclarator));

    newDeclarator.is(
        or(
            noptrNewDeclarator,
            and(ptrOperator, opt(newDeclarator))
        )
        );

    noptrNewDeclarator.is("[", expression, "]", opt(attributeSpecifierSeq), o2n("[", constantExpression, "]", opt(attributeSpecifierSeq)));

    newInitializer.is(
        or(
            and("(", opt(expressionList), ")"),
            bracedInitList
        )
        );

    deleteExpression.is(opt("::"), CxxKeyword.DELETE, opt("[", "]"), castExpression);

    noexceptExpression.is(CxxKeyword.NOEXCEPT, "(", expression, ")");

    castExpression.is(
        or(
            and(next("(", typeId, ")"), "(", typeId, ")", castExpression),
            unaryExpression
        )
        ).skipIfOneChild();

    pmExpression.is(castExpression, o2n(or(".*", "->*"), castExpression)).skipIfOneChild();

    multiplicativeExpression.is(pmExpression, o2n(or("*", "/", "%"), pmExpression)).skipIfOneChild();

    additiveExpression.is(multiplicativeExpression, o2n(or("+", "-"), multiplicativeExpression)).skipIfOneChild();

    shiftExpression.is(additiveExpression, o2n(or("<<", ">>"), additiveExpression)).skipIfOneChild();

    relationalExpression.is(shiftExpression, o2n(or("<", ">", "<=", ">="), shiftExpression)).skipIfOneChild();

    equalityExpression.is(relationalExpression, o2n(or("==", "!="), relationalExpression)).skipIfOneChild();

    andExpression.is(equalityExpression, o2n("&", equalityExpression)).skipIfOneChild();

    exclusiveOrExpression.is(andExpression, o2n("^", andExpression)).skipIfOneChild();

    inclusiveOrExpression.is(exclusiveOrExpression, o2n("|", exclusiveOrExpression)).skipIfOneChild();

    logicalAndExpression.is(inclusiveOrExpression, o2n("&&", inclusiveOrExpression)).skipIfOneChild();

    logicalOrExpression.is(logicalAndExpression, o2n("||", logicalAndExpression)).skipIfOneChild();

    conditionalExpression.is(
        or(
            and(logicalOrExpression, "?", expression, ":", assignmentExpression),
            logicalOrExpression
        )
        ).skipIfOneChild();

    assignmentExpression.is(
        or(
            and(logicalOrExpression, assignmentOperator, initializerClause),
            conditionalExpression,
            throwExpression
        )
        ).skipIfOneChild();

    assignmentOperator.is(or("=", "*=", "/=", "%=", "+=", "-=", ">>=", "<<=", "&=", "^=", "|="));

    expression.is(assignmentExpression, o2n(",", assignmentExpression));

    constantExpression.is(conditionalExpression);
  }

  private void statements() {
    statement.is(
        or(
            labeledStatement,
            and(opt(attributeSpecifierSeq), expressionStatement),
            and(opt(attributeSpecifierSeq), compoundStatement),
            and(opt(attributeSpecifierSeq), selectionStatement),
            and(opt(attributeSpecifierSeq), iterationStatement),
            and(opt(attributeSpecifierSeq), jumpStatement),
            declarationStatement,
            and(opt(attributeSpecifierSeq), tryBlock)
        )
        );

    labeledStatement.is(opt(attributeSpecifierSeq), or(IDENTIFIER, and(CxxKeyword.CASE, constantExpression), CxxKeyword.DEFAULT), ":", statement);

    expressionStatement.is(opt(expression), ";");

    compoundStatement.is("{",
                         or(
                           and(
                             opt(statementSeq),
                             "}"
                             ),
                           errorInCompoundStatement
                           )
      );

    if(error_recovery == true){
      errorInCompoundStatement.is(o2n(not("}"), anyToken()), "}");
    }
    else{
      errorInCompoundStatement.is(isFalse());
    }
    
    statementSeq.is(one2n(statement));

    selectionStatement.is(
        or(
            and(CxxKeyword.IF, "(", condition, ")", statement, opt(CxxKeyword.ELSE, statement)),
            and(CxxKeyword.SWITCH, "(", condition, ")", statement)
        )
        );

    condition.is(
        or(
            and(opt(attributeSpecifierSeq), conditionDeclSpecifierSeq, declarator, or(and("=", initializerClause), bracedInitList)),
            expression
        )
        );

    conditionDeclSpecifierSeq.is(
        one2n(
            not(and(declarator, or("=", "{"))),
            declSpecifier
        ),
        opt(attributeSpecifierSeq)
        );

    iterationStatement.is(
        or(
            and(CxxKeyword.WHILE, "(", condition, ")", statement),
            and(CxxKeyword.DO, statement, CxxKeyword.WHILE, "(", expression, ")", ";"),
            and(CxxKeyword.FOR, "(", forInitStatement, opt(condition), ";", opt(expression), ")", statement),
            and(CxxKeyword.FOR, "(", forRangeDeclaration, ":", forRangeInitializer, ")", statement)
        )
        );

    forInitStatement.is(
        or(
            expressionStatement,
            simpleDeclaration
        )
        );

    forRangeDeclaration.is(opt(attributeSpecifierSeq), forrangeDeclSpecifierSeq, declarator);

    forrangeDeclSpecifierSeq.is(
        one2n(
            not(declarator),
            declSpecifier
        ),
        opt(attributeSpecifierSeq)
        );

    forRangeInitializer.is(
        or(
            expression,
            bracedInitList
        )
        );

    jumpStatement.is(
        or(
            and(CxxKeyword.BREAK, ";"),
            and(CxxKeyword.CONTINUE, ";"),
            and(CxxKeyword.RETURN, opt(expression), ";"),
            and(CxxKeyword.RETURN, bracedInitList, ";"),
            and(CxxKeyword.GOTO, IDENTIFIER, ";")//,
            //errorInJumpStatement
          )
        );
    
    // if(error_recovery == true){
    //   errorInJumpStatement.is(o2n(not(";"), anyToken()), ";");
    // }
    // else{
    //   errorInJumpStatement.is(isFalse());
    // }
    
    declarationStatement.is(blockDeclaration);
  }

  private void declarations() {
    declarationSeq.is(one2n(declaration));

    declaration.is(
        or(
            functionDefinition,
            blockDeclaration,
            templateDeclaration,
            explicitInstantiation,
            explicitSpecialization,
            linkageSpecification,
            namespaceDefinition,
            emptyDeclaration,
            attributeDeclaration
        )
        );

    blockDeclaration.is(
        or(
            simpleDeclaration,
            asmDefinition,
            namespaceAliasDefinition,
            usingDeclaration,
            usingDirective,
            staticAssertDeclaration,
            aliasDeclaration,
            opaqueEnumDeclaration
        )
        );

    aliasDeclaration.is(CxxKeyword.USING, IDENTIFIER, opt(attributeSpecifierSeq), "=", typeId);

    simpleDeclaration.is(
        or(
            and(opt(simpleDeclSpecifierSeq), opt(initDeclaratorList), ";"),
            and(attributeSpecifierSeq, opt(simpleDeclSpecifierSeq), initDeclaratorList, ";")//,
            //errorInSimpleDeclaration
          )
        );
    
    // if(error_recovery == true){
    //   errorInSimpleDeclaration.is(o2n(not(";"), anyToken()), ";");
    // }
    // else{
    //   errorInSimpleDeclaration.is(isFalse());
    // }
    
    simpleDeclSpecifierSeq.is(
        one2n(
            not(and(opt(initDeclaratorList), ";")),
            declSpecifier
        ),
        opt(attributeSpecifierSeq)
        );

    staticAssertDeclaration.is(CxxKeyword.STATIC_ASSERT, "(", constantExpression, ",", STRING, ")", ";");

    emptyDeclaration.is(";");

    attributeDeclaration.is(attributeSpecifierSeq, ";");

    declSpecifier.is(
        or(
            CxxKeyword.FRIEND, CxxKeyword.TYPEDEF, CxxKeyword.CONSTEXPR,
            storageClassSpecifier,
            functionSpecifier,
            typeSpecifier
        )
        );

    storageClassSpecifier.is(
        or(CxxKeyword.REGISTER, CxxKeyword.STATIC, CxxKeyword.THREAD_LOCAL, CxxKeyword.EXTERN, CxxKeyword.MUTABLE)
        );

    functionSpecifier.is(
        or(CxxKeyword.INLINE, CxxKeyword.VIRTUAL, CxxKeyword.EXPLICIT)
        );

    typedefName.is(IDENTIFIER);

    typeSpecifier.is(
        or(
            classSpecifier,
            enumSpecifier,
            trailingTypeSpecifier
        )
        );

    trailingTypeSpecifier.is(
        or(
            simpleTypeSpecifier,
            elaboratedTypeSpecifier,
            typenameSpecifier,
            cvQualifier)
        );

    typeSpecifierSeq.is(one2n(typeSpecifier), opt(attributeSpecifierSeq));

    trailingTypeSpecifierSeq.is(one2n(trailingTypeSpecifier), opt(attributeSpecifierSeq));

    simpleTypeSpecifier.is(
        or(
            "char", "char16_t", "char32_t", "wchar_t", "bool", "short", "int", "long", "signed", "unsigned", "float", "double", "void", "auto",
            decltypeSpecifier,
            and(nestedNameSpecifier, CxxKeyword.TEMPLATE, simpleTemplateId),

            // TODO: the "::"-Alternative to nested-name-specifier is because of need to parse
            // stuff like "void foo(::A a);". Figure out if there is another way
            and(opt(or(nestedNameSpecifier, "::")), typeName)
        )
        );

    typeName.is(
        or(
            simpleTemplateId,
            className,
            enumName,
            typedefName)
        );

    decltypeSpecifier.is(CxxKeyword.DECLTYPE, "(", expression, ")");

    elaboratedTypeSpecifier.is(
        or(
            and(classKey, opt(nestedNameSpecifier), opt(CxxKeyword.TEMPLATE), simpleTemplateId),

            // TODO: the "::"-Alternative to nested-name-specifier is because of need to parse
            // stuff like "friend class ::A". Figure out if there is another way
            and(classKey, opt(attributeSpecifierSeq), opt(or(nestedNameSpecifier, "::")), IDENTIFIER),

            and(CxxKeyword.ENUM, opt(nestedNameSpecifier), IDENTIFIER)
        )
        );

    enumName.is(IDENTIFIER);

    enumSpecifier.is(
        or(
            and(enumHead, "{", opt(enumeratorList), "}"),
            and(enumHead, "{", enumeratorList, ",", "}")
        )
        );

    enumHead.is(enumKey, opt(attributeSpecifierSeq), or(and(nestedNameSpecifier, IDENTIFIER), opt(IDENTIFIER)), opt(enumBase));

    opaqueEnumDeclaration.is(enumKey, opt(attributeSpecifierSeq), IDENTIFIER, opt(enumBase), ";");

    enumKey.is(CxxKeyword.ENUM, opt(or(CxxKeyword.CLASS, CxxKeyword.STRUCT)));

    enumBase.is(":", typeSpecifierSeq);

    enumeratorList.is(enumeratorDefinition, o2n(",", enumeratorDefinition));

    enumeratorDefinition.is(enumerator, opt("=", constantExpression));

    enumerator.is(IDENTIFIER);

    namespaceName.is(
        or(
            originalNamespaceName,
            namespaceAlias
        )
        );

    originalNamespaceName.is(IDENTIFIER);

    namespaceDefinition.is(
        or(
            namedNamespaceDefinition,
            unnamedNamespaceDefinition
        )
        );

    namedNamespaceDefinition.is(
        or(
            originalNamespaceDefinition,
            extensionNamespaceDefinition
        )
        );

    originalNamespaceDefinition.is(opt(CxxKeyword.INLINE), CxxKeyword.NAMESPACE, IDENTIFIER, "{", namespaceBody, "}");

    extensionNamespaceDefinition.is(opt(CxxKeyword.INLINE), CxxKeyword.NAMESPACE, originalNamespaceName, "{", namespaceBody, "}");

    unnamedNamespaceDefinition.is(opt(CxxKeyword.INLINE), CxxKeyword.NAMESPACE, "{", namespaceBody, "}");

    namespaceBody.is(opt(declarationSeq));

    namespaceAlias.is(IDENTIFIER);

    namespaceAliasDefinition.is(CxxKeyword.NAMESPACE, IDENTIFIER, "=", qualifiedNamespaceSpecifier, ";");

    qualifiedNamespaceSpecifier.is(opt(nestedNameSpecifier), namespaceName);

    usingDeclaration.is(
        or(
            and(CxxKeyword.USING, opt(CxxKeyword.TYPENAME), nestedNameSpecifier, unqualifiedId, ";"),
            and(CxxKeyword.USING, "::", unqualifiedId, ";")
        )
        );

    usingDirective.is(opt(attributeSpecifier), CxxKeyword.USING, CxxKeyword.NAMESPACE, opt("::"), opt(nestedNameSpecifier), namespaceName, ";");

    asmDefinition.is(CxxKeyword.ASM, "(", STRING, ")", ";");

    linkageSpecification.is(CxxKeyword.EXTERN, STRING, or(and("{", opt(declarationSeq), "}"), declaration));

    attributeSpecifierSeq.is(one2n(attributeSpecifier));

    attributeSpecifier.is(
        or(
            and("[", "[", attributeList, "]", "]"),
            alignmentSpecifier
        ));

    alignmentSpecifier.is(
        or(
            and(CxxKeyword.ALIGNAS, "(", typeId, opt("..."), ")"),
            and(CxxKeyword.ALIGNAS, "(", assignmentExpression, opt("..."), ")")
        ));

    attributeList.is(
        or(
            and(attribute, "...", o2n(",", attribute, "...")),
            and(opt(attribute), o2n(",", opt(attribute)))
        ));

    attribute.is(attributeToken, opt(attributeArgumentClause));

    attributeToken.is(
        or(
            attributeScopedToken,
            IDENTIFIER
        ));

    attributeScopedToken.is(attributeNamespace, "::", IDENTIFIER);

    attributeNamespace.is(IDENTIFIER);

    attributeArgumentClause.is("(", balancedTokenSeq, ")");

    balancedTokenSeq.is(o2n(balancedToken));

    balancedToken.is(
        or(
            IDENTIFIER,
            and("(", balancedTokenSeq, ")"),
            and("{", balancedTokenSeq, "}"),
            and("[", balancedTokenSeq, "]")
        ));
  }

  private void declarators() {
    initDeclaratorList.is(initDeclarator, o2n(",", initDeclarator));

    initDeclarator.is(declarator, opt(initializer));

    declarator.is(
        or(
            ptrDeclarator,
            and(noptrDeclarator, parametersAndQualifiers, trailingReturnType)
        )
        );

    ptrDeclarator.is(
        or(
            and(ptrOperator, ptrDeclarator),
            noptrDeclarator
        )
        );

    noptrDeclarator.is(
        or(
            and(declaratorId, opt(attributeSpecifierSeq)),
            and("(", ptrDeclarator, ")")
        ),
        o2n(
        or(
            parametersAndQualifiers,
            and("[", opt(constantExpression), "]", opt(attributeSpecifierSeq))
        )
        )
        );

    parametersAndQualifiers.is("(", parameterDeclarationClause, ")", opt(attributeSpecifierSeq), opt(cvQualifierSeq), opt(refQualifier), opt(exceptionSpecification));

    trailingReturnType.is("->", trailingTypeSpecifierSeq, opt(abstractDeclarator));

    ptrOperator.is(
        or(
            and("*", opt(attributeSpecifierSeq), opt(cvQualifierSeq)),
            and("&", opt(attributeSpecifierSeq)),
            and("&&", opt(attributeSpecifierSeq)),
            and(nestedNameSpecifier, "*", opt(attributeSpecifierSeq), opt(cvQualifierSeq))
        )
        );

    cvQualifierSeq.is(one2n(cvQualifier));

    cvQualifier.is(
        or(CxxKeyword.CONST, CxxKeyword.VOLATILE)
        );

    refQualifier.is(
        or("&", "&&")
        );

    declaratorId.is(
        or(
            and(opt(nestedNameSpecifier), className),
            and(opt("..."), idExpression)
        )
        );

    typeId.is(typeSpecifierSeq, opt(abstractDeclarator));

    abstractDeclarator.is(
        or(
            ptrAbstractDeclarator,
            and(opt(noptrAbstractDeclarator), parametersAndQualifiers, trailingReturnType),
            abstractPackDeclarator
        )
        );

    ptrAbstractDeclarator.is(o2n(ptrOperator), opt(noptrAbstractDeclarator));

    noptrAbstractDeclarator.is(
        opt("(", ptrAbstractDeclarator, ")"),
        o2n(
        or(
            parametersAndQualifiers,
            and("[", opt(constantExpression), "]", opt(attributeSpecifierSeq))
        )
        )
        );

    abstractPackDeclarator.is(o2n(ptrOperator), noptrAbstractPackDeclarator);

    noptrAbstractPackDeclarator.is(
        "...",
        o2n(or(parametersAndQualifiers,
            and("[", opt(constantExpression), "]", opt(attributeSpecifierSeq))
        )
        )
        );

    parameterDeclarationClause.is(
        or(
            and(parameterDeclarationList, ",", "..."),
            and(opt(parameterDeclarationList), opt("...")),
            "..."
        )
        );

    parameterDeclarationList.is(parameterDeclaration, o2n(",", parameterDeclaration));

    parameterDeclaration.is(
        or(
            and(opt(attributeSpecifierSeq), parameterDeclSpecifierSeq, declarator, opt("=", initializerClause)),
            and(opt(attributeSpecifierSeq), parameterDeclSpecifierSeq, opt(abstractDeclarator), opt("=", initializerClause))
        )
        );

    parameterDeclSpecifierSeq.is(
        o2n(
            not(and(opt(declarator), or("=", ")", ","))),
            declSpecifier
        ),
        opt(attributeSpecifierSeq)
        );

    functionDefinition.is(opt(attributeSpecifierSeq), opt(functionDeclSpecifierSeq), declarator, opt(virtSpecifierSeq), functionBody);

    functionDeclSpecifierSeq.is(
        one2n(
            not(and(declarator, opt(virtSpecifierSeq), functionBody)),
            declSpecifier
        ),
        opt(attributeSpecifierSeq)
        );

    functionBody.is(
        or(
            and(opt(ctorInitializer), compoundStatement),
            functionTryBlock,
            and("=", CxxKeyword.DELETE, ";"),
            and("=", CxxKeyword.DEFAULT, ";")
        )
        );

    initializer.is(
        or(
            and("(", expressionList, ")"),
            braceOrEqualInitializer
        )
        );

    braceOrEqualInitializer.is(
        or(
            and("=", initializerClause),
            bracedInitList
        )
        );

    initializerClause.is(
        or(
            assignmentExpression,
            bracedInitList
        )
        );

    initializerList.is(initializerClause, opt("..."), o2n(",", initializerClause, opt("...")));

    bracedInitList.is("{", opt(initializerList), opt(","), "}");
  }

  private void classes() {
    className.is(
        or(
            simpleTemplateId,
            IDENTIFIER
        )
        );

    classSpecifier.is(classHead, "{", opt(memberSpecification), "}");

    classHead.is(
        or(
            and(classKey, opt(attributeSpecifierSeq), classHeadName, opt(classVirtSpecifier), opt(baseClause)),
            and(classKey, opt(attributeSpecifierSeq), opt(baseClause))
        )
        );

    classHeadName.is(opt(nestedNameSpecifier), className);

    classVirtSpecifier.is(CxxKeyword.FINAL);

    classKey.is(
        or(CxxKeyword.CLASS, CxxKeyword.STRUCT, CxxKeyword.UNION)
        );

    memberSpecification.is(
        one2n(
        or(
            memberDeclaration,
            and(accessSpecifier, ":")
        )
        )
        );

    memberDeclaration.is(
        or(
            and(opt(attributeSpecifierSeq), opt(memberDeclSpecifierSeq), opt(memberDeclaratorList), ";"),
            and(functionDefinition, opt(";")),
            and(opt("::"), nestedNameSpecifier, opt(CxxKeyword.TEMPLATE), unqualifiedId, ";"),
            usingDeclaration,
            staticAssertDeclaration,
            templateDeclaration,
            aliasDeclaration
        )
        );

    memberDeclSpecifierSeq.is(
        one2n(
            not(and(opt(memberDeclaratorList), ";")),
            declSpecifier
        ),
        opt(attributeSpecifierSeq)
        );

    memberDeclaratorList.is(memberDeclarator, o2n(",", memberDeclarator));

    memberDeclarator.is(
        or(
            and(declarator, braceOrEqualInitializer),
            and(declarator, virtSpecifierSeq, opt(pureSpecifier)),
            and(opt(IDENTIFIER), opt(attributeSpecifierSeq), ":", constantExpression),
            declarator
        )
        );

    virtSpecifierSeq.is(one2n(virtSpecifier));

    virtSpecifier.is(
        or(CxxKeyword.OVERRIDE, CxxKeyword.FINAL)
        );

    pureSpecifier.is("=", "0");
  }

  private void derivedClasses() {
    baseClause.is(":", baseSpecifierList);

    baseSpecifierList.is(baseSpecifier, opt("..."), o2n(",", baseSpecifier, opt("...")));

    baseSpecifier.is(
        or(
            and(opt(attributeSpecifierSeq), baseTypeSpecifier),
            and(opt(attributeSpecifierSeq), CxxKeyword.VIRTUAL, opt(accessSpecifier), baseTypeSpecifier),
            and(opt(attributeSpecifierSeq), accessSpecifier, opt(CxxKeyword.VIRTUAL), baseTypeSpecifier)
        )
        );

    classOrDecltype.is(
        or(
            and(opt(nestedNameSpecifier), className),
            decltypeSpecifier)
        );

    baseTypeSpecifier.is(classOrDecltype);

    accessSpecifier.is(
        or(CxxKeyword.PRIVATE, CxxKeyword.PROTECTED, CxxKeyword.PUBLIC)
        );
  }

  private void specialMemberFunctions() {
    conversionFunctionId.is(CxxKeyword.OPERATOR, conversionTypeId);

    conversionTypeId.is(typeSpecifierSeq, opt(conversionDeclarator));

    conversionDeclarator.is(one2n(ptrOperator));

    ctorInitializer.is(":", memInitializerList);

    memInitializerList.is(memInitializer, opt("..."), o2n(",", memInitializer, opt("...")));

    memInitializer.is(memInitializerId, or(and("(", opt(expressionList), ")"), bracedInitList));

    memInitializerId.is(
        or(
            classOrDecltype,
            IDENTIFIER
        )
        );
  }

  private void overloading() {
    operatorFunctionId.is(CxxKeyword.OPERATOR, operator);

    operator.is(
        or(
            and(CxxKeyword.NEW, "[", "]"),
            and(CxxKeyword.DELETE, "[", "]"),
            CxxKeyword.NEW, CxxKeyword.DELETE,
            "+", "-", "!", "=", "^=", "&=", "<=", ">=",
            and("(", ")"),
            and("[", "]"),
            "*", "<", "|=", "&&", "/",
            ">", "<<", "||", "%", "+=", ">>", "++", "^", "-=", ">>=", "--", "&", "*=", "<<=",
            ",", "|", "/=", "==", "->*", "~", "%=", "!=", "->"
        )
        );

    literalOperatorId.is(CxxKeyword.OPERATOR, "\"\"", IDENTIFIER);
  }

  private void templates() {
    templateDeclaration.is(CxxKeyword.TEMPLATE, "<", templateParameterList, ">", declaration);

    templateParameterList.is(templateParameter, o2n(",", templateParameter));

    templateParameter.is(
        or(
            typeParameter,
            parameterDeclaration
        )
        );

    typeParameter.is(
        or(
            and(CxxKeyword.CLASS, opt(IDENTIFIER), "=", typeId),
            and(CxxKeyword.CLASS, opt("..."), opt(IDENTIFIER)),
            and(CxxKeyword.TYPENAME, opt(IDENTIFIER), "=", typeId),
            and(CxxKeyword.TYPENAME, opt("..."), opt(IDENTIFIER)),
            and(CxxKeyword.TEMPLATE, "<", templateParameterList, ">", CxxKeyword.CLASS, opt(IDENTIFIER), "=", idExpression),
            and(CxxKeyword.TEMPLATE, "<", templateParameterList, ">", CxxKeyword.CLASS, opt("..."), opt(IDENTIFIER))
        )
        );

    simpleTemplateId.is(templateName, "<", opt(templateArgumentList), ">");

    templateId.is(
        or(
            simpleTemplateId,
            and(operatorFunctionId, "<", opt(templateArgumentList), ">"),
            and(literalOperatorId, "<", opt(templateArgumentList), ">")
        )
        );

    templateName.is(IDENTIFIER);

    templateArgumentList.is(templateArgument, opt("..."), o2n(",", templateArgument, opt("...")));

    templateArgument.is(
        or(
            typeId,

            // FIXME: workaround to parse stuff like "carray<int, 10>"
            // actually, it should be covered by the next rule (constantExpression)
            // but it doesnt work because of ambiguity template syntax <--> relationalExpression
            shiftExpression,
            constantExpression,

            idExpression
        )
        );

    typenameSpecifier.is(
        CxxKeyword.TYPENAME, nestedNameSpecifier,
        or(and(opt(CxxKeyword.TEMPLATE), simpleTemplateId), IDENTIFIER));

    explicitInstantiation.is(opt(CxxKeyword.EXTERN), CxxKeyword.TEMPLATE, declaration);

    explicitSpecialization.is(CxxKeyword.TEMPLATE, "<", ">", declaration);
  }

  private void exceptionHandling() {
    tryBlock.is(CxxKeyword.TRY, compoundStatement, handlerSeq);

    functionTryBlock.is(CxxKeyword.TRY, opt(ctorInitializer), compoundStatement, handlerSeq);

    handlerSeq.is(one2n(handler));

    handler.is(CxxKeyword.CATCH, "(", exceptionDeclaration, ")", compoundStatement);

    exceptionDeclaration.is(
        or(
            and(opt(attributeSpecifierSeq), typeSpecifierSeq, or(declarator, opt(abstractDeclarator))),
            "..."
        )
        );

    throwExpression.is(CxxKeyword.THROW, opt(assignmentExpression));

    exceptionSpecification.is(
        or(
            dynamicExceptionSpecification,
            noexceptSpecification
        )
        );

    dynamicExceptionSpecification.is(CxxKeyword.THROW, "(", opt(typeIdList), ")");

    typeIdList.is(typeId, opt("..."), o2n(",", typeId, opt("...")));

    noexceptSpecification.is(CxxKeyword.NOEXCEPT, opt("(", constantExpression, ")"));
  }
}
