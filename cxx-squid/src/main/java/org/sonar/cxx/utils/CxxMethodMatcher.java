/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.cxx.utils;

import com.sonar.cxx.sslr.api.AstNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.CheckForNull;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.squidbridge.api.AstNodeSymbolExtension;
import org.sonar.cxx.squidbridge.api.Symbol;
import org.sonar.cxx.squidbridge.api.Type;

/**
 * Fluent API for matching C++ functions and methods based on type, name, and parameters.
 *
 * <p>This class provides a builder pattern for creating function/method matchers,
 * similar to sonar-java's MethodMatchers but adapted for C++ grammar and semantics.
 *
 * <p>Example usage:
 * <pre>
 * CxxMethodMatcher matcher = CxxMethodMatcher.create()
 *     .ofTypes("EVP_CIPHER_CTX")
 *     .names("EVP_CipherInit", "EVP_CipherInit_ex")
 *     .withAnyParameters()
 *     .build();
 *
 * if (matcher.matches(functionCallNode)) {
 *     // Handle matched function call
 * }
 * </pre>
 */
public final class CxxMethodMatcher {

  private final Predicate<Type> typePredicate;
  private final Predicate<String> namePredicate;
  private final List<Predicate<List<Type>>> parameterMatchers;

  private CxxMethodMatcher(Predicate<Type> typePredicate,
                           Predicate<String> namePredicate,
                           List<Predicate<List<Type>>> parameterMatchers) {
    this.typePredicate = Objects.requireNonNull(typePredicate, "typePredicate cannot be null");
    this.namePredicate = Objects.requireNonNull(namePredicate, "namePredicate cannot be null");
    this.parameterMatchers = new ArrayList<>(Objects.requireNonNull(parameterMatchers,
      "parameterMatchers cannot be null"));
  }

  /**
   * Create a new method matcher builder.
   *
   * @return a new TypeBuilder instance
   */
  public static TypeBuilder create() {
    return new Builder();
  }

  /**
   * Combines multiple matchers with OR logic.
   *
   * @param matchers the matchers to combine
   * @return a matcher that matches if any of the provided matchers match
   */
  public static CxxMethodMatcher or(CxxMethodMatcher... matchers) {
    return or(Arrays.asList(matchers));
  }

  /**
   * Combines multiple matchers with OR logic.
   *
   * @param matchers the matchers to combine
   * @return a matcher that matches if any of the provided matchers match
   */
  public static CxxMethodMatcher or(List<CxxMethodMatcher> matchers) {
    List<CxxMethodMatcher> matcherList = Objects.requireNonNull(matchers, "matchers cannot be null");
    return new Builder()
      .ofType(type -> matcherList.stream().anyMatch(m -> m.typePredicate.test(type)))
      .name(name -> matcherList.stream().anyMatch(m -> m.namePredicate.test(name)))
      .addParametersMatcher(params -> matcherList.stream().anyMatch(m ->
        m.parameterMatchers.stream().anyMatch(pm -> pm.test(params))))
      .build();
  }

  /**
   * Checks if a function call node matches this matcher.
   *
   * @param callNode a postfixExpression node representing a function call
   * @return true if the call matches
   */
  public boolean matches(@CheckForNull AstNode callNode) {
    if (callNode == null || !CxxAstNodeHelper.isFunctionCall(callNode)) {
      return false;
    }

    String functionName = CxxAstNodeHelper.getFunctionCallName(callNode);
    if (functionName == null || !namePredicate.test(functionName)) {
      return false;
    }

    Symbol calleeSymbol = AstNodeSymbolExtension.getSymbol(callNode);
    if (calleeSymbol != null && calleeSymbol.isFunctionSymbol()) {
      Symbol.FunctionSymbol funcSymbol = (Symbol.FunctionSymbol) calleeSymbol;

      Type returnType = funcSymbol.returnType();
      if (!typePredicate.test(returnType)) {
        return false;
      }

      List<Type> paramTypes = funcSymbol.parameterTypes();
      return parameterMatchers.stream().anyMatch(pm -> pm.test(paramTypes));
    }

    return typePredicate.test(Type.UNKNOWN_TYPE)
      && parameterMatchers.stream().anyMatch(pm -> pm.test(List.of()));
  }

  /**
   * Checks if a function definition node matches this matcher.
   *
   * @param functionDefNode a functionDefinition node
   * @return true if the function definition matches
   */
  public boolean matchesDefinition(@CheckForNull AstNode functionDefNode) {
    if (functionDefNode == null || !functionDefNode.is(CxxGrammarImpl.functionDefinition)) {
      return false;
    }

    String functionName = CxxAstNodeHelper.getFunctionDefinitionName(functionDefNode);
    if (functionName == null || !namePredicate.test(functionName)) {
      return false;
    }

    Symbol funcSymbol = AstNodeSymbolExtension.getSymbol(functionDefNode);
    if (funcSymbol != null && funcSymbol.isFunctionSymbol()) {
      Symbol.FunctionSymbol fs = (Symbol.FunctionSymbol) funcSymbol;

      Type returnType = fs.returnType();
      if (!typePredicate.test(returnType)) {
        return false;
      }

      List<Type> paramTypes = fs.parameterTypes();
      return parameterMatchers.stream().anyMatch(pm -> pm.test(paramTypes));
    }

    return typePredicate.test(Type.UNKNOWN_TYPE)
      && parameterMatchers.stream().anyMatch(pm -> pm.test(List.of()));
  }

  /**
   * Checks if a symbol matches this matcher.
   *
   * @param symbol a function symbol
   * @return true if the symbol matches
   */
  public boolean matches(@CheckForNull Symbol symbol) {
    if (symbol == null || !symbol.isFunctionSymbol()) {
      return false;
    }

    Symbol.FunctionSymbol funcSymbol = (Symbol.FunctionSymbol) symbol;

    String name = funcSymbol.name();
    if (name == null || !namePredicate.test(name)) {
      return false;
    }

    Type returnType = funcSymbol.returnType();
    if (!typePredicate.test(returnType)) {
      return false;
    }

    List<Type> paramTypes = funcSymbol.parameterTypes();
    return parameterMatchers.stream().anyMatch(pm -> pm.test(paramTypes));
  }

  public interface TypeBuilder {
    /**
     * Match any of the type and sub-type of the fully qualified names.
     */
    NameBuilder ofSubTypes(String... fullyQualifiedTypeNames);

    /**
     * Match any type.
     */
    NameBuilder ofAnyType();

    /**
     * Match any of the fully qualified name types, but not the subtype.
     */
    NameBuilder ofTypes(String... fullyQualifiedTypeNames);

    /**
     * Match a type matching a predicate.
     */
    NameBuilder ofType(Predicate<Type> typePredicate);
  }

  public interface NameBuilder {
    /**
     * Match a function with any name in the list.
     */
    ParametersBuilder names(String... names);

    /**
     * Match a function with any name.
     */
    ParametersBuilder anyName();

    /**
     * Match a constructor.
     */
    ParametersBuilder constructor();

    /**
     * Match the name matching the predicate.
     */
    ParametersBuilder name(Predicate<String> namePredicate);
  }

  public interface ParametersBuilder {
    /**
     * Match a function signature with any number of parameters of any types.
     */
    ParametersBuilder withAnyParameters();

    /**
     * Match a function signature without parameters.
     */
    ParametersBuilder addWithoutParametersMatcher();

    /**
     * Match a function signature with exactly the types provided.
     */
    ParametersBuilder addParametersMatcher(String... parametersType);

    /**
     * Match a function signature respecting the predicate.
     */
    ParametersBuilder addParametersMatcher(Predicate<List<Type>> parametersType);

    /**
     * Build a CxxMethodMatcher.
     */
    CxxMethodMatcher build();
  }

  private static class Builder implements TypeBuilder, NameBuilder, ParametersBuilder {
    private Predicate<Type> typePredicate;
    private Predicate<String> namePredicate;
    private final List<Predicate<List<Type>>> parameterMatchers = new ArrayList<>();

    @Override
    public NameBuilder ofSubTypes(String... fullyQualifiedTypeNames) {
      this.typePredicate = type -> {
        if (type == null || type.isUnknown()) {
          return false;
        }
        for (String fqn : fullyQualifiedTypeNames) {
          if (type.isSubtypeOf(fqn)) {
            return true;
          }
        }
        return false;
      };
      return this;
    }

    @Override
    public NameBuilder ofAnyType() {
      this.typePredicate = type -> true;
      return this;
    }

    @Override
    public NameBuilder ofTypes(String... fullyQualifiedTypeNames) {
      this.typePredicate = type -> {
        if (type == null || type.isUnknown()) {
          return false;
        }
        for (String fqn : fullyQualifiedTypeNames) {
          if (type.is(fqn)) {
            return true;
          }
        }
        return false;
      };
      return this;
    }

    @Override
    public NameBuilder ofType(Predicate<Type> typePredicate) {
      this.typePredicate = Objects.requireNonNull(typePredicate, "typePredicate cannot be null");
      return this;
    }

    @Override
    public ParametersBuilder names(String... names) {
      Objects.requireNonNull(names, "names cannot be null");
      List<String> nameList = Arrays.asList(names);
      this.namePredicate = nameList::contains;
      return this;
    }

    @Override
    public ParametersBuilder anyName() {
      this.namePredicate = name -> true;
      return this;
    }

    @Override
    public ParametersBuilder constructor() {
      this.namePredicate = name -> name != null && (name.contains("::")
        ? name.substring(name.lastIndexOf("::") + 2).equals(name.substring(0, name.indexOf("::")))
        : false);
      return this;
    }

    @Override
    public ParametersBuilder name(Predicate<String> namePredicate) {
      this.namePredicate = Objects.requireNonNull(namePredicate, "namePredicate cannot be null");
      return this;
    }

    @Override
    public ParametersBuilder withAnyParameters() {
      this.parameterMatchers.add(params -> true);
      return this;
    }

    @Override
    public ParametersBuilder addWithoutParametersMatcher() {
      this.parameterMatchers.add(List::isEmpty);
      return this;
    }

    @Override
    public ParametersBuilder addParametersMatcher(String... parametersType) {
      Objects.requireNonNull(parametersType, "parametersType cannot be null");
      List<String> expectedParams = Arrays.asList(parametersType);
      this.parameterMatchers.add(actualParams -> {
        if (actualParams.size() != expectedParams.size()) {
          return false;
        }
        for (int i = 0; i < expectedParams.size(); i++) {
          String expected = expectedParams.get(i);
          Type actual = actualParams.get(i);

          if ("*".equals(expected)) {
            continue;
          }

          if (actual == null || actual.isUnknown()) {
            return false;
          }

          if (!actual.is(expected)) {
            return false;
          }
        }
        return true;
      });
      return this;
    }

    @Override
    public ParametersBuilder addParametersMatcher(Predicate<List<Type>> parametersType) {
      this.parameterMatchers.add(Objects.requireNonNull(parametersType,
        "parametersType predicate cannot be null"));
      return this;
    }

    @Override
    public CxxMethodMatcher build() {
      if (typePredicate == null) {
        throw new IllegalStateException("Type predicate must be defined");
      }
      if (namePredicate == null) {
        throw new IllegalStateException("Name predicate must be defined");
      }
      if (parameterMatchers.isEmpty()) {
        throw new IllegalStateException("At least one parameter matcher must be defined");
      }
      return new CxxMethodMatcher(typePredicate, namePredicate, parameterMatchers);
    }
  }
}
