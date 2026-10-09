/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) SonarOpenCommunity
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

import static org.assertj.core.api.Assertions.assertThat;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.Token;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxFileTesterHelper;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.squidbridge.api.AstNodeSymbolExtension;
import org.sonar.cxx.squidbridge.api.Symbol;
import org.sonar.cxx.squidbridge.api.SymbolTable;
import org.sonar.cxx.utils.CxxAstNodeHelper;

class CxxSymbolResolverVisitorTest {

  @Test
  void scopeStackReturnsToEmptyAfterFullFileScan() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverScopes.cc", ".", "");
    CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, visitor);

    assertThat(visitor.currentScope()).isNull();
  }

  @Test
  void resolvesFunctionParameterAndLocalVariableSymbols() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverFunctionLocals.cc", ".", "");
    CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, visitor);

    assertThat(visitor.lastResolvedFunctionName()).isEqualTo("add");
    assertThat(visitor.lastResolvedParameterNames()).containsExactly("a", "b");
    assertThat(visitor.lastResolvedLocalVariableNames()).containsExactly("sum");
  }

  @Test
  void deletedFunctionWithParametersDoesNotLeakPendingScope() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverDeletedFunctionParams.cc", ".", "");
    CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, visitor);

    assertThat(visitor.lastResolvedFunctionName()).isEqualTo("Foo");
    assertThat(visitor.lastResolvedParameterNames()).containsExactly("x");
    assertThat(visitor.pendingScopeCount()).isZero();
  }

  @Test
  void resolvesClassTypeSymbolAndMemberFields() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverClassFields.cc", ".", "");
    CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, visitor);

    assertThat(visitor.lastResolvedClassName()).isEqualTo("Point");
    assertThat(visitor.lastResolvedClassIsStruct()).isTrue();
    assertThat(visitor.lastResolvedFieldNames()).containsExactly("x", "y");
  }

  @Test
  void resolvesEnumConstantsAndTypedefs() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverEnumsTypedefs.cc", ".", "");
    CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, visitor);

    assertThat(visitor.lastResolvedEnumName()).isEqualTo("Color");
    assertThat(visitor.lastResolvedEnumConstantNames()).containsExactly("RED", "GREEN", "BLUE");
    assertThat(visitor.lastResolvedTypedefNames()).containsExactly("byte_t");
  }

  @Test
  void resolvesGlobalVariableAndIdentifierUsages() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverGlobalsUsages.cc", ".", "");
    CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, visitor);

    assertThat(visitor.lastResolvedGlobalVariableNames()).containsExactly("counter");
    assertThat(visitor.usageResolutionCount()).isGreaterThan(0);
  }

  @Test
  void bareParameterPassedAsCallArgumentResolvesToItsParameterDeclaration() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverParameterUsageInCall.cc", ".", "");
    var root = captureRoot(tester, squidConfig, visitor);

    AstNode usage = findCallArgumentIdentifier(root, "value");
    Symbol resolved = AstNodeSymbolExtension.getSymbol(usage);

    assertThat(resolved).isNotNull();
    assertThat(resolved.isVariableSymbol()).isTrue();
    assertThat(resolved).isInstanceOf(Symbol.VariableSymbol.class);
    assertThat(((Symbol.VariableSymbol) resolved).isParameter()).isTrue();
  }

  @Test
  void parameterUsedInFunctionTryBlockBodyResolvesToItsParameterDeclaration() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverFunctionTryBlock.cc", ".", "");
    var root = captureRoot(tester, squidConfig, visitor);

    AstNode usage = findCallArgumentIdentifier(root, "value");
    Symbol resolved = AstNodeSymbolExtension.getSymbol(usage);

    assertThat(resolved).isNotNull();
    assertThat(resolved.isVariableSymbol()).isTrue();
    assertThat(resolved).isInstanceOf(Symbol.VariableSymbol.class);
    assertThat(((Symbol.VariableSymbol) resolved).isParameter()).isTrue();
  }

  @Test
  void resolvesPointerDeclarationsParsedAsExpressionStatements() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverPointerDeclarations.cc", ".", "");
    var root = captureRoot(tester, squidConfig, visitor);

    // "a * b" in product() is a multiplication of two parameters, not a declaration
    assertThat(visitor.lastResolvedLocalVariableNames()).containsExactly("kdf", "ctx", "session");

    Symbol kdf = AstNodeSymbolExtension.getSymbol(findCallArgumentIdentifier(root, "kdf"));
    assertThat(kdf).isInstanceOf(Symbol.VariableSymbol.class);
    assertThat(kdf.declaration().getTokenValue()).isEqualTo("kdf");
    assertThat(kdf.usages()).hasSize(1);
    AstNode initializer = ((Symbol.VariableSymbol) kdf).initializer();
    assertThat(initializer).isNotNull();
    assertThat(initializer.getLastChild().getTokenValue()).isEqualTo("EVP_KDF_fetch");

    AstNode fetchCall = root.getDescendants(CxxGrammarImpl.postfixExpression).stream()
      .filter(node -> "EVP_KDF_fetch".equals(CxxAstNodeHelper.getFunctionCallName(node)))
      .findFirst()
      .orElseThrow();
    assertThat(CxxAstNodeHelper.getAssignedSymbol(fetchCall)).isSameAs(kdf);

    Symbol ctx = AstNodeSymbolExtension.getSymbol(findCallArgumentIdentifier(root, "ctx"));
    assertThat(ctx).isInstanceOf(Symbol.VariableSymbol.class);
    assertThat(((Symbol.VariableSymbol) ctx).initializer()).isNull();
  }

  @Test
  void resolvesEveryDeclaratorOfAPointerDeclarationParsedAsAnExpressionStatement() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverPointerDeclarators.cc", ".", "");
    var root = captureRoot(tester, squidConfig, visitor);

    Symbol first = AstNodeSymbolExtension.getSymbol(findCallArgumentIdentifier(root, "first"));
    Symbol second = AstNodeSymbolExtension.getSymbol(findCallArgumentIdentifier(root, "second"));
    assertThat(first).isInstanceOf(Symbol.VariableSymbol.class);
    assertThat(second).isInstanceOf(Symbol.VariableSymbol.class).isNotSameAs(first);

    List<AstNode> calls = root.getDescendants(CxxGrammarImpl.postfixExpression).stream()
      .filter(node -> "EVP_PKEY_CTX_new_id".equals(CxxAstNodeHelper.getFunctionCallName(node)))
      .toList();
    assertThat(CxxAstNodeHelper.getAssignedSymbol(calls.get(0))).isSameAs(first);
    assertThat(CxxAstNodeHelper.getAssignedSymbol(calls.get(1))).isSameAs(second);
    assertThat(((Symbol.VariableSymbol) second).initializer().getLastChild().getTokenValue())
      .isEqualTo("EVP_PKEY_CTX_new_id");
  }

  @Test
  void keepsAProductNamingAVariableOfTheScopeAnExpression() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverPointerDeclarators.cc", ".", "");
    var root = captureRoot(tester, squidConfig, visitor);

    // "UNKNOWN_FACTOR * ctx;" does not declare a second ctx in the scope of the first one
    Symbol ctx = AstNodeSymbolExtension.getSymbol(findCallArgumentIdentifier(root, "ctx"));
    assertThat(ctx).isInstanceOf(Symbol.VariableSymbol.class);
    assertThat(ctx.declaration().getTokenLine()).isEqualTo(8);
  }

  @Test
  void resolvesDeclaredTypeOfPointerDeclarationsParsedAsExpressionStatements() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverPointerDeclarations.cc", ".", "");
    var root = captureRoot(tester, squidConfig, visitor);

    Symbol session = AstNodeSymbolExtension.getSymbol(findCallArgumentIdentifier(root, "session"));
    assertThat(session).isInstanceOf(Symbol.VariableSymbol.class);
    Symbol.TypeSymbol declaredType = ((Symbol.VariableSymbol) session).declaredType();
    assertThat(declaredType).isNotNull();
    assertThat(declaredType.name()).isEqualTo("Session");

    Symbol kdf = AstNodeSymbolExtension.getSymbol(findCallArgumentIdentifier(root, "kdf"));
    assertThat(((Symbol.VariableSymbol) kdf).declaredType()).isNull();
  }

  @Test
  void resolvesInitializerForLocalGlobalAndFieldVariables() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverVariableInitializers.cc", ".", "");
    var root = captureRoot(tester, squidConfig, visitor);

    AstNode localUsage = findCallArgumentIdentifier(root, "local_sigalgs");
    Symbol localSymbol = AstNodeSymbolExtension.getSymbol(localUsage);
    assertThat(localSymbol).isInstanceOf(Symbol.VariableSymbol.class);
    AstNode localInitializer = ((Symbol.VariableSymbol) localSymbol).initializer();
    assertThat(localInitializer).isNotNull();
    assertThat(localInitializer.getTokens().stream().map(Token::getValue))
        .anyMatch(value -> value.contains("local-value"));

    AstNode globalDeclaration = findDeclarationIdentifier(root, "global_sigalgs");
    Symbol globalSymbol = AstNodeSymbolExtension.getSymbol(globalDeclaration);
    assertThat(globalSymbol).isInstanceOf(Symbol.VariableSymbol.class);
    AstNode globalInitializer = ((Symbol.VariableSymbol) globalSymbol).initializer();
    assertThat(globalInitializer).isNotNull();
    assertThat(globalInitializer.getTokens().stream().map(Token::getValue))
        .anyMatch(value -> value.contains("global-value"));

    AstNode fieldDeclaration = findDeclarationIdentifier(root, "name");
    Symbol fieldSymbol = AstNodeSymbolExtension.getSymbol(fieldDeclaration);
    assertThat(fieldSymbol).isInstanceOf(Symbol.VariableSymbol.class);
    AstNode fieldInitializer = ((Symbol.VariableSymbol) fieldSymbol).initializer();
    assertThat(fieldInitializer).isNotNull();
    assertThat(fieldInitializer.getTokens().stream().map(Token::getValue))
        .anyMatch(value -> value.contains("field-default"));
  }

  @Test
  void classifiesAssignmentUsageKinds() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverUsageKinds.cc", ".", "");
    var root = captureRoot(tester, squidConfig, visitor);

    AstNode readUsage = findCallArgumentIdentifier(root, "value");
    Symbol symbol = AstNodeSymbolExtension.getSymbol(readUsage);
    assertThat(symbol).isNotNull();

    List<Symbol.Usage> usages = symbol.usages();
    Map<Symbol.Usage.UsageKind, Long> countsByKind = usages.stream()
        .collect(Collectors.groupingBy(Symbol.Usage::kind, Collectors.counting()));

    assertThat(countsByKind.getOrDefault(Symbol.Usage.UsageKind.WRITE, 0L)).isEqualTo(1L);
    assertThat(countsByKind.getOrDefault(Symbol.Usage.UsageKind.READ_WRITE, 0L)).isEqualTo(1L);
    assertThat(countsByKind.getOrDefault(Symbol.Usage.UsageKind.READ, 0L)).isEqualTo(1L);
  }

  @ParameterizedTest
  @CsvSource({
    "values, false",
    "index, false",
    "target, false",
    "plain, true",
    "grouped, true"
  })
  void onlyTheAssignedOperandIsWritten(String name, boolean written) throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverAssignedOperand.cc", ".", "");
    var root = captureRoot(tester, squidConfig, visitor);

    Symbol symbol = AstNodeSymbolExtension.getSymbol(findCallArgumentIdentifier(root, name));
    assertThat(symbol).isNotNull();

    assertThat(symbol.usages())
      .extracting(Symbol.Usage::kind)
      .as("usage kinds of %s", name)
      .matches(kinds -> kinds.contains(Symbol.Usage.UsageKind.WRITE) == written)
      .doesNotContain(Symbol.Usage.UsageKind.READ_WRITE);
  }

  @Test
  void anArgumentReadAsAParameterIsAUsageOfTheVariable() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverConstructorArguments.cc", ".", "");
    var root = captureRoot(tester, squidConfig, visitor);

    List<AstNode> names = new ArrayList<>();
    collectIdentifiers(root, "name", names);
    AstNode argument = names.get(names.size() - 1);
    assertThat(argument.getTokenLine()).isEqualTo(10);
    assertThat(CxxAstNodeHelper.isUntypedParameterName(argument)).isTrue();

    Symbol symbol = AstNodeSymbolExtension.getSymbol(argument);
    assertThat(symbol).isInstanceOf(Symbol.VariableSymbol.class);
    assertThat(((Symbol.VariableSymbol) symbol).isLocalVariable()).isTrue();
    assertThat(symbol.declaration().getTokenLine()).isEqualTo(9);
    assertThat(symbol.usages())
      .extracting(Symbol.Usage::kind)
      .containsExactly(Symbol.Usage.UsageKind.READ);
  }

  @Test
  void anUntypedParameterNamingATypeIsNotAUsage() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverConstructorArguments.cc", ".", "");
    var root = captureRoot(tester, squidConfig, visitor);

    List<AstNode> configs = new ArrayList<>();
    collectIdentifiers(root, "Config", configs);
    AstNode parameter = configs.get(configs.size() - 1);
    assertThat(parameter.getTokenLine()).isEqualTo(11);
    assertThat(CxxAstNodeHelper.isUntypedParameterName(parameter)).isTrue();
    assertThat(AstNodeSymbolExtension.getSymbol(parameter)).isNull();
  }

  @Test
  void scopedEnumConstantsAreNotVisibleUnqualified() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverScopedEnums.cc", ".", "");
    captureRoot(tester, squidConfig, visitor);

    SymbolTable rootScope = visitor.getContext().getSymbolTable();
    assertThat(rootScope).isNotNull();

    assertThat(rootScope.lookupSymbol("LOW")).isNotNull();
    assertThat(rootScope.lookupSymbol("STRICT")).isNull();
    assertThat(rootScope.lookupSymbol("LENIENT")).isNull();

    Symbol modeSymbol = rootScope.lookupSymbol("Mode");
    assertThat(modeSymbol).isInstanceOf(Symbol.TypeSymbol.class);
    Symbol.TypeSymbol modeTypeSymbol = (Symbol.TypeSymbol) modeSymbol;
    assertThat(modeTypeSymbol.isScopedEnum()).isTrue();
    SymbolTable memberScope = modeTypeSymbol.memberScope();
    assertThat(memberScope).isNotNull();
    assertThat(memberScope.lookupSymbol("STRICT")).isNotNull();
    assertThat(memberScope.lookupSymbol("LENIENT")).isNotNull();

    Symbol levelSymbol = rootScope.lookupSymbol("Level");
    assertThat(levelSymbol).isInstanceOf(Symbol.TypeSymbol.class);
    Symbol.TypeSymbol levelTypeSymbol = (Symbol.TypeSymbol) levelSymbol;
    assertThat(levelTypeSymbol.isScopedEnum()).isFalse();
    assertThat(levelTypeSymbol.memberScope()).isNull();
  }

  @Test
  void anonymousClassRegistersNoClassNameButStillResolvesFields() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverAnonymousClass.cc", ".", "");
    CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, visitor);

    assertThat(visitor.lastResolvedClassName()).isNull();
    assertThat(visitor.lastResolvedFieldNames()).containsExactly("x");
  }

  @Test
  void anonymousClassMembersAreVisibleInEnclosingScope() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverAnonymousClass.cc", ".", "");
    CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, visitor);

    SymbolTable rootScope = visitor.getContext().getSymbolTable();
    assertThat(rootScope).isNotNull();
    Symbol xSymbol = rootScope.lookupSymbol("x");
    assertThat(xSymbol).isInstanceOf(Symbol.VariableSymbol.class);
    assertThat(((Symbol.VariableSymbol) xSymbol).isField()).isTrue();
  }

  @Test
  void anonymousScopedEnumConstantsAreVisibleInEnclosingScope() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverAnonymousScopedEnum.cc", ".", "");
    CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, visitor);

    assertThat(visitor.lastResolvedEnumName()).isNull();
    assertThat(visitor.lastResolvedEnumConstantNames()).containsExactly("RED", "GREEN", "BLUE");

    SymbolTable rootScope = visitor.getContext().getSymbolTable();
    assertThat(rootScope).isNotNull();
    assertThat(rootScope.lookupSymbol("RED")).isNotNull();
    assertThat(rootScope.lookupSymbol("GREEN")).isNotNull();
    assertThat(rootScope.lookupSymbol("BLUE")).isNotNull();
  }

  @Test
  void anonymousEnumRegistersNoEnumNameButStillResolvesConstants() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverAnonymousEnum.cc", ".", "");
    CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, visitor);

    assertThat(visitor.lastResolvedEnumName()).isNull();
    assertThat(visitor.lastResolvedEnumConstantNames()).containsExactly("RED", "GREEN", "BLUE");
  }

  @Test
  void resolvesUnionTypeSymbol() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverUnionType.cc", ".", "");
    CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, visitor);

    assertThat(visitor.lastResolvedClassName()).isEqualTo("Variant");
    assertThat(visitor.lastResolvedClassIsStruct()).isFalse();
    assertThat(visitor.lastResolvedFieldNames()).containsExactly("asInt", "asFloat");
  }

  @Test
  void memberDeclaratorListEmptyForForwardDeclaration() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverForwardDeclaration.cc", ".", "");
    CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, visitor);

    assertThat(visitor.lastResolvedGlobalVariableNames()).isEmpty();
    assertThat(visitor.lastResolvedLocalVariableNames()).isEmpty();
  }

  @Test
  void aliasDeclarationOwnNameIsNotRecordedAsItsOwnUsage() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverAliasDeclaration.cc", ".", "");
    captureRoot(tester, squidConfig, visitor);

    SymbolTable rootScope = visitor.getContext().getSymbolTable();
    assertThat(rootScope).isNotNull();

    Symbol myAlias = rootScope.lookupSymbol("MyAlias");
    assertThat(myAlias).isNotNull();
    assertThat(myAlias.usages()).hasSize(1);

    Symbol otherAlias = rootScope.lookupSymbol("OtherAlias");
    assertThat(otherAlias).isNotNull();
    assertThat(otherAlias.usages()).isEmpty();
  }

  @Test
  void pureVirtualMemberFunctionIsRegisteredAsFunctionSymbolNotField() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverPureVirtualMember.cc", ".", "");
    var root = captureRoot(tester, squidConfig, visitor);

    assertThat(visitor.lastResolvedFieldNames()).containsExactly("sides");

    AstNode drawDeclaration = findDeclarationIdentifier(root, "draw");
    Symbol drawSymbol = AstNodeSymbolExtension.getSymbol(drawDeclaration);
    assertThat(drawSymbol).isInstanceOf(Symbol.FunctionSymbol.class);
    assertThat(drawSymbol.isFunctionSymbol()).isTrue();

    AstNode sidesDeclaration = findDeclarationIdentifier(root, "sides");
    Symbol sidesSymbol = AstNodeSymbolExtension.getSymbol(sidesDeclaration);
    assertThat(sidesSymbol).isInstanceOf(Symbol.VariableSymbol.class);
    assertThat(((Symbol.VariableSymbol) sidesSymbol).initializer()).isNull();
  }

  @Test
  void bitFieldMembersAreRegisteredAsFields() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverBitFieldMembers.cc", ".", "");
    var root = captureRoot(tester, squidConfig, visitor);

    List<AstNode> enabledIdentifiers = new ArrayList<>();
    collectIdentifiers(root, "enabled", enabledIdentifiers);
    assertThat(enabledIdentifiers).hasSize(1);
    Symbol enabledSymbol = AstNodeSymbolExtension.getSymbol(enabledIdentifiers.get(0));
    assertThat(enabledSymbol).isInstanceOf(Symbol.VariableSymbol.class);
    assertThat(((Symbol.VariableSymbol) enabledSymbol).isField()).isTrue();

    List<AstNode> levelIdentifiers = new ArrayList<>();
    collectIdentifiers(root, "level", levelIdentifiers);
    assertThat(levelIdentifiers).hasSize(1);
    Symbol levelSymbol = AstNodeSymbolExtension.getSymbol(levelIdentifiers.get(0));
    assertThat(levelSymbol).isInstanceOf(Symbol.VariableSymbol.class);
    assertThat(((Symbol.VariableSymbol) levelSymbol).isField()).isTrue();

    assertThat(visitor.lastResolvedFieldNames()).containsExactlyInAnyOrder("enabled", "level");
  }

  @Test
  void memberAccessResolvesFieldAgainstObjectsOwnType() throws IOException {
    // "int fld = 5;" is a deliberate shadowing control for Outer::fld.
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverMemberAccess.cc", ".", "");
    var root = captureRoot(tester, squidConfig, visitor);

    List<AstNode> fldUsages = new ArrayList<>();
    collectIdentifiers(root, "fld", fldUsages);
    AstNode fldUsageNode = fldUsages.stream()
      .filter(node -> !CxxAstNodeHelper.isInsideDeclarator(node))
      .findFirst()
      .orElseThrow(() -> new AssertionError("No usage site of 'fld' found."));
    Symbol fldSymbol = AstNodeSymbolExtension.getSymbol(fldUsageNode);
    assertThat(fldSymbol).isInstanceOf(Symbol.VariableSymbol.class);
    Symbol.VariableSymbol fldVariableSymbol = (Symbol.VariableSymbol) fldSymbol;
    assertThat(fldVariableSymbol.isField()).isTrue();
    assertThat(fldVariableSymbol.isLocalVariable()).isFalse();

    List<AstNode> valUsages = new ArrayList<>();
    collectIdentifiers(root, "val", valUsages);
    AstNode valUsageNode = valUsages.stream()
      .filter(node -> !CxxAstNodeHelper.isInsideDeclarator(node))
      .findFirst()
      .orElseThrow(() -> new AssertionError("No usage site of 'val' found."));
    Symbol valSymbol = AstNodeSymbolExtension.getSymbol(valUsageNode);
    assertThat(valSymbol).isInstanceOf(Symbol.VariableSymbol.class);
    assertThat(((Symbol.VariableSymbol) valSymbol).isField()).isTrue();
  }

  @Test
  void memberAccessObjectOperandIsReadNotWrittenByTheAssignment() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverMemberAccess.cc", ".", "");
    var root = captureRoot(tester, squidConfig, visitor);

    List<AstNode> sUsages = new ArrayList<>();
    collectIdentifiers(root, "s", sUsages);
    AstNode sUsageNode = sUsages.stream()
      .filter(node -> !CxxAstNodeHelper.isInsideDeclarator(node))
      .findFirst()
      .orElseThrow(() -> new AssertionError("No usage site of 's' found."));
    Symbol sSymbol = AstNodeSymbolExtension.getSymbol(sUsageNode);
    assertThat(sSymbol).isNotNull();

    Symbol.Usage sUsage = sSymbol.usages().stream()
      .filter(usage -> usage.node() == sUsageNode)
      .findFirst()
      .orElseThrow(() -> new AssertionError("No recorded usage for 's' found."));
    assertThat(sUsage.kind()).isEqualTo(Symbol.Usage.UsageKind.READ);
  }

  @Test
  void memberAccessOnUnresolvedObjectTypeDoesNotFallBackToAmbientScope() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverMemberAccessUnresolvedType.cc", ".", "");
    var root = captureRoot(tester, squidConfig, visitor);

    List<AstNode> knownFieldUsages = new ArrayList<>();
    collectIdentifiers(root, "knownField", knownFieldUsages);
    AstNode usageNode = knownFieldUsages.stream()
      .filter(node -> !CxxAstNodeHelper.isInsideDeclarator(node))
      .findFirst()
      .orElseThrow(() -> new AssertionError("No usage site of 'knownField' found."));

    assertThat(AstNodeSymbolExtension.getSymbol(usageNode)).isNull();
  }

  @Test
  void memberAccessResolvesAgainstParameterAndElaboratedDeclaredTypes() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverParameterMemberAccess.cc", ".", "");
    var root = captureRoot(tester, squidConfig, visitor);

    Map<String, String> declaredTypes = Map.of(
      "api", "DigestApi", "taggedApi", "DigestApi", "hasher", "Hasher", "constHasher", "Hasher",
      "local", "DigestApi");
    declaredTypes.forEach((variable, type) -> {
      Symbol symbol = AstNodeSymbolExtension.getSymbol(findDeclarationIdentifier(root, variable));
      assertThat(symbol).isInstanceOf(Symbol.VariableSymbol.class);
      Symbol.TypeSymbol declaredType = ((Symbol.VariableSymbol) symbol).declaredType();
      assertThat(declaredType).isNotNull();
      assertThat(declaredType.name()).isEqualTo(type);
    });

    List<AstNode> digestCalls = new ArrayList<>();
    collectIdentifiers(root, "digest", digestCalls);
    digestCalls.removeIf(CxxAstNodeHelper::isInsideDeclarator);
    assertThat(digestCalls).hasSize(3);
    for (AstNode digestCall : digestCalls) {
      Symbol digestSymbol = AstNodeSymbolExtension.getSymbol(digestCall);
      assertThat(digestSymbol).isInstanceOf(Symbol.VariableSymbol.class);
      assertThat(((Symbol.VariableSymbol) digestSymbol).isField()).isTrue();
    }

    List<AstNode> hashCalls = new ArrayList<>();
    collectIdentifiers(root, "hash", hashCalls);
    hashCalls.removeIf(CxxAstNodeHelper::isInsideDeclarator);
    assertThat(hashCalls).hasSize(2);
    for (AstNode hashCall : hashCalls) {
      assertThat(AstNodeSymbolExtension.getSymbol(hashCall)).isInstanceOf(Symbol.FunctionSymbol.class);
    }
  }

  private static AstNode captureRoot(org.sonar.cxx.CxxFileTester tester,
      CxxSquidConfiguration squidConfig, CxxSymbolResolverVisitor<Grammar> visitor) throws IOException {
    AstNode[] captured = new AstNode[1];
    var rootCapture = new org.sonar.cxx.squidbridge.SquidAstVisitor<Grammar>() {
      @Override
      public void visitFile(AstNode astNode) {
        captured[0] = astNode;
      }
    };
    CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig, visitor, rootCapture);
    return captured[0];
  }

  private static AstNode findCallArgumentIdentifier(AstNode root, String name) {
    List<AstNode> candidates = new ArrayList<>();
    collectIdentifiers(root, name, candidates);
    for (AstNode candidate : candidates) {
      if (candidate.getFirstAncestor(CxxGrammarImpl.expressionList) != null) {
        return candidate;
      }
    }
    throw new AssertionError("No call-argument usage of '" + name + "' found in the parsed tree.");
  }

  private static AstNode findDeclarationIdentifier(AstNode root, String name) {
    List<AstNode> candidates = new ArrayList<>();
    collectIdentifiers(root, name, candidates);
    for (AstNode candidate : candidates) {
      if (org.sonar.cxx.utils.CxxAstNodeHelper.isInsideDeclarator(candidate)) {
        AstNode declaratorId = candidate.getFirstAncestor(CxxGrammarImpl.declaratorId);
        return declaratorId != null ? declaratorId : candidate;
      }
    }
    throw new AssertionError("No declaration site of '" + name + "' found in the parsed tree.");
  }

  private static void collectIdentifiers(AstNode node, String name, List<AstNode> out) {
    if (node.is(com.sonar.cxx.sslr.api.GenericTokenType.IDENTIFIER) && name.equals(node.getTokenValue())) {
      out.add(node);
    }
    for (AstNode child : node.getChildren()) {
      collectIdentifiers(child, name, out);
    }
  }

}
