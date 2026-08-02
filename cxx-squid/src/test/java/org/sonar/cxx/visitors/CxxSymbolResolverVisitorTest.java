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

    // "Foo(int x) = delete;" has a parameter but no compound-statement body: the walker never
    // visits a functionBody node for it, so nothing should ever have been left pending.
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
    // Reproduces the real-world motivating scenario: a bare function parameter passed straight
    // through as a call argument (e.g. SSL_CTX_set1_sigalgs_list(ctx, signature_algorithms))
    // must resolve, via the real Symbol API, to the declared parameter -- not be mistaken for a
    // literal value or left unresolved.
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
    // A function-try-block ("try { body } catch (...) { }") nests its real compound-statement
    // body one level deeper than a plain function body (inside an intermediate functionTryBlock
    // node), so the parameter usage inside it must still resolve through the scope pushed for the
    // function rather than being silently skipped as a declaration-only body.
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

  @Test
  void scopedEnumConstantsAreNotVisibleUnqualified() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverScopedEnums.cc", ".", "");
    captureRoot(tester, squidConfig, visitor);

    SymbolTable rootScope = visitor.getContext().getSymbolTable();
    assertThat(rootScope).isNotNull();

    // Unscoped enum's constant IS visible unqualified in the enclosing scope, unchanged.
    assertThat(rootScope.lookupSymbol("LOW")).isNotNull();

    // Scoped enum's constants are NOT visible unqualified in the enclosing scope.
    assertThat(rootScope.lookupSymbol("STRICT")).isNull();
    assertThat(rootScope.lookupSymbol("LENIENT")).isNull();

    // The scoped enum's own TypeSymbol correctly reports isScopedEnum() and exposes a non-null
    // memberScope() containing its constants.
    Symbol modeSymbol = rootScope.lookupSymbol("Mode");
    assertThat(modeSymbol).isInstanceOf(Symbol.TypeSymbol.class);
    Symbol.TypeSymbol modeTypeSymbol = (Symbol.TypeSymbol) modeSymbol;
    assertThat(modeTypeSymbol.isScopedEnum()).isTrue();
    SymbolTable memberScope = modeTypeSymbol.memberScope();
    assertThat(memberScope).isNotNull();
    assertThat(memberScope.lookupSymbol("STRICT")).isNotNull();
    assertThat(memberScope.lookupSymbol("LENIENT")).isNotNull();

    // The unscoped enum's own TypeSymbol correctly reports isScopedEnum() == false and has no
    // member scope (its constants live in the enclosing scope, not a nested one).
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
    // With no class name to reach the anonymous struct's own member scope through, "x" would
    // otherwise be registered only into a scope nothing else can ever reach. Real C++ semantics
    // for an anonymous struct/union make its members transparently visible in the enclosing
    // scope, so "x" must be reachable directly from the (here, root/global) enclosing scope too.
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
    // An anonymous "enum class" has no name to qualify its constants with at all (Color::RED
    // needs "Color" to exist), so its own child scope -- which a NAMED scoped enum's constants
    // are correctly confined to -- would be unreachable from anywhere. Its constants must instead
    // be registered directly into the enclosing scope, same as an unscoped anonymous enum.
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
    // An anonymous enum has no enumHeadName for getEnumName() to find, so lastEnumName stays
    // null -- but its constants are still registered into the enclosing scope, since constant
    // registration in resolveEnumDeclaration does not depend on the enum itself being named.
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
    // "struct Foo;" is an elaborated-type-specifier declaration with no init-declarator-list at
    // all, so getInitDeclarators returns an empty list and resolveLocalVariableDeclaration's loop
    // body never executes -- no local/global variable is registered for it.
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
    // aliasDeclaration's own name is a bare IDENTIFIER with no declaration-site wrapper node of
    // its own, unlike every other declaration shape isInsideDeclarator recognizes -- without the
    // direct-child check for it, this identifier would be looked up like any other usage,
    // resolve to the symbol resolveAliasDeclaration just registered, and be recorded as a
    // (spurious) usage of itself. "OtherAlias = MyAlias" exercises the opposite direction: the
    // reference to MyAlias on the RHS is a genuine usage and must still be counted.
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
    // "draw" is a member function declarator (has a parametersAndQualifiers descendant), so
    // resolveMemberFields registers it as a SourceCodeFunctionSymbol via resolveMemberFunction,
    // not as a field -- it is excluded from lastResolvedFieldNames(), which reports data members
    // only. "sides" is the plain data member used as a control.
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
    // memberDeclarator's bit-field alternative ([IDENTIFIER] [attrs] ":" constantExpression
    // [init]) has no declarator child at all, unlike every other member shape -- "enabled" and
    // "level" are named bit-fields that must still be registered as data-member fields, while the
    // unnamed "unsigned int : 2;" padding bit-field has no name and is legitimately skipped. A
    // bit-field's name is a bare IDENTIFIER token (not wrapped in a declaratorId like every other
    // member shape), so it is looked up directly by token text rather than via
    // findDeclarationIdentifier (which requires isInsideDeclarator to recognize the shape).
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
    // "s.fld = 1;" must resolve "fld" against Outer's own member scope (via s's declaredType()),
    // not against the ambient/current scope -- a same-named local "int fld = 5;" is deliberately
    // declared just before it as a control: before this fix, the ambient-scope lookup that
    // resolveIdentifierUsage used for every bare IDENTIFIER (including member-access RHS operands)
    // would incorrectly resolve "fld" to this unrelated shadowing local instead of Outer::fld.
    // "s.inner.val = 2;" is a chained member access: "inner" (itself a field, of type Inner) must
    // resolve first via s's type, then "val" must resolve via inner's own declaredType().
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var visitor = new CxxSymbolResolverVisitor<Grammar>();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverMemberAccess.cc", ".", "");
    var root = captureRoot(tester, squidConfig, visitor);

    List<AstNode> fldUsages = new ArrayList<>();
    collectIdentifiers(root, "fld", fldUsages);
    // Declaration sites: Outer::fld field declarator, and the shadowing local's declarator.
    // Usage site: the "fld" in "s.fld = 1;".
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
