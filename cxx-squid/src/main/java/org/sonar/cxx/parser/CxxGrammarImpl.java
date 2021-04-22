/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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
package org.sonar.cxx.parser;

import static com.sonar.sslr.api.GenericTokenType.EOF;
import static com.sonar.sslr.api.GenericTokenType.IDENTIFIER;
import com.sonar.sslr.api.Grammar;
import org.sonar.cxx.config.CxxSquidConfiguration;
import static org.sonar.cxx.parser.CxxTokenType.CHARACTER;
import static org.sonar.cxx.parser.CxxTokenType.NUMBER;
import static org.sonar.cxx.parser.CxxTokenType.STRING;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerfulGrammarBuilder;

/**
 * Parsing expression grammar (PEG)[https://en.wikipedia.org/wiki/Parsing_expression_grammar]
 *
 * The fundamental difference between context-free grammars and parsing expression grammars is that the PEG's choice
 * operator is ordered. If the first alternative succeeds, the second alternative is ignored. The consequence is that if
 * a CFG is transliterated directly to a PEG, any ambiguity in the former is resolved by deterministically picking one
 * parse tree from the possible parses.
 *
 * By carefully choosing the order in which the grammar alternatives are specified, a programmer has a great deal of
 * control over which parse tree is selected. Additional syntax sugar like 'next', 'nextNot' can help.
 */
/**
 * Based on the C++ Standard, Appendix A
 */
@SuppressWarnings({"squid:S00115", "squid:S00103"})
public enum CxxGrammarImpl implements GrammarRuleKey {

  // Misc
  BOOL,
  NULLPTR,
  LITERAL,
  // Top level components
  translationUnit,
  // Expressions
  primaryExpression,
  idExpression,
  unqualifiedId,
  qualifiedId,
  nestedNameSpecifier,
  lambdaExpression,
  lambdaIntroducer,
  lambdaCapture,
  captureDefault,
  captureList,
  capture,
  simpleCapture,
  initCapture,
  lambdaDeclarator,
  foldExpression,
  foldOperator,
  requiresExpression,
  requirementParameterList,
  requirementBody,
  requirementSeq,
  requirement,
  simpleRequirement,
  typeRequirement,
  compoundRequirement,
  returnTypeRequirement,
  nestedRequirement,
  postfixExpression,
  expressionList,
  unaryExpression,
  unaryOperator,
  awaitExpression,
  newExpression,
  newPlacement,
  newTypeId,
  newDeclarator,
  noptrNewDeclarator,
  newInitializer,
  deleteExpression,
  noexceptExpression,
  castExpression,
  pmExpression,
  multiplicativeExpression,
  additiveExpression,
  shiftExpression,
  compareExpression,
  relationalExpression,
  equalityExpression,
  andExpression,
  exclusiveOrExpression,
  inclusiveOrExpression,
  logicalAndExpression,
  logicalOrExpression,
  conditionalExpression,
  yieldExpression,
  assignmentExpression,
  assignmentOperator,
  expression,
  constantExpression,
  // Statements
  statement,
  labeledStatement,
  expressionStatement,
  compoundStatement,
  statementSeq,
  condition,
  selectionStatement,
  iterationStatement,
  initStatement,
  forRangeDeclaration,
  forRangeInitializer,
  jumpStatement,
  coroutineReturnStatement,
  declarationStatement,
  // Declarations
  declarationSeq,
  declaration,
  blockDeclaration,
  nodeclspecFunctionDeclaration,
  aliasDeclaration,
  simpleDeclaration,
  staticAssertDeclaration,
  emptyDeclaration,
  attributeDeclaration,
  declSpecifier,
  recoveredDeclaration,
  conditionDeclSpecifierSeq,
  forRangeDeclSpecifierSeq,
  parameterDeclSpecifierSeq,
  functionDeclSpecifierSeq,
  declSpecifierSeq,
  memberDeclSpecifierSeq,
  identifierList,
  storageClassSpecifier,
  functionSpecifier,
  explicitSpecifier,
  typedefName,
  typeSpecifier,
  typeSpecifierSeq,
  definingTypeSpecifier,
  definingTypeSpecifierSeq,
  simpleTypeSpecifier,
  typeName,
  decltypeSpecifier,
  placeholderTypeSpecifier,
  elaboratedTypeSpecifier,
  elaboratedEnumSpecifier,
  enumName,
  enumSpecifier,
  enumHead,
  enumHeadName,
  opaqueEnumDeclaration,
  enumKey,
  enumBase,
  enumeratorList,
  enumeratorDefinition,
  enumerator,
  usingEnumDeclaration,
  namespaceName,
  namespaceDefinition,
  namedNamespaceDefinition,
  unnamedNamespaceDefinition,
  nestedNamespaceDefinition,
  enclosingNamespaceSpecifier,
  namespaceBody,
  namespaceAlias,
  namespaceAliasDefinition,
  qualifiedNamespaceSpecifier,
  usingDeclaration,
  usingDeclaratorList,
  usingDeclarator,
  usingDirective,
  asmDeclaration,
  asmLabel,
  linkageSpecification,
  attributeSpecifierSeq,
  attributeSpecifier,
  alignmentSpecifier,
  attributeUsingPrefix,
  attributeList,
  attribute,
  attributeToken,
  attributeScopedToken,
  attributeNamespace,
  attributeArgumentClause,
  balancedTokenSeq,
  balancedToken,
  // Declarators
  initDeclaratorList,
  initDeclarator,
  declarator,
  ptrDeclarator,
  noptrDeclarator,
  parametersAndQualifiers,
  trailingReturnType,
  ptrOperator,
  cvQualifierSeq,
  cvQualifier,
  refQualifier,
  declaratorId,
  typeId,
  definingTypeId,
  typeIdEnclosed,
  abstractDeclarator,
  ptrAbstractDeclarator,
  noptrAbstractDeclarator,
  abstractPackDeclarator,
  noptrAbstractPackDeclarator,
  parameterDeclarationClause,
  parameterDeclarationList,
  parameterDeclaration,
  functionDefinition,
  functionBody,
  initializer,
  braceOrEqualInitializer,
  initializerClause,
  initializerList,
  designatedInitializerList,
  designatedInitializerClause,
  designator,
  bracedInitList,
  exprOrBracedInitList,
  // Modules
  moduleDeclaration,
  moduleName,
  modulePartition,
  moduleNameQualifier,
  exportDeclaration,
  moduleImportDeclaration,
  globalModuleFragment,
  privateModuleFragment,
  // Classes
  className,
  classSpecifier,
  classHead,
  classHeadName,
  classVirtSpecifier,
  classKey,
  memberSpecification,
  memberDeclaration,
  memberDeclaratorList,
  memberDeclarator,
  virtSpecifierSeq,
  virtSpecifier,
  pureSpecifier,
  // Derived classes
  baseClause,
  baseSpecifierList,
  baseSpecifier,
  classOrDecltype,
  accessSpecifier,
  // Special member functions
  conversionFunctionId,
  conversionTypeId,
  conversionDeclarator,
  ctorInitializer,
  memInitializerList,
  memInitializer,
  memInitializerId,
  // Overloading
  operatorFunctionId,
  operator,
  literalOperatorId,
  // Templates
  templateDeclaration,
  templateHead,
  templateParameterList,
  requiresClause,
  constraintLogicalOrExpression,
  constraintLogicalAndExpression,
  templateParameter,
  typeParameter,
  typeTraits,
  typeParameterKey,
  typeConstraint,
  innerTypeParameter,
  simpleTemplateId,
  templateId,
  templateName,
  templateArgumentList,
  templateArgument,
  innerSimpleTemplateId,
  innerTrailingTypeSpecifier,
  innerTypeId,
  innerTemplateId,
  innerTemplateArgumentList,
  innerTemplateArgument,
  constraintExpression,
  conceptDefinition,
  conceptName,
  typenameSpecifier,
  explicitInstantiation,
  explicitSpecialization,
  deductionGuide,
  // Exception handling
  tryBlock,
  functionTryBlock,
  handlerSeq,
  handler,
  exceptionDeclaration,
  throwExpression,
  typeIdList,
  noexceptSpecifier,
  // Microsoft Extension: C++/CLI
  cliTopLevelVisibility,
  cliFinallyClause,
  cliFunctionModifiers,
  cliFunctionModifier,
  cliEventDefinition,
  cliEventModifiers,
  cliPropertyOrEventName,
  cliEventType,
  cliParameterArray,
  cliPropertyDefinition,
  cliPropertyDeclSpecifierSeq,
  cliPropertyBody,
  cliPropertyModifiers,
  cliPropertyIndexes,
  cliPropertyIndexParameterList,
  cliAccessorSpecification,
  cliAccessorDeclaration,
  cliDelegateSpecifier,
  cliDelegateDeclSpecifierSeq,
  cliGenericDeclaration,
  cliGenericParameterList,
  cliConstraintClauseList,
  cliConstraintItemList,
  cliGenericParameter,
  cliGenericId,
  cliGenericName,
  cliGenericArgumentList,
  cliGenericArgument,
  cliConstraintClause,
  cliConstraintItem,
  cliAttributes,
  cliAttributeSection,
  cliAttributeTargetSpecifier,
  cliAttributeTarget,
  cliAttributeList,
  cliAttribute,
  cliAttributeArguments,
  cliPositionArgumentList,
  cliNamedArgumentList,
  cliPositionArgument,
  cliNamedArgument,
  cliAttributeArgumentExpression,
  // Microsoft Extension: Attributed ATL
  vcAtlDeclaration,
  vcAtlAttribute,
  // CUDA extension
  cudaKernel;

  public static Grammar create(CxxSquidConfiguration squidConfig) {
    var b = LexerfulGrammarBuilder.create();

    toplevel(b, squidConfig);
    expressions(b);
    statements(b);
    declarations(b);
    modules(b);
    classes(b);
    properties(b);
    overloading(b);
    templates(b);
    generics(b);
    exceptionHandling(b);
    cliAttributes(b);

    misc(b);
    vcAttributedAtl(b);

    b.setRootRule(translationUnit);

    return b.buildWithMemoizationOfMatchesForAllRules();
  }

  private static void misc(LexerfulGrammarBuilder b) {

    b.rule(identifierList).is(
      IDENTIFIER, b.zeroOrMore(",", IDENTIFIER) //C++
    );

    // C++ Boolean literals [lex.bool]
    b.rule(BOOL).is(
      b.firstOf(
        CxxKeyword.TRUE,
        CxxKeyword.FALSE
      )
    );

    // C++ Pointer literals [lex.nullptr]
    b.rule(NULLPTR).is(
      CxxKeyword.NULLPTR
    );

    // C++ Kinds of literals [lex.literal.kinds]
    b.rule(LITERAL).is(
      b.firstOf(
        CHARACTER, // character-literal, including user-defined-literal
        STRING, // string-literal, including user-defined-string-literal
        NUMBER, // integer-literal, floating-literal, including user-defined-literal
        BOOL, // boolean-literal
        NULLPTR // pointer-literal
      )
    ).skip();
  }

  private static void vcAttributedAtl(LexerfulGrammarBuilder b) {

    b.rule(vcAtlAttribute).is(
      "[", b.oneOrMore(b.anyTokenButNot("]")), "]"
    );
    b.rule(vcAtlDeclaration).is(vcAtlAttribute, ";");
  }

  // **A.3 Basics [gram.basic]**
  //
  private static void toplevel(LexerfulGrammarBuilder b, CxxSquidConfiguration squidConfig) {
    if (squidConfig.getBoolean(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES,
                               CxxSquidConfiguration.ERROR_RECOVERY_ENABLED).orElse(Boolean.TRUE)) {
      //
      // parsing with error recovery
      //
      b.rule(translationUnit).is(
        b.firstOf(
          b.sequence(b.optional(globalModuleFragment),
                     moduleDeclaration,
                     b.zeroOrMore(
                       b.firstOf(
                         declaration,
                         recoveredDeclaration)
                     ),
                     b.optional(privateModuleFragment)),
          b.zeroOrMore(
            b.firstOf(
              declaration,
              recoveredDeclaration)
          )
        ),
        EOF
      );

      // eat all tokens until the next declaration is recognized
      // this works only on top level!!!
      b.rule(recoveredDeclaration).is(
        b.oneOrMore(
          b.nextNot(
            b.firstOf(
              declaration,
              EOF
            )
          ), b.anyToken()
        )
      );
    } else {
      b.rule(translationUnit).is(
        b.firstOf(
          b.sequence(b.optional(globalModuleFragment),
                     moduleDeclaration,
                     b.zeroOrMore(declaration),
                     b.optional(privateModuleFragment)), // C++
          b.zeroOrMore(declaration) // C++
        ),
        EOF
      );
    }
  }

  // **A.4 Expressions [gram.expr]**
  //
  private static void expressions(LexerfulGrammarBuilder b) {
    b.rule(primaryExpression).is(
      b.firstOf(
        LITERAL, // C++
        CxxKeyword.THIS, // C++
        // EXTENSION: gcc's statement expression: a compound statement enclosed in parentheses may appear as an expression
        b.sequence("(",
                   b.firstOf(
                     expression,
                     compoundStatement
                   ), ")"
        ), // C++: ( expression )
        idExpression, // C++
        lambdaExpression, // C++
        foldExpression, // C++
        requiresExpression // C++
      )
    ).skipIfOneChild();

    b.rule(idExpression).is(
      b.firstOf(
        qualifiedId, // C++
        unqualifiedId // C++ (PEG: different order)
      )
    ).skip();

    b.rule(unqualifiedId).is(
      b.firstOf(
        // mitigate ambiguity between relational operators < > and angular brackets by looking ahead
        b.sequence(templateId, b.nextNot(b.firstOf("<", ">"))), // C++
        IDENTIFIER, // C++
        operatorFunctionId, // C++
        conversionFunctionId, // C++
        literalOperatorId, // C++
        b.sequence("~", typeName), // C++
        b.sequence("~", decltypeSpecifier), // C++
        //----
        b.sequence("!", className), // C++/CLI
        cliGenericId, // C++/CLI
        CxxKeyword.DEFAULT // C++/CLI
      )
    ).skipIfOneChild();

    b.rule(qualifiedId).is(
      nestedNameSpecifier, b.optional(CxxKeyword.TEMPLATE), unqualifiedId // C++
    );

    b.rule(nestedNameSpecifier).is(
      b.firstOf(
        "::", // C++
        b.sequence(typeName, "::"), // C++
        b.sequence(namespaceName, "::"), // C++
        b.sequence(decltypeSpecifier, "::") // C++
      ),
      b.zeroOrMore(
        b.firstOf(
          b.sequence(IDENTIFIER, "::"), // C++
          b.sequence(b.optional(CxxKeyword.TEMPLATE), simpleTemplateId, "::") // C++
        )
      )
    );

    b.rule(lambdaExpression).is(
      lambdaIntroducer, // C++
      b.optional(
        b.sequence("<", templateParameterList, ">", b.optional(requiresClause)) // C++
      ),
      b.optional(lambdaDeclarator), // C++
      compoundStatement // C++
    );

    b.rule(lambdaIntroducer).is(
      "[", b.optional(lambdaCapture), "]" // C++
    );

    b.rule(lambdaDeclarator).is(
      "(", parameterDeclarationClause, ")", b.optional(declSpecifierSeq), b.optional(noexceptSpecifier),
      b.optional(attributeSpecifierSeq), b.optional(trailingReturnType), b.optional(requiresClause) // C++
    );

    b.rule(lambdaCapture).is(
      b.firstOf(
        b.sequence(captureDefault, b.optional(",", captureList)), // C++
        captureList // C++
      )
    );

    b.rule(captureDefault).is(
      b.firstOf(
        "&",
        "="
      ), // C++
      b.nextNot(capture)
    );

    b.rule(captureList).is(
      b.sequence(capture, b.zeroOrMore(",", capture)) // C++
    );

    b.rule(capture).is(
      b.firstOf(
        b.sequence(simpleCapture, b.nextNot("=")), // C++
        initCapture // C++
      )
    ).skip();

    b.rule(simpleCapture).is(
      b.firstOf(
        b.sequence(IDENTIFIER, b.optional("...")), // C++
        b.sequence("&", IDENTIFIER, b.optional("...")), // C++
        CxxKeyword.THIS, // C++
        b.sequence("*", CxxKeyword.THIS) // C++
      )
    );

    b.rule(initCapture).is(
      b.firstOf(
        b.sequence(b.optional("..."), IDENTIFIER, initializer), // C++
        b.sequence("&", b.optional("..."), IDENTIFIER, initializer) // C++
      )
    );

    b.rule(foldExpression).is(
      "(",
      b.firstOf(
        b.sequence(castExpression, foldOperator, "...", b.optional(foldOperator, castExpression)), // C++
        b.sequence("...", foldOperator, castExpression) // C++
      ),
      ")"
    );

    b.rule(foldOperator).is(
      b.firstOf( // C++
        "+", "-", "*", "/", "%", "ˆ", "&", "|", "<<", ">>",
        "+=", "-=", "*=", "/=", "%=", "ˆ=", "&=", "|=", "<<=", ">>=", "=",
        "==", "!=", "<", ">", "<=", ">=", "&&", "||", ",", ".*", "->*",
        //----
        CxxKeyword.XOR, CxxKeyword.BITAND, CxxKeyword.BITOR, CxxKeyword.XOR_EQ, CxxKeyword.AND_EQ, CxxKeyword.OR_EQ,
        CxxKeyword.NOT_EQ, CxxKeyword.AND, CxxKeyword.OR
      )
    );

    b.rule(requiresExpression).is(
      CxxKeyword.REQUIRES, b.optional(requirementParameterList), requirementBody // C++
    );

    b.rule(requirementParameterList).is(
      "(", b.optional(parameterDeclarationClause), ")" // C++
    );

    b.rule(requirementBody).is(
      "{", requirementSeq, "}" // C++
    );

    b.rule(requirementSeq).is(
      b.oneOrMore(requirement) // C++
    );

    b.rule(requirement).is(
      b.firstOf(
        simpleRequirement, // C++
        typeRequirement, // C++
        compoundRequirement, // C++
        nestedRequirement // C++
      )
    );

    b.rule(simpleRequirement).is(
      expression, ";" // C++
    );

    b.rule(typeRequirement).is(
      CxxKeyword.TYPENAME, b.optional(nestedNameSpecifier), typeName, ";" // C++
    );

    b.rule(compoundRequirement).is(
      "{", expression, "}", b.optional(CxxKeyword.NOEXCEPT), b.optional(returnTypeRequirement), ";" // C++
    );

    b.rule(returnTypeRequirement).is(
      "->", typeConstraint // C++
    );

    b.rule(nestedRequirement).is(
      CxxKeyword.REQUIRES, constraintExpression, ";" // C++
    );

    b.rule(postfixExpression).is(
      b.firstOf(
        b.sequence(
          typenameSpecifier,
          b.firstOf(
            b.sequence("::", CxxKeyword.TYPEID), // C++/CLI
            b.sequence(b.optional(cudaKernel), "(", b.optional(expressionList), ")"), // C++
            bracedInitList // C++
          )
        ),
        b.sequence(
          simpleTypeSpecifier,
          b.firstOf(
            b.sequence("::", CxxKeyword.TYPEID), // C++/CLI
            b.sequence(b.optional(cudaKernel), "(", b.optional(expressionList), ")"), // C++
            bracedInitList // C++
          )
        ),
        primaryExpression, // C++ (PEG: different order)
        b.sequence(CxxKeyword.DYNAMIC_CAST, typeIdEnclosed, "(", expression, ")"), // C++
        b.sequence(CxxKeyword.STATIC_CAST, typeIdEnclosed, "(", expression, ")"), // C++
        b.sequence(CxxKeyword.REINTERPRET_CAST, typeIdEnclosed, "(", expression, ")"), // C++
        b.sequence(CxxKeyword.CONST_CAST, typeIdEnclosed, "(", expression, ")"), //C++
        b.sequence(
          CxxKeyword.TYPEID,
          "(",
          b.firstOf(
            expression, // C++
            typeId // C++
          ),
          ")"
        )
      ),
      b.zeroOrMore(
        b.firstOf(
          b.sequence("[", exprOrBracedInitList, "]"), // C++
          b.sequence("(", b.optional(expressionList), ")"), // C++
          b.sequence(
            b.firstOf(
              ".",
              "->"
            ),
            b.sequence(b.optional(CxxKeyword.TEMPLATE), idExpression) // C++
          ),
          "++", // C++
          "--" // C++
        )
      )
    ).skipIfOneChild();

    b.rule(cudaKernel).is(
      b.sequence("<<", "<", b.optional(expressionList), ">>", ">") // CUDA
    );

    b.rule(typeIdEnclosed).is( // todo
      "<",
      b.firstOf(
        b.sequence(typeId, ">"),
        b.sequence(innerTypeId, ">>")
      )
    );

    b.rule(expressionList).is(
      initializerList // C++
    );

    b.rule(unaryExpression).is(
      b.firstOf(
        b.sequence(unaryOperator, castExpression), // C++ (PEG: different order)
        newExpression, // C++
        postfixExpression, // C++
        b.sequence("++", castExpression), // C++
        b.sequence("--", castExpression), // C++
        awaitExpression, // C++
        b.sequence(
          CxxKeyword.SIZEOF,
          b.firstOf(
            unaryExpression, // C++
            b.sequence("(", typeId, ")"), // C++
            b.sequence("...", "(", IDENTIFIER, ")") // C++
          )
        ),
        b.sequence(CxxKeyword.ALIGNOF, "(", typeId, ")"), // C++
        noexceptExpression, // C++
        deleteExpression // C++
      )
    ).skipIfOneChild();

    b.rule(unaryOperator).is(
      b.firstOf("*", "&", "+", "-", "!", "~", CxxKeyword.NOT, CxxKeyword.COMPL) // C++
    );

    b.rule(awaitExpression).is(
      CxxKeyword.CO_AWAIT, castExpression // C++
    );

    b.rule(noexceptExpression).is(
      CxxKeyword.NOEXCEPT, "(", expression, ")" // C++
    );

    b.rule(newExpression).is(
      b.sequence(
        b.optional("::"), // C++
        b.firstOf(
          CxxKeyword.NEW, // C++
          "gcnew" // C++/CLI
        ),
        b.firstOf(
          b.sequence("(", typeId, ")"), // syntax sugar (todo)
          b.sequence(
            b.optional(newPlacement),
            b.firstOf(
              newTypeId, // C++
              b.sequence("(", typeId, ")") // C++
            ),
            b.optional(newInitializer) // C++
          )
        )
      )
    );

    b.rule(newPlacement).is(
      "(", expressionList, ")" // C++
    );

    b.rule(newTypeId).is(
      typeSpecifierSeq, b.optional(newDeclarator) // C++
    );

    b.rule(newDeclarator).is(
      b.firstOf(
        b.sequence(ptrOperator, b.optional(newDeclarator)), // C++
        noptrNewDeclarator // C++
      )
    );

    b.rule(noptrNewDeclarator).is(
      "[", b.optional(expression), "]", b.optional(attributeSpecifierSeq), // C++
      b.zeroOrMore("[", constantExpression, "]", b.optional(attributeSpecifierSeq)) // C++
    );

    b.rule(newInitializer).is(
      b.firstOf(
        b.sequence("(", b.optional(expressionList), ")"), // C++
        bracedInitList // C++
      )
    );

    b.rule(deleteExpression).is(
      b.optional("::"), CxxKeyword.DELETE, b.optional("[", "]"), castExpression // C++
    );

    b.rule(castExpression).is(
      b.firstOf(
        b.sequence("(", typeId, ")",
                   b.firstOf(
                     castExpression, // C++
                     bracedInitList // C-COMPATIBILITY: C99 compound literals
                   )
        ),
        unaryExpression // C++ (PEG: different order)
      )
    ).skipIfOneChild();

    b.rule(pmExpression).is(
      castExpression, b.zeroOrMore(b.firstOf(".*", "->*"), castExpression) // C++
    ).skipIfOneChild();

    b.rule(multiplicativeExpression).is(
      pmExpression, b.zeroOrMore(b.firstOf("*", "/", "%"), pmExpression) // C++
    ).skipIfOneChild();

    b.rule(additiveExpression).is(
      multiplicativeExpression, b.zeroOrMore(b.firstOf("+", "-"), multiplicativeExpression) // C++
    ).skipIfOneChild();

    b.rule(shiftExpression).is(
      additiveExpression, b.zeroOrMore(b.firstOf("<<", ">>"), additiveExpression) // C++
    ).skipIfOneChild();

    b.rule(compareExpression).is(
      shiftExpression, b.zeroOrMore("<=>", shiftExpression)
    ).skipIfOneChild();

    b.rule(relationalExpression).is(
      compareExpression,
      b.zeroOrMore(
        b.firstOf(
          "<", b.sequence(">", b.nextNot("::", "type")), "<=", ">="),
        compareExpression
      ) // C++
    ).skipIfOneChild();

    b.rule(equalityExpression).is(
      relationalExpression, b.zeroOrMore(b.firstOf("==", "!=", CxxKeyword.NOT_EQ), relationalExpression) // C++
    ).skipIfOneChild();

    b.rule(andExpression).is(
      equalityExpression, b.zeroOrMore(b.firstOf("&", CxxKeyword.BITAND), equalityExpression) // C++
    ).skipIfOneChild();

    b.rule(exclusiveOrExpression).is(
      andExpression, b.zeroOrMore(b.firstOf("^", CxxKeyword.XOR), andExpression) // C++
    ).skipIfOneChild();

    b.rule(inclusiveOrExpression).is(
      exclusiveOrExpression, b.zeroOrMore(b.firstOf("|", CxxKeyword.BITOR), exclusiveOrExpression) // C++
    ).skipIfOneChild();

    b.rule(logicalAndExpression).is(
      inclusiveOrExpression, b.zeroOrMore(b.firstOf("&&", CxxKeyword.AND), inclusiveOrExpression) // C++
    ).skipIfOneChild();

    b.rule(logicalOrExpression).is(
      logicalAndExpression, b.zeroOrMore(b.firstOf("||", CxxKeyword.OR), logicalAndExpression) // C++
    ).skipIfOneChild();

    b.rule(conditionalExpression).is(
      // EXTENSION: gcc's conditional with omitted operands: the expression is optional
      logicalOrExpression, b.optional("?", b.optional(expression), ":", assignmentExpression) // C++
    ).skipIfOneChild();

    b.rule(yieldExpression).is(
      CxxKeyword.CO_YIELD, b.firstOf(assignmentExpression, bracedInitList) // C++
    );

    b.rule(throwExpression).is(
      CxxKeyword.THROW, b.optional(assignmentExpression) // C++
    );

    b.rule(assignmentExpression).is(
      b.firstOf(
        b.sequence(logicalOrExpression, assignmentOperator, initializerClause), // C++ (PEG: different order)
        conditionalExpression, // C++ (PEG: different order)
        yieldExpression, // C++
        throwExpression // C++
      )
    ).skipIfOneChild();

    b.rule(assignmentOperator).is(
      b.firstOf("=", "*=", "/=", "%=", "+=", "-=", ">>=", "<<=", "&=", "^=", "|=",
                CxxKeyword.AND_EQ, CxxKeyword.XOR_EQ, CxxKeyword.OR_EQ) // C++
    );

    b.rule(expression).is(
      assignmentExpression, b.zeroOrMore(",", assignmentExpression) // C++
    );

    b.rule(constantExpression).is(
      conditionalExpression // C++
    );
  }

  // **A.5 Statements [gram.stmt]**
  //
  private static void statements(LexerfulGrammarBuilder b) {

    b.rule(statement).is(
      b.firstOf(
        labeledStatement, // C++
        b.sequence(b.optional(attributeSpecifierSeq), expressionStatement), // C++
        b.sequence(b.optional(attributeSpecifierSeq), compoundStatement), // C++
        b.sequence(b.optional(attributeSpecifierSeq), selectionStatement), // C++
        b.sequence(b.optional(attributeSpecifierSeq), iterationStatement), // C++
        b.sequence(b.optional(attributeSpecifierSeq), jumpStatement), // C++
        declarationStatement, // C++
        b.sequence(b.optional(attributeSpecifierSeq), tryBlock) // C++
      )
    );

    b.rule(initStatement).is(
      b.firstOf(
        expressionStatement, // C++
        simpleDeclaration // C++
      )
    );

    b.rule(condition).is(
      b.firstOf(
        b.sequence(b.optional(attributeSpecifierSeq), conditionDeclSpecifierSeq, declarator, braceOrEqualInitializer), // C++
        expression // C++ (PEG: different order)
      )
    );

    b.rule(labeledStatement).is(
      b.firstOf(
        b.sequence(b.optional(attributeSpecifierSeq), IDENTIFIER, ":", statement), // C++
        b.sequence(
          b.optional(attributeSpecifierSeq), CxxKeyword.CASE, constantExpression,
          b.firstOf(
            b.sequence(":", statement), // C++
            b.sequence("...", constantExpression, ":", statement) // EXTENSION: gcc's case range
          )
        ),
        b.sequence(b.optional(attributeSpecifierSeq), CxxKeyword.DEFAULT, ":", statement) // C++
      )
    );

    b.rule(expressionStatement).is(
      b.optional(expression), ";" // C++
    );

    b.rule(compoundStatement).is(
      "{", b.optional(statementSeq), "}" // C++
    );

    b.rule(statementSeq).is(
      b.oneOrMore(statement) // C++
    );

    b.rule(selectionStatement).is(
      b.firstOf(
        b.sequence(CxxKeyword.IF, b.optional(CxxKeyword.CONSTEXPR), "(", b.optional(initStatement), condition, ")",
                   statement, b.optional(CxxKeyword.ELSE, statement)), // C++
        b.sequence(CxxKeyword.SWITCH, "(", b.optional(initStatement), condition, ")", statement)
      )
    );

    b.rule(conditionDeclSpecifierSeq).is( // decl-specifier-seq
      b.oneOrMore(
        b.nextNot(declarator, b.firstOf("=", "{")),
        declSpecifier
      ),
      b.optional(attributeSpecifierSeq)
    ).skipIfOneChild();

    b.rule(iterationStatement).is(
      b.firstOf(
        b.sequence(CxxKeyword.WHILE, "(", condition, ")",
                   statement), // C++
        b.sequence(CxxKeyword.DO, statement, CxxKeyword.WHILE, "(", expression, ")", ";"), // C++
        b.sequence(CxxKeyword.FOR, "(", initStatement, b.optional(condition), ";", b.optional(expression), ")",
                   statement), // C++
        b.sequence(CxxKeyword.FOR, "(", b.optional(initStatement), forRangeDeclaration, ":", forRangeInitializer, ")",
                   statement), // C++
        b.sequence(CxxKeyword.FOR, "each", "(", forRangeDeclaration, "in", forRangeInitializer, ")",
                   statement) // C++/CLI
      )
    );

    b.rule(forRangeDeclaration).is(
      b.optional(attributeSpecifierSeq),
      b.firstOf(
        b.sequence(forRangeDeclSpecifierSeq, declarator), // C++
        b.sequence(declSpecifierSeq, b.optional(refQualifier), "[", identifierList, "]") // C++ todo declSpecifierSeq?
      )
    );

    b.rule(forRangeDeclSpecifierSeq).is( // decl-specifier-seq
      b.oneOrMore(
        b.nextNot(b.optional(declarator), b.firstOf(":", "in")),
        declSpecifier
      ),
      b.optional(attributeSpecifierSeq)
    ).skipIfOneChild();

    b.rule(forRangeInitializer).is(
      exprOrBracedInitList // C++
    );

    b.rule(jumpStatement).is(
      b.firstOf(
        b.sequence(CxxKeyword.BREAK, ";"), // C++
        b.sequence(CxxKeyword.CONTINUE, ";"), // C++
        b.sequence(CxxKeyword.RETURN, b.optional(exprOrBracedInitList), ";"), // C++
        coroutineReturnStatement, // C++
        b.sequence(CxxKeyword.GOTO, IDENTIFIER, ";") // C++
      )
    );

    b.rule(coroutineReturnStatement).is(
      CxxKeyword.CO_RETURN, b.optional(exprOrBracedInitList), ";"
    );

    b.rule(declarationStatement).is(
      blockDeclaration // C++
    );
  }

  // **A.6 Declarations [gram.dcl]**
  //
  private static void declarations(LexerfulGrammarBuilder b) {

    b.rule(declarationSeq).is(
      b.oneOrMore(declaration) // C++
    ).skipIfOneChild();

    b.rule(declaration).is(
      b.firstOf(
        // identifiers with special meaning: import and module => must be placed before rules that start with an identifier!
        moduleImportDeclaration, // C++ import ...

        blockDeclaration, // C++
        nodeclspecFunctionDeclaration, // C++
        functionDefinition, // C++
        templateDeclaration, // C++
        deductionGuide, // C++
        cliGenericDeclaration, // C++/CLI
        explicitInstantiation, // C++
        explicitSpecialization, // C++
        exportDeclaration, // C++
        linkageSpecification, // C++
        namespaceDefinition, // C++
        emptyDeclaration, // C++
        attributeDeclaration, // C++

        vcAtlDeclaration // Attributted-ATL
      )
    );

    b.rule(blockDeclaration).is(
      b.firstOf(
        simpleDeclaration, // C++
        asmDeclaration, // C++
        namespaceAliasDefinition, // C++
        usingDeclaration, // C++
        usingEnumDeclaration, // C++
        usingDirective, // C++
        staticAssertDeclaration, // C++
        aliasDeclaration, // C++
        opaqueEnumDeclaration // C++
      )
    ).skip();

    b.rule(nodeclspecFunctionDeclaration).is(
      b.optional(attributeSpecifierSeq), declarator, ";" // C++
    );

    b.rule(aliasDeclaration).is(
      CxxKeyword.USING, IDENTIFIER, b.optional(attributeSpecifierSeq), "=", definingTypeId, ";" // C++
    );

    b.rule(simpleDeclaration).is(
      b.firstOf(
        b.sequence( // todo
          b.optional(cliAttributes),
          b.optional(attributeSpecifierSeq),
          b.firstOf(
            b.sequence(declSpecifierSeq, b.optional(initDeclaratorList)),
            b.sequence(b.optional(declSpecifierSeq), initDeclaratorList)
          ),
          ";"
        ), // C++, C++/CLI
        b.sequence(b.optional(attributeSpecifierSeq), declSpecifierSeq, b.optional(refQualifier),
                   "[", identifierList, "]", initializer, ";") // C++
      )
    );

    b.rule(staticAssertDeclaration).is(
      CxxKeyword.STATIC_ASSERT, "(", constantExpression, b.optional(",", STRING), ")", ";" // C++
    );

    b.rule(emptyDeclaration).is(
      ";" // C++
    );

    b.rule(attributeDeclaration).is(
      attributeSpecifierSeq, ";" // C++
    );

    b.rule(declSpecifier).is(
      b.firstOf(
        storageClassSpecifier, // C++
        definingTypeSpecifier, // C++
        functionSpecifier, // C++
        CxxKeyword.FRIEND, // C++
        CxxKeyword.TYPEDEF, // C++
        CxxKeyword.CONSTEXPR, // C++
        CxxKeyword.CONSTEVAL, // C++
        CxxKeyword.CONSTINIT, // C++
        CxxKeyword.INLINE // C++
      )
    );

    b.rule(declSpecifierSeq).is(
      b.oneOrMore(
        b.nextNot( // simpleDeclaration
          b.optional(initDeclaratorList), ";"
        ),
        declSpecifier // C++
      ),
      b.optional(attributeSpecifierSeq) // C++
    ).skipIfOneChild();

    b.rule(storageClassSpecifier).is(
      b.firstOf(
        CxxKeyword.REGISTER, // C++11, C++14, deprecated with C++17
        CxxKeyword.STATIC, // C++
        CxxKeyword.THREAD_LOCAL, // C++
        CxxKeyword.EXTERN, // C++
        CxxKeyword.MUTABLE // C++
      )
    );

    b.rule(functionSpecifier).is(
      b.firstOf(
        CxxKeyword.VIRTUAL, // C++
        explicitSpecifier // C++
      )
    );

    b.rule(explicitSpecifier).is(
      CxxKeyword.EXPLICIT,
      b.optional("(", constantExpression, ")")
    );

    b.rule(typedefName).is(
      b.firstOf(
        b.sequence(IDENTIFIER, b.nextNot("<")), // C++
        simpleTemplateId // C++
      )
    );

    b.rule(typeSpecifier).is( // todo wrong
      b.firstOf(
        classSpecifier, // ???
        enumSpecifier, // ???
        simpleTypeSpecifier, // C++
        elaboratedTypeSpecifier, // C++
        typenameSpecifier, // C++
        cvQualifier, // C++
        cliDelegateSpecifier // C++/CLI
      )
    ).skip();

    b.rule(typeSpecifierSeq).is(
      b.oneOrMore(typeSpecifier), // C++
      b.optional(attributeSpecifierSeq) // C++
    ).skipIfOneChild();

    b.rule(definingTypeSpecifier).is(
      b.firstOf(
        typeSpecifier, // C++
        classSpecifier, // C++
        enumSpecifier // C++
      )
    ).skip();

    b.rule(definingTypeSpecifierSeq).is(
      b.oneOrMore(definingTypeSpecifier), // C++
      b.optional(attributeSpecifierSeq) // C++
    ).skipIfOneChild();

    b.rule(simpleTypeSpecifier).is(
      b.firstOf(
        b.sequence(b.optional(nestedNameSpecifier), typeName), // C++
        b.sequence(nestedNameSpecifier, CxxKeyword.TEMPLATE, simpleTemplateId), // C++
        placeholderTypeSpecifier, // C++
        b.sequence(b.optional(nestedNameSpecifier), templateName), // C++
        CxxKeyword.CHAR, // C++
        CxxKeyword.CHAR8_T, // C++
        CxxKeyword.CHAR16_T, // C++
        CxxKeyword.CHAR32_T, // C++
        CxxKeyword.WCHAR_T, // C++
        CxxKeyword.BOOL, // C++
        CxxKeyword.SHORT, // C++
        CxxKeyword.INT, // C++
        CxxKeyword.LONG, // C++
        CxxKeyword.SIGNED, // C++
        CxxKeyword.UNSIGNED, // C++
        CxxKeyword.FLOAT, // C++
        CxxKeyword.DOUBLE, // C++
        CxxKeyword.VOID, // C++
        decltypeSpecifier // C++ (PEG: different order)
      )
    ).skipIfOneChild();

    b.rule(typeName).is(
      b.firstOf(
        className, // C++
        enumName, // C++
        typedefName // C++
      )
    );

    b.rule(elaboratedTypeSpecifier).is(
      b.optional(cliAttributes),
      b.firstOf( // PEG: different order
        b.sequence(
          classKey,
          b.firstOf(
            simpleTemplateId, // C++
            b.sequence(nestedNameSpecifier, b.optional(CxxKeyword.TEMPLATE), simpleTemplateId), // C++
            b.sequence(b.optional(attributeSpecifierSeq), b.optional(nestedNameSpecifier), IDENTIFIER) // C++
          )
        ),
        elaboratedEnumSpecifier // C++
      )
    );

    b.rule(elaboratedEnumSpecifier).is(
      CxxKeyword.ENUM, b.optional(nestedNameSpecifier), IDENTIFIER // C++
    );

    b.rule(decltypeSpecifier).is(
      CxxKeyword.DECLTYPE, "(",
      b.firstOf(
        expression, // C++
        CxxKeyword.AUTO // C++ (keep for backward compatibility)
      ),
      ")"
    );

    b.rule(placeholderTypeSpecifier).is(
      b.optional(typeConstraint), // C++
      b.firstOf(
        CxxKeyword.AUTO, // C++
        b.sequence(CxxKeyword.DECLTYPE, "(", CxxKeyword.AUTO, ")") // C++
      )
    );

    b.rule(initDeclaratorList).is(
      initDeclarator, b.zeroOrMore(",", initDeclarator) // C++
    );

    b.rule(initDeclarator).is(
      declarator,
      b.firstOf(
        requiresClause,
        b.sequence(b.optional(asmLabel), b.optional(initializer)) // C++ (asmLabel: GCC ASM label)
      )
    );

    b.rule(declarator).is(
      b.firstOf(
        ptrDeclarator, // C++
        b.sequence(noptrDeclarator, parametersAndQualifiers, trailingReturnType) // C++
      )
    );

    b.rule(ptrDeclarator).is(
      b.firstOf(
        b.sequence(ptrOperator, ptrDeclarator), // C++
        noptrDeclarator // C++ (PEG: different order)
      )
    ).skipIfOneChild();

    b.rule(noptrDeclarator).is(
      b.firstOf(
        b.sequence(declaratorId, b.optional(attributeSpecifierSeq)), // C++
        b.sequence("(", ptrDeclarator, ")") // C++
      ),
      b.zeroOrMore(
        b.firstOf(
          parametersAndQualifiers,
          b.sequence("[", b.optional(constantExpression), "]", b.optional(attributeSpecifierSeq))
        )
      )
    ).skipIfOneChild();

    b.rule(parametersAndQualifiers).is(
      "(", parameterDeclarationClause, ")", // C++
      b.optional(attributeSpecifierSeq), // C++ todo wrong position
      b.optional(cvQualifierSeq), // C++
      b.optional(cliFunctionModifiers), // C++/CLI
      b.optional(refQualifier), // C++
      b.optional(noexceptSpecifier), // C++
      b.optional(trailingReturnType) // todo wrong?
    );

    b.rule(trailingReturnType).is(
      "->", typeId // C++
    );

    b.rule(ptrOperator).is(
      b.firstOf(
        b.sequence("*", b.optional(attributeSpecifierSeq), b.optional(cvQualifierSeq)), // C++
        b.sequence("&", b.optional(attributeSpecifierSeq)), // C++
        b.sequence("&&", b.optional(attributeSpecifierSeq)), // C++
        b.sequence(nestedNameSpecifier, "*", b.optional(attributeSpecifierSeq), b.optional(cvQualifierSeq)), //C++
        //C++/CLI handle reference
        b.sequence("^", b.optional(attributeSpecifierSeq), b.optional(cvQualifierSeq)), // C++/CLI
        b.sequence("%", b.optional(attributeSpecifierSeq)), // C++/CLI
        // C++/CLI tracking reference
        b.sequence("^%", b.optional(attributeSpecifierSeq)) // C++/CLI
      )
    );

    b.rule(cvQualifierSeq).is(
      b.oneOrMore(cvQualifier) // C++
    ).skipIfOneChild();

    b.rule(cvQualifier).is(
      b.firstOf(
        CxxKeyword.CONST, // C++
        CxxKeyword.VOLATILE // C++
      )
    );

    b.rule(refQualifier).is(
      b.firstOf(
        "&", // C++
        "&&" // C++
      )
    );

    b.rule(declaratorId).is(
      b.firstOf(
        b.sequence(b.optional(nestedNameSpecifier), className), // todo wrong?
        b.sequence(b.optional("..."), idExpression) // C++
      )
    );

    b.rule(typeId).is(
      typeSpecifierSeq, b.optional(abstractDeclarator) // C++
    ).skip();

    b.rule(definingTypeId).is(
      definingTypeSpecifierSeq, b.optional(abstractDeclarator) // C++
    );

    b.rule(abstractDeclarator).is(
      b.firstOf(
        ptrAbstractDeclarator, // C++
        b.sequence(b.optional(noptrAbstractDeclarator), parametersAndQualifiers, trailingReturnType), // C++
        abstractPackDeclarator // C++
      )
    );

    b.rule(ptrAbstractDeclarator).is(
      b.oneOrMore(
        b.firstOf(
          noptrAbstractDeclarator, // C++
          b.sequence(ptrOperator, b.optional(noptrAbstractDeclarator)) // C++
        )
      )
    );

    b.rule(noptrAbstractDeclarator).is(
      b.oneOrMore(
        b.firstOf(
          parametersAndQualifiers, // C++
          b.sequence("[", b.optional(constantExpression), "]", b.optional(attributeSpecifierSeq)), // C++
          b.sequence("(", ptrAbstractDeclarator, ")") // C++
        )
      )
    );

    b.rule(abstractPackDeclarator).is(
      b.firstOf(
        noptrAbstractPackDeclarator, // C++
        b.sequence(ptrOperator, abstractPackDeclarator) // C++
      )
    );

    b.rule(noptrAbstractPackDeclarator).is(
      b.oneOrMore(
        b.firstOf(
          parametersAndQualifiers, // C++
          b.sequence("[", b.optional(constantExpression), "]", b.optional(attributeSpecifierSeq)), // C++
          "..." // C++
        )
      )
    );

    b.rule(parameterDeclarationClause).is(
      b.firstOf(
        b.sequence(parameterDeclarationList, ",", "..."), // C++
        b.sequence(b.optional(parameterDeclarationList), b.optional("...")), // C++ (PEG: different order)
        cliParameterArray // C++/CLI
      )
    );

    b.rule(cliParameterArray).is(
      b.optional(attribute), "...", parameterDeclaration // C++/CLI
    );

    b.rule(parameterDeclarationList).is(
      parameterDeclaration, b.zeroOrMore(",", parameterDeclaration) // C++
    );

    b.rule(parameterDeclaration).is(
      b.firstOf(
        // solve issue in templateParameter, conflict in initializerClause, relationalExpression (>),
        // sample: template<bool x=false> string f();
        b.sequence(b.optional(attributeSpecifierSeq), parameterDeclSpecifierSeq, declarator,
                   "=", LITERAL), // syntax sugar
        b.sequence(b.optional(attributeSpecifierSeq), b.optional(vcAtlAttribute), parameterDeclSpecifierSeq, declarator,
                   b.optional("=", initializerClause)), // C++
        b.sequence(b.optional(attributeSpecifierSeq), parameterDeclSpecifierSeq, b.optional(abstractDeclarator),
                   b.optional("=", initializerClause))) // C++
    );

    b.rule(parameterDeclSpecifierSeq).is( // is decl-specifier-seq
      b.zeroOrMore(
        b.nextNot(b.optional(declarator), b.firstOf("=", ")", ",")), declSpecifier, b.optional("...") // todo wrong ...
      ),
      b.optional(attributeSpecifierSeq)
    ).skipIfOneChild();

    b.rule(initializer).is(
      b.firstOf(
        braceOrEqualInitializer, // C++
        b.sequence("(", expressionList, ")") // C++
      )
    );

    b.rule(braceOrEqualInitializer).is(
      b.firstOf(
        b.sequence("=", initializerClause), // C++
        bracedInitList // C++
      )
    ).skip();

    b.rule(initializerClause).is(
      b.firstOf(
        assignmentExpression, // C++
        bracedInitList // C++
      )
    ).skipIfOneChild();

    b.rule(bracedInitList).is(
      "{",
      b.firstOf(
        b.sequence(LITERAL, b.oneOrMore(",", LITERAL), "}"), // syntax sugar: speed-up initialisation of big arrays
        b.sequence(initializerList, b.optional(","), "}"), // C++
        b.sequence(designatedInitializerList, b.optional(","), "}"), // C++
        "}" // C++
      )
    );

    b.rule(initializerList).is(
      initializerClause, b.optional("..."), b.zeroOrMore(",", initializerClause, b.optional("...")) // C++
    );

    b.rule(designatedInitializerList).is(
      designatedInitializerClause, b.zeroOrMore(",", designatedInitializerClause) // C++
    );

    b.rule(designatedInitializerClause).is(
      b.firstOf(
        b.sequence(designator, braceOrEqualInitializer), // C++
        initializerClause // C99 mixed
      )
    );

    b.rule(designator).is(
      b.firstOf(
        b.oneOrMore(b.sequence(".", IDENTIFIER)), // C++ & C99
        b.sequence("[", constantExpression, "]", b.zeroOrMore(".", IDENTIFIER)), // C99 designated initializers
        b.sequence("[", constantExpression, "...", constantExpression, "]") // EXTENSION: gcc's designated initializers range
      )
    );

    b.rule(exprOrBracedInitList).is(
      b.firstOf(
        expression, // C++
        bracedInitList // C++
      )
    ).skip();

    b.rule(functionDefinition).is(
      b.optional(attributeSpecifierSeq), // C++
      b.optional(cliAttributes), // C++/CLI
      b.optional(functionDeclSpecifierSeq), // C++
      declarator, //C++
      b.firstOf(
        requiresClause,
        b.optional(virtSpecifierSeq) // C++
      ),
      functionBody // C++
    );

    b.rule(functionDeclSpecifierSeq).is( // is decl-specifier-seq
      b.oneOrMore(
        b.nextNot( // see functionDefinition
          declarator,
          b.firstOf(
            requiresClause,
            b.optional(virtSpecifierSeq)
          ),
          functionBody
        ),
        declSpecifier // C++
      ),
      b.optional(attributeSpecifierSeq)
    ).skipIfOneChild();

    b.rule(functionBody).is(
      b.firstOf(
        b.sequence(b.optional(ctorInitializer), compoundStatement), // C++
        functionTryBlock, // C++
        b.sequence("=", CxxKeyword.DELETE, ";"), // C++
        b.sequence("=", CxxKeyword.DEFAULT, ";") // C++
      )
    );

    b.rule(enumName).is(
      IDENTIFIER, b.nextNot("<") // C++
    );

    b.rule(enumSpecifier).is(
      b.firstOf(
        b.sequence(enumHead, "{", b.optional(enumeratorList), "}"), // C++
        b.sequence(enumHead, "{", enumeratorList, ",", "}") // C++
      )
    );

    b.rule(enumHead).is(
      b.optional(vcAtlAttribute), b.optional(cliTopLevelVisibility), enumKey, b.optional(attributeSpecifierSeq),
      b.optional(enumHeadName), b.optional(enumBase) // C++
    );

    b.rule(enumHeadName).is(
      b.optional(nestedNameSpecifier), IDENTIFIER // C++
    );

    b.rule(opaqueEnumDeclaration).is(
      enumKey, b.optional(attributeSpecifierSeq), enumHeadName, b.optional(enumBase), ";" // C++
    );

    b.rule(enumKey).is(
      CxxKeyword.ENUM, b.optional(b.firstOf(CxxKeyword.CLASS, CxxKeyword.STRUCT)) // C++
    );

    b.rule(enumBase).is(
      ":", typeSpecifierSeq // C++
    );

    b.rule(enumeratorList).is(
      enumeratorDefinition, b.zeroOrMore(",", enumeratorDefinition) // C++
    );

    b.rule(enumeratorDefinition).is(
      enumerator, b.optional("=", constantExpression) // C++
    );

    b.rule(enumerator).is(
      IDENTIFIER, b.optional(attributeSpecifierSeq) // C++
    );

    b.rule(usingEnumDeclaration).is(
      CxxKeyword.USING, elaboratedEnumSpecifier, ";"
    );

    b.rule(namespaceName).is(
      b.firstOf(
        IDENTIFIER, // C++
        namespaceAlias // C++
      )
    );

    b.rule(namespaceDefinition).is(
      b.firstOf(
        namedNamespaceDefinition, // C++
        unnamedNamespaceDefinition, // C++
        nestedNamespaceDefinition // C++
      )
    );

    b.rule(namedNamespaceDefinition).is(
      b.optional(CxxKeyword.INLINE), CxxKeyword.NAMESPACE, b.optional(attributeSpecifierSeq), IDENTIFIER,
      "{", namespaceBody, "}" // C++
    );

    b.rule(unnamedNamespaceDefinition).is(
      b.optional(CxxKeyword.INLINE), CxxKeyword.NAMESPACE, b.optional(attributeSpecifierSeq),
      "{", namespaceBody, "}" // C++
    );

    b.rule(nestedNamespaceDefinition).is(
      CxxKeyword.NAMESPACE, enclosingNamespaceSpecifier, "::", b.optional(CxxKeyword.INLINE), IDENTIFIER,
      "{", namespaceBody, "}" // C++
    );

    b.rule(enclosingNamespaceSpecifier).is(
      IDENTIFIER, b.zeroOrMore("::", b.optional(CxxKeyword.INLINE), IDENTIFIER, b.nextNot("{")) // C++
    );

    b.rule(namespaceBody).is(
      b.optional(declarationSeq) // C++
    );

    b.rule(namespaceAlias).is(
      IDENTIFIER // C++
    );

    b.rule(namespaceAliasDefinition).is(
      CxxKeyword.NAMESPACE, IDENTIFIER, "=", qualifiedNamespaceSpecifier, ";" // C++
    );

    b.rule(qualifiedNamespaceSpecifier).is(
      b.optional(nestedNameSpecifier), namespaceName // C++
    );

    b.rule(usingDirective).is(
      b.optional(attributeSpecifierSeq), CxxKeyword.USING, CxxKeyword.NAMESPACE, b.optional(nestedNameSpecifier),
      namespaceName, ";" // C++
    );

    b.rule(usingDeclaration).is(
      CxxKeyword.USING, usingDeclaratorList, ";" // C++
    );

    b.rule(usingDeclaratorList).is(
      usingDeclarator, b.optional("..."), b.zeroOrMore(",", usingDeclarator, b.optional("...")) // C++
    );

    b.rule(usingDeclarator).is(
      b.optional(CxxKeyword.TYPENAME), nestedNameSpecifier, unqualifiedId // C++
    );

    b.rule(asmDeclaration).is(
      b.firstOf(
        b.sequence(
          b.optional(attributeSpecifierSeq),
          b.firstOf(CxxKeyword.ASM, "__asm__"), // C++ asm; GCC: __asm__
          b.optional(b.firstOf(CxxKeyword.VIRTUAL, CxxKeyword.INLINE, "__virtual__")), // GCC asm qualifiers
          "(", STRING, ")", ";"
        ),
        b.sequence(b.firstOf("__asm", CxxKeyword.ASM), b.firstOf( // VS
                   b.sequence("{", b.oneOrMore(b.nextNot(b.firstOf("}", EOF)), b.anyToken()), "}", b.optional(";")), // VS __asm block
                   b.sequence(b.oneOrMore(b.nextNot(b.firstOf(";", EOF)), b.anyToken()), ";") // VS __asm ... ;
                 )
        )
      )
    );

    b.rule(asmLabel).is(
      b.firstOf(CxxKeyword.ASM, "__asm__"), "(", STRING, ")" // GCC ASM label
    );

    b.rule(linkageSpecification).is(
      CxxKeyword.EXTERN, STRING,
      b.firstOf(
        b.sequence("{", b.optional(declarationSeq), "}"), // C++
        declaration // C++
      )
    );

    b.rule(attributeSpecifierSeq).is(
      b.oneOrMore(attributeSpecifier) // C++
    ).skipIfOneChild();

    b.rule(attributeSpecifier).is(
      b.firstOf(
        b.sequence("[", "[", b.optional(attributeUsingPrefix), attributeList, "]", "]"), // C++
        alignmentSpecifier // C++
      ));

    b.rule(alignmentSpecifier).is(
      b.firstOf(
        b.sequence(CxxKeyword.ALIGNAS, "(", typeId, b.optional("..."), ")"), // C++
        b.sequence(CxxKeyword.ALIGNAS, "(", constantExpression, b.optional("..."), ")"), // C++
        b.sequence(attributeUsingPrefix, ":"), // C++
        b.sequence(CxxKeyword.USING, attributeNamespace, ":") // C++
      ));

    b.rule(attributeUsingPrefix).is(
      CxxKeyword.USING, attributeNamespace, ":" // C++
    );

    b.rule(attributeList).is(
      b.optional(attribute, b.optional("...")), // C++
      b.zeroOrMore(
        ",", b.optional(attribute, b.optional("...")) // C++
      )
    );

    b.rule(attribute).is(
      attributeToken, b.optional(attributeArgumentClause) // C++
    );

    b.rule(attributeToken).is(
      b.firstOf(
        b.sequence(IDENTIFIER, b.nextNot("::")), // C++
        attributeScopedToken // C++
      )
    );

    b.rule(attributeScopedToken).is(
      attributeNamespace, "::", IDENTIFIER // C++
    );

    b.rule(attributeNamespace).is(
      IDENTIFIER // C++
    );

    b.rule(attributeArgumentClause).is(
      "(", b.optional(balancedTokenSeq), ")" // C++
    );

    b.rule(balancedTokenSeq).is(
      b.oneOrMore(balancedToken) // C++
    ).skipIfOneChild();

    b.rule(balancedToken).is(
      b.firstOf(
        b.sequence("(", b.optional(balancedTokenSeq), ")"), // C++
        b.sequence("{", b.optional(balancedTokenSeq), "}"), // C++
        b.sequence("[", b.optional(balancedTokenSeq), "]"), // C++
        b.oneOrMore(b.nextNot(b.firstOf("(", ")", "{", "}", "[", "]", EOF)), b.anyToken()) // C++
      )
    );
  }

  // **A.7 Modules [gram.module]**
  //
  private static void modules(LexerfulGrammarBuilder b) {

    b.rule(moduleDeclaration).is(
      b.optional("export"), "module", moduleName,
      b.optional(modulePartition), b.optional(attributeSpecifierSeq), ";" // C++
    );

    b.rule(moduleName).is(
      b.optional(moduleNameQualifier), IDENTIFIER // C++
    );

    b.rule(modulePartition).is(
      ":", b.optional(moduleNameQualifier), IDENTIFIER // C++
    );

    b.rule(moduleNameQualifier).is(
      IDENTIFIER, ".", b.zeroOrMore(IDENTIFIER, ".") // C++
    );

    b.rule(exportDeclaration).is( // C++
      "export",
      b.firstOf(
        declaration,
        b.sequence("{", b.optional(declarationSeq), "}"),
        moduleImportDeclaration
      )
    );

    b.rule(moduleImportDeclaration).is( // C++
      "import",
      b.firstOf(
        moduleName,
        modulePartition
      //####todo headerName
      ),
      b.optional(attributeSpecifierSeq),
      ";"
    );

    b.rule(globalModuleFragment).is(
      "module", ";", b.optional(declarationSeq) // C++
    );

    b.rule(privateModuleFragment).is(
      "module", ":", "private", ";", b.optional(declarationSeq) // C++
    );
  }

  // **A.8 Classes [gram.class]**
  //
  private static void classes(LexerfulGrammarBuilder b) {
    b.rule(className).is(
      b.firstOf(
        b.sequence(IDENTIFIER, b.nextNot("<")), // C++
        simpleTemplateId // C++
      )
    );

    b.rule(classSpecifier).is(
      b.optional(vcAtlAttribute), classHead, "{", b.optional(memberSpecification), "}" // C++
    );

    b.rule(classHead).is(
      b.optional(cliTopLevelVisibility), b.optional(vcAtlAttribute), classKey, b.optional(attributeSpecifierSeq),
      b.firstOf(
        b.sequence(classHeadName, b.optional(classVirtSpecifier), b.optional(baseClause), // C++
                   b.optional(attributeSpecifierSeq)), // Microsoft: attributeSpecifierSeq
        b.optional(baseClause) // C++
      )
    );

    b.rule(classHeadName).is(
      b.optional(nestedNameSpecifier), className // C++
    );

    b.rule(cliTopLevelVisibility).is( // C++/CLI
      b.firstOf(
        CxxKeyword.PUBLIC,
        CxxKeyword.PRIVATE
      )
    );

    b.rule(classVirtSpecifier).is(
      b.firstOf(
        "final", // C++
        "sealed", // C++/CLI
        "abstract" // C++/CLI
      )
    );

    b.rule(classKey).is(
      b.firstOf(
        b.sequence(b.optional(b.firstOf("ref", "value", "interface")), CxxKeyword.CLASS), // C++, C++/CLI
        b.sequence(b.optional(b.firstOf("ref", "value", "interface")), CxxKeyword.STRUCT), // C++, C++/CLI
        CxxKeyword.UNION // C++, C++/CLI
      )
    );

    b.rule(memberSpecification).is(
      b.oneOrMore(
        b.firstOf(
          memberDeclaration, // C++
          b.sequence(accessSpecifier, ":") // C++
        )
      )
    );

    b.rule(memberDeclaration).is( // todo
      b.firstOf(
        functionDefinition, // C++
        b.sequence(
          b.optional(attributeSpecifierSeq),
          b.optional(b.firstOf(cliAttributes, vcAtlAttribute)),
          b.optional(b.firstOf("initonly", "literal")),
          b.optional(memberDeclSpecifierSeq), // is decl-specifier-seqopt
          b.optional(memberDeclaratorList),
          ";"
        ),
        usingDeclaration, // C++
        usingEnumDeclaration, // C++
        staticAssertDeclaration, // C++
        templateDeclaration, // C++
        explicitSpecialization, // C++
        deductionGuide, // C++
        aliasDeclaration, // C++
        opaqueEnumDeclaration, // C++
        emptyDeclaration, // C++
        //----
        cliGenericDeclaration, // C++/CLI
        cliDelegateSpecifier, // C++/CLI
        cliEventDefinition, // C++/CLI
        cliPropertyDefinition // C++/CLI
      )
    );

    b.rule(cliDelegateDeclSpecifierSeq).is( // C++/CLI
      b.oneOrMore(
        b.nextNot(b.optional(declarator), emptyDeclaration),
        declSpecifier
      )
    ).skipIfOneChild();

    b.rule(cliDelegateSpecifier).is( // C++/CLI
      b.optional(cliAttributes), b.optional(cliTopLevelVisibility), "delegate", cliDelegateDeclSpecifierSeq, declarator,
      emptyDeclaration
    );

    b.rule(memberDeclSpecifierSeq).is( // is decl-specifier-seqopt
      b.oneOrMore(
        b.nextNot(b.optional(memberDeclaratorList), emptyDeclaration), declSpecifier
      ),
      b.optional(attributeSpecifierSeq)
    ).skipIfOneChild();

    b.rule(memberDeclaratorList).is(
      memberDeclarator, b.zeroOrMore(",", memberDeclarator) // C++
    );

    b.rule(memberDeclarator).is(
      b.firstOf(
        b.sequence(declarator, requiresClause), // C++
        b.sequence(declarator, braceOrEqualInitializer), // C++
        b.sequence(b.optional(IDENTIFIER), b.optional(attributeSpecifierSeq), ":", constantExpression,
                   b.optional(braceOrEqualInitializer)), // C++
        b.sequence(declarator, b.optional(virtSpecifierSeq), b.optional(cliFunctionModifiers),
                   b.optional(pureSpecifier)) // C++
      )
    );

    b.rule(virtSpecifierSeq).is(
      b.oneOrMore(virtSpecifier) // C++
    ).skipIfOneChild();

    b.rule(virtSpecifier).is(
      b.firstOf(
        "override", // C++
        "final" // C++
      )
    );

    b.rule(pureSpecifier).is(
      "=", "0" // C++
    );

    b.rule(cliFunctionModifiers).is(
      b.oneOrMore(cliFunctionModifier) // C++/CLI
    );

    b.rule(cliFunctionModifier).is(
      b.firstOf("abstract", CxxKeyword.NEW, "sealed") // C++/CLI
    );

    b.rule(conversionFunctionId).is(
      CxxKeyword.OPERATOR, conversionTypeId // C++
    );

    b.rule(conversionTypeId).is(
      typeSpecifierSeq, b.optional(conversionDeclarator) // C++
    );

    b.rule(conversionDeclarator).is(
      b.oneOrMore(ptrOperator) // C++
    );

    b.rule(baseClause).is(
      ":", baseSpecifierList // C++
    );

    b.rule(baseSpecifierList).is(
      baseSpecifier, b.optional("..."), b.zeroOrMore(",", baseSpecifier, b.optional("...")) // C++
    );

    b.rule(baseSpecifier).is(
      b.optional(attributeSpecifierSeq),
      b.firstOf(
        classOrDecltype, // C++
        b.sequence(CxxKeyword.VIRTUAL, b.optional(accessSpecifier), classOrDecltype), // C++
        b.sequence(accessSpecifier, b.optional(CxxKeyword.VIRTUAL), classOrDecltype) // C++
      )
    );

    b.rule(classOrDecltype).is(
      b.firstOf(
        b.sequence(b.optional(nestedNameSpecifier), typeName), // C++
        b.sequence(b.optional(nestedNameSpecifier), CxxKeyword.TEMPLATE, simpleTemplateId), // C++
        decltypeSpecifier // C++
      )
    );

    b.rule(accessSpecifier).is(
      b.firstOf(
        b.sequence(CxxKeyword.PROTECTED, CxxKeyword.PUBLIC), // C++/CLI
        b.sequence(CxxKeyword.PUBLIC, CxxKeyword.PROTECTED), // C++/CLI
        b.sequence(CxxKeyword.PROTECTED, CxxKeyword.PRIVATE), // C++/CLI
        b.sequence(CxxKeyword.PRIVATE, CxxKeyword.PROTECTED), // C++/CLI
        CxxKeyword.PRIVATE, // C++
        CxxKeyword.PROTECTED, // C++
        CxxKeyword.PUBLIC, // C++
        "internal" // C++/CLI
      )
    );

    b.rule(ctorInitializer).is(
      ":", memInitializerList // C++
    );

    b.rule(memInitializerList).is(
      memInitializer, b.optional("..."), b.zeroOrMore(",", memInitializer, b.optional("...")) // C++
    );

    b.rule(memInitializer).is(
      b.firstOf(
        b.sequence(memInitializerId, "(", b.optional(expressionList), ")"), // C++
        b.sequence(memInitializerId, bracedInitList) // C++
      )
    );

    b.rule(memInitializerId).is(
      b.firstOf(
        classOrDecltype, // C++
        IDENTIFIER // C++
      )
    );

  }

  // **A.9 Overloading [gram.over]**
  //
  private static void overloading(LexerfulGrammarBuilder b) {
    b.rule(operatorFunctionId).is(
      CxxKeyword.OPERATOR, operator
    );

    b.rule(operator).is(
      b.firstOf(
        b.sequence(CxxKeyword.NEW, b.optional("[", "]")), b.sequence(CxxKeyword.DELETE, b.optional("[", "]")),
        CxxKeyword.CO_AWAIT, b.sequence("(", ")"), b.sequence("[", "]"), "->", "->*",
        "~", "!", "+", "-", "*", "/", "%", "^", "&",
        "|", "=", "+=", "-=", "*=", "/=", "%=", "^=", "&=",
        "|=", "==", "!=", "<", ">", "<=", ">=", "<=>", "&&",
        "||", "<<", ">>", "<<=", ">>=", "++", "--", ",",
        //--- alternative tokens
        CxxKeyword.XOR, CxxKeyword.BITAND, CxxKeyword.BITOR, CxxKeyword.COMPL, CxxKeyword.NOT, CxxKeyword.XOR_EQ,
        CxxKeyword.AND_EQ, CxxKeyword.OR_EQ, CxxKeyword.NOT_EQ, CxxKeyword.AND, CxxKeyword.OR
      )
    );

    b.rule(literalOperatorId).is(
      CxxKeyword.OPERATOR, STRING, b.optional(IDENTIFIER) // C++ (string-literal and user-defined-string-literal is both STRING)
    );
  }

  private static void properties(LexerfulGrammarBuilder b) {
    b.rule(cliPropertyOrEventName).is(
      b.firstOf(
        IDENTIFIER,
        CxxKeyword.DEFAULT
      )
    ).skip();

    b.rule(cliPropertyDeclSpecifierSeq).is(
      b.oneOrMore(
        b.nextNot(declarator, b.optional(cliPropertyBody), b.optional(";")), typeSpecifier
      )
    ).skipIfOneChild();

    b.rule(cliPropertyDefinition).is(
      b.optional(cliAttributes),
      b.optional(cliPropertyModifiers),
      "property",
      b.firstOf(
        cliPropertyDeclSpecifierSeq,
        b.sequence(b.optional(nestedNameSpecifier), b.optional(cliPropertyOrEventName))
      ),
      declarator,
      b.firstOf(
        emptyDeclaration,
        cliPropertyBody)
    );

    b.rule(cliPropertyBody).is(
      b.optional(cliPropertyIndexes), "{", cliAccessorSpecification, "}"
    );

    b.rule(cliPropertyModifiers).is(
      b.oneOrMore(b.firstOf(CxxKeyword.VIRTUAL, CxxKeyword.STATIC))
    );

    b.rule(cliPropertyIndexes).is(
      "[", cliPropertyIndexParameterList, "]"
    );

    b.rule(cliPropertyIndexParameterList).is(
      typeId, b.zeroOrMore(",", typeId)
    );

    b.rule(cliAccessorSpecification).is(
      b.zeroOrMore(b.optional(accessSpecifier, ":"), cliAccessorDeclaration)
    );

    b.rule(cliAccessorDeclaration).is(
      b.firstOf(
        functionDefinition,
        b.sequence(b.optional(attribute), b.optional(declSpecifierSeq), b.optional(memberDeclaratorList), ";")
      )
    );

    b.rule(cliEventDefinition).is(
      b.optional(cliAttributes),
      b.optional(cliEventModifiers),
      "event",
      cliEventType,
      IDENTIFIER,
      b.firstOf(
        emptyDeclaration,
        b.sequence("{", cliAccessorSpecification, "}")
      )
    );

    b.rule(cliEventModifiers).is(
      b.oneOrMore(b.firstOf(CxxKeyword.VIRTUAL, CxxKeyword.STATIC))
    );

    b.rule(cliEventType).is(
      b.firstOf(
        b.sequence(b.optional("::"), b.optional(nestedNameSpecifier), typeName, b.optional("^")),
        b.sequence(b.optional("::"), b.optional(nestedNameSpecifier), CxxKeyword.TEMPLATE, templateId, "^")
      )
    );
  }

  // **A.10 Templates [gram.temp]**
  //
  private static void templates(LexerfulGrammarBuilder b) {
    b.rule(templateDeclaration).is(
      templateHead,
      b.firstOf(
        declaration, // C++
        conceptDefinition // C++
      )
    );

    b.rule(templateHead).is(
      CxxKeyword.TEMPLATE,
      "<",
      b.firstOf(
        b.sequence(templateParameterList, ">", b.optional(requiresClause)), // C++
        b.sequence(b.oneOrMore(templateParameter, ","), innerTypeParameter, ">>") // syntax sugar C++
      )
    );

    b.rule(templateParameterList).is(
      templateParameter, b.zeroOrMore(",", templateParameter) // C++
    );

    b.rule(requiresClause).is(
      CxxKeyword.REQUIRES, constraintLogicalOrExpression
    );

    b.rule(constraintLogicalOrExpression).is(
      constraintLogicalAndExpression, b.zeroOrMore("||", constraintLogicalAndExpression)
    ).skipIfOneChild();

    b.rule(constraintLogicalAndExpression).is(
      primaryExpression, b.zeroOrMore("&&", primaryExpression)
    ).skipIfOneChild();

    b.rule(templateParameter).is(
      b.firstOf(
        typeParameter, // C++
        parameterDeclaration // C++
      )
    );

    b.rule(typeParameter).is(
      b.firstOf(
        b.sequence(
          typeParameterKey,
          b.firstOf(
            typeTraits, // syntax sugar to handle type traits ... ::type
            b.sequence(b.optional(IDENTIFIER), "=", typeId), // C++
            b.sequence(b.optional("..."), b.optional(IDENTIFIER)) // C++ (PEG: different order)
          )
        ),
        b.sequence(
          templateHead,
          typeParameterKey,
          b.firstOf(
            b.sequence(b.optional(IDENTIFIER), "=", idExpression), // C++
            b.sequence(b.optional("..."), b.optional(IDENTIFIER)) // C++ (PEG: different order)
          )
        ),
        b.sequence( // C++ (PEG: different order)
          typeConstraint,
          b.firstOf(
            b.sequence(b.optional(IDENTIFIER), "=", typeId), // C++
            b.sequence(b.optional("..."), b.optional(IDENTIFIER)) // C++ (PEG: different order)
          )
        )
      )
    );

    b.rule(typeTraits).is( // syntax sugar to handle type traits ...::type  (not part of C++ grammar)
      b.zeroOrMore(b.sequence(IDENTIFIER, "::")),
      simpleTemplateId, "::", "type", b.optional("*"),
      b.optional("=", initializerClause)
    );

    b.rule(innerTypeParameter).is(
      b.firstOf(
        b.sequence(typeParameterKey, b.optional(IDENTIFIER), "=", innerTypeId),
        b.sequence(templateHead, CxxKeyword.CLASS, b.optional(IDENTIFIER), "=", innerTypeId)
      )
    );

    b.rule(typeParameterKey).is(
      b.firstOf(
        CxxKeyword.CLASS, // C++
        CxxKeyword.TYPENAME // C++
      )
    );

    b.rule(typeConstraint).is(
      b.optional(nestedNameSpecifier), // C++
      conceptName, // C++
      b.optional(b.sequence("<", b.optional(templateArgumentList), ">")) // C++
    );

    b.rule(simpleTemplateId).is(
      templateName, "<",
      b.firstOf(
        b.sequence(b.optional(templateArgumentList), ">"), // C++
        b.sequence(innerTemplateId, ">>") // syntax sugar C++
      )
    );

    b.rule(innerTemplateId).is(
      b.zeroOrMore(b.nextNot(innerTypeId, ">>"), templateArgument, b.optional("..."), ","), innerTypeId
    );

    b.rule(innerTypeId).is(
      b.firstOf(
        innerTrailingTypeSpecifier,
        b.sequence(b.oneOrMore(typeSpecifier), innerTrailingTypeSpecifier),
        b.sequence(typeSpecifierSeq, b.optional(noptrAbstractDeclarator), parametersAndQualifiers, "->",
                   innerTrailingTypeSpecifier)
      )
    );

    b.rule(innerTrailingTypeSpecifier).is(
      b.firstOf(
        // simpleTypeSpecifier:
        b.sequence(
          b.optional("::"),
          b.optional(nestedNameSpecifier),
          b.optional(CxxKeyword.TEMPLATE),
          innerSimpleTemplateId
        ),
        // elaboratedTypeSpecifier:
        b
          .sequence(b.optional(cliAttributes), classKey, b.optional(nestedNameSpecifier), b
                    .optional(CxxKeyword.TEMPLATE), innerSimpleTemplateId),
        // typenameSpecifier:
        b.sequence(CxxKeyword.TYPENAME, nestedNameSpecifier, CxxKeyword.TEMPLATE, innerSimpleTemplateId)
      // cvQualifier, cliDelegateSpecifier: never end with a template
      )
    );

    b.rule(innerSimpleTemplateId).is(
      templateName, "<", innerTemplateArgumentList
    );

    b.rule(templateId).is(
      b.firstOf(
        simpleTemplateId, // C++
        b.sequence(
          b.firstOf(
            operatorFunctionId,
            literalOperatorId
          ),
          "<", b.optional(templateArgumentList), ">" // C++
        ),
        b.sequence(
          b.firstOf(
            operatorFunctionId,
            literalOperatorId
          ),
          "<", innerTemplateId, ">>" // syntax sugar C++
        )
      )
    );

    b.rule(templateName).is(
      IDENTIFIER // C++
    );

    b.rule(templateArgumentList).is(
      templateArgument, b.optional("..."), b.zeroOrMore(",", templateArgument, b.optional("...")) // C++
    );

    b.rule(innerTemplateArgumentList).is(
      innerTemplateArgument, b.optional("..."), b.zeroOrMore(",", innerTemplateArgument, b.optional("..."))
    );

    b.rule(templateArgument).is(
      b.firstOf(
        b.sequence(typeId, b.next(b.firstOf(">", ",", "..."))), // C++
        // FIXME: workaround to parse stuff like "carray<int, 10>" actually, it should be covered by the next rule (constantExpression)
        // but it doesnt work because of ambiguity template syntax <--> relationalExpression
        b.sequence(shiftExpression, b.next(b.firstOf(">", ",", "..."))),
        b.sequence(constantExpression, b.next(b.firstOf(">", ",", "..."))), // C++
        b.sequence(idExpression, b.next(b.firstOf(">", ",", "..."))) // C++
      )
    );

    b.rule(innerTemplateArgument).is(
      b.firstOf(
        b.sequence(typeId, b.next(b.firstOf(">>", ",", "..."))),
        b.sequence(typenameSpecifier, b.next(b.firstOf(">>", ",", "..."))), // seen in gnu system headers
        // FIXME: workaround to parse stuff like "carray<int, 10>", see above
        b.sequence(additiveExpression, b.next(b.firstOf(">>", ","))),
        b.sequence(constantExpression, b.next(b.firstOf(">>", ","))),
        b.sequence(idExpression, b.next(b.firstOf(">>", ",")))
      )
    );

    b.rule(constraintExpression).is(
      logicalOrExpression // C++
    );

    b.rule(deductionGuide).is(
      b.optional(explicitSpecifier), templateName, "(", parameterDeclarationClause, ")", "->", simpleTemplateId, ";" // C++
    );

    b.rule(conceptDefinition).is(
      CxxKeyword.CONCEPT, conceptName, "=", constraintExpression, ";" // C++
    );

    b.rule(conceptName).is(
      IDENTIFIER // C++
    );

    b.rule(typenameSpecifier).is(
      b.firstOf(
        b.sequence(
          CxxKeyword.TYPENAME, // C++
          b.firstOf(
            b.sequence(
              nestedNameSpecifier, // C++
              b.firstOf(
                b.sequence(b.optional(CxxKeyword.TEMPLATE), simpleTemplateId), // C++
                IDENTIFIER // C++
              )),
            IDENTIFIER // C++ syntax sugar to avoid syntax errors in case of types without a declaration
          )
        ),
        IDENTIFIER // special cases  ... ::typeid (C++/CLI)
      )
    );

    b.rule(explicitInstantiation).is(
      b.optional(CxxKeyword.EXTERN), CxxKeyword.TEMPLATE, declaration // C++
    );

    b.rule(explicitSpecialization).is(
      CxxKeyword.TEMPLATE, "<", ">", declaration // C++
    );

  }

  private static void generics(LexerfulGrammarBuilder b) {
    b.rule(cliGenericDeclaration).is(
      "generic", "<", cliGenericParameterList, ">", b.optional(cliConstraintClauseList), declaration
    );

    b.rule(cliGenericParameterList).is(
      cliGenericParameter, b.zeroOrMore(",", cliGenericParameter)
    );

    b.rule(cliGenericParameter).is(
      b.optional(attribute), b.firstOf(CxxKeyword.CLASS, CxxKeyword.TYPENAME), IDENTIFIER
    );

    b.rule(cliGenericId).is(
      cliGenericName, "<", cliGenericArgumentList, ">"
    );

    b.rule(cliGenericName).is(
      b.firstOf(IDENTIFIER, operatorFunctionId)
    );

    b.rule(cliGenericArgumentList).is(
      cliGenericArgument, b.zeroOrMore(",", cliGenericArgument)
    );

    b.rule(cliGenericArgument).is(
      typeId
    );

    b.rule(cliConstraintClauseList).is(
      cliConstraintClause, b.zeroOrMore(cliConstraintClause)
    );

    b.rule(cliConstraintClause).is(
      "where", IDENTIFIER, ":", cliConstraintItemList
    );

    b.rule(cliConstraintItemList).is(
      cliConstraintItem, b.zeroOrMore(",", cliConstraintItem)
    );

    b.rule(cliConstraintItem).is(
      b.firstOf(
        typeId,
        b.sequence(b.firstOf("ref", "value"), b.firstOf(CxxKeyword.CLASS, CxxKeyword.STRUCT)),
        "gcnew"
      )
    );
  }

  // **A.11 Exception handling [gram.except]**
  //
  private static void exceptionHandling(LexerfulGrammarBuilder b) {

    b.rule(tryBlock).is(
      CxxKeyword.TRY, compoundStatement, // C++
      b.firstOf(
        b.sequence(handlerSeq, b.optional(cliFinallyClause)), // C++: handlerSeq; C++/CLI: cliFinallyClause
        cliFinallyClause // C++/CLI
      )
    );

    b.rule(functionTryBlock).is(
      CxxKeyword.TRY, b.optional(ctorInitializer), compoundStatement, // C++
      b.firstOf(
        b.sequence(handlerSeq, b.optional(cliFinallyClause)), // C++: handlerSeq; C++/CLI: cliFinallyClause
        cliFinallyClause // C++/CLI
      )
    );

    b.rule(handlerSeq).is(
      b.oneOrMore(handler) // C++
    ).skipIfOneChild();

    b.rule(cliFinallyClause).is(
      "finally", compoundStatement // C++/CLI
    );

    b.rule(handler).is(
      CxxKeyword.CATCH, "(", exceptionDeclaration, ")", compoundStatement // C++
    );

    b.rule(exceptionDeclaration).is(
      b.firstOf(
        b.sequence(
          b.optional(attributeSpecifierSeq), typeSpecifierSeq,
          b.firstOf(
            declarator, // C++
            b.optional(abstractDeclarator) // C++
          )
        ),
        "..." // C++
      )
    );

    b.rule(noexceptSpecifier).is(
      b.firstOf(
        b.sequence(CxxKeyword.NOEXCEPT, "(", constantExpression, ")"), // C++
        CxxKeyword.NOEXCEPT, // C++
        // removed with C++20, keep it for backward compatibility (C++ / Microsoft: typeIdList)
        b.sequence(CxxKeyword.THROW, "(", b.optional(typeIdList), ")")
      )
    );

    b.rule(typeIdList).is(
      b.firstOf(
        b.sequence(typeId, b.optional("..."), b.zeroOrMore(",", typeId, b.optional("..."))), // C++
        "..." // Microsoft extension
      )
    );

  }

  private static void cliAttributes(LexerfulGrammarBuilder b) {
    b.rule(cliAttributes).is(
      b.oneOrMore(cliAttributeSection)
    );

    b.rule(cliAttributeSection).is(
      "[", b.optional(cliAttributeTargetSpecifier), cliAttributeList, "]"
    );

    b.rule(cliAttributeTargetSpecifier).is(
      cliAttributeTarget, ":"
    );

    b.rule(cliAttributeTarget).is(
      b.firstOf(
        "assembly", CxxKeyword.CLASS, "constructor", "delegate", CxxKeyword.ENUM, "event", "field",
        "interface", "method", "parameter", "property", "returnvalue", CxxKeyword.STRUCT
      ));

    b.rule(cliAttributeList).is(
      cliAttribute, b.zeroOrMore(",", cliAttribute)
    );

    b.rule(cliAttribute).is(
      b.optional(nestedNameSpecifier), typeName, b.optional(cliAttributeArguments)
    );

    b.rule(cliAttributeArguments).is(
      "(",
      b.firstOf(
        b.optional(cliPositionArgumentList),
        b.sequence(cliPositionArgumentList, ",", cliNamedArgumentList),
        cliNamedArgumentList
      ),
      ")"
    );

    b.rule(cliPositionArgumentList).is(
      cliPositionArgument, b.zeroOrMore(",", cliPositionArgument)
    );

    b.rule(cliPositionArgument).is(
      cliAttributeArgumentExpression
    );

    b.rule(cliNamedArgumentList).is(
      cliNamedArgument, b.zeroOrMore(",", cliNamedArgument)
    );

    b.rule(cliNamedArgument).is(
      IDENTIFIER, "=", cliAttributeArgumentExpression
    );

    b.rule(cliAttributeArgumentExpression).is(
      assignmentExpression
    );

  }

}
