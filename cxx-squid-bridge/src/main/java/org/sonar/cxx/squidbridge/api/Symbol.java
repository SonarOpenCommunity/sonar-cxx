/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2025 SonarOpenCommunity
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
package org.sonar.cxx.squidbridge.api;

import com.sonar.cxx.sslr.api.AstNode;
import java.util.Collection;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

/**
 * Interface to access symbol information for C/C++ code.
 *
 * <p>This abstraction provides semantic information about symbols (variables, functions, classes, etc.)
 * in the analyzed C/C++ code.
 *
 * <p>The Symbol interface bridges the gap between the syntactic AST representation (AstNode) and
 * the semantic model (SourceCode hierarchy), providing a first-class abstraction for symbol
 * information that can be used by detection rules and code analysis.
 */
public interface Symbol {

  /**
   * An instance of {@link Symbol} representing an unknown symbol.
   */
  Symbol UNKNOWN_SYMBOL = new UnknownSymbol();

  /**
   * Name of this symbol.
   *
   * @return simple name of the symbol (e.g., "myVariable", "MyClass", "myFunction")
   */
  String name();

  /**
   * The owner of this symbol.
   *
   * @return the symbol that owns this symbol, null for global symbols or unknown symbols
   */
  @Nullable
  Symbol owner();

  /**
   * Fully qualified name of this symbol.
   *
   * <p>For C++, this includes the namespace and class qualifiers.
   * For example: "std::vector", "MyNamespace::MyClass::method"
   *
   * @return fully qualified name, or null if not available
   */
  @CheckForNull
  String fullyQualifiedName();

  /**
   * Kind of symbol.
   *
   * @return the kind of this symbol (e.g., VARIABLE, FUNCTION, CLASS)
   */
  Kind kind();

  /**
   * Check if this symbol matches any of the specified kinds.
   *
   * @param kinds the kinds to check against
   * @return true if this symbol's kind matches any of the specified kinds
   */
  boolean is(Kind... kinds);

  /**
   * @return true if this symbol represents a variable (local variable, parameter, field, etc.)
   */
  boolean isVariableSymbol();

  /**
   * @return true if this symbol represents a type (class, struct, union, enum, typedef)
   */
  boolean isTypeSymbol();

  /**
   * @return true if this symbol represents a function or method
   */
  boolean isFunctionSymbol();

  /**
   * @return true if this symbol represents a namespace
   */
  boolean isNamespaceSymbol();

  /**
   * @return true if this symbol is unknown (no semantic information available)
   */
  boolean isUnknown();

  /**
   * @return true if this symbol is static (internal linkage in C/C++)
   */
  boolean isStatic();

  /**
   * @return true if this symbol is const
   */
  boolean isConst();

  /**
   * @return true if this symbol is volatile
   */
  boolean isVolatile();

  /**
   * @return true if this symbol has public visibility (for class members)
   */
  boolean isPublic();

  /**
   * @return true if this symbol has private visibility (for class members)
   */
  boolean isPrivate();

  /**
   * @return true if this symbol has protected visibility (for class members)
   */
  boolean isProtected();

  /**
   * The closest enclosing class or struct.
   *
   * @return null for namespace-level symbols, the enclosing TypeSymbol for members
   */
  @Nullable
  TypeSymbol enclosingClass();

  /**
   * The usages of this symbol (references in the code).
   *
   * @return a list of Usage objects representing where this symbol is referenced.
   *         An empty list if this symbol is unused.
   */
  List<Usage> usages();

  /**
   * Declaration node of this symbol.
   *
   * @return the AstNode of the declaration of this symbol, or null if not available
   */
  @Nullable
  AstNode declaration();

  /**
   * Corresponding SourceCode entity for this symbol (if any).
   *
   * <p>This bridges to the existing sonar-cxx semantic model.
   *
   * @return the SourceCode object representing this symbol, or null if not available
   */
  @Nullable
  SourceCode sourceCode();

  /**
   * Enumeration of symbol kinds for C/C++.
   */
  enum Kind {
    /** Variable (local variable, global variable, field) */
    VARIABLE,
    /** Function or method */
    FUNCTION,
    /** Type (class, struct, union, enum, typedef) */
    TYPE,
    /** Namespace */
    NAMESPACE,
    /** Enum constant */
    ENUM_CONSTANT,
    /** Template parameter */
    TEMPLATE_PARAMETER,
    /** Unknown or ambiguous symbol */
    UNKNOWN
  }

  /**
   * Symbol for a type: class, struct, union, enum, or typedef.
   */
  interface TypeSymbol extends Symbol {

    /**
     * An instance of {@link TypeSymbol} representing an unknown type symbol.
     */
    TypeSymbol UNKNOWN_TYPE = new UnknownTypeSymbol();

    /**
     * Returns the base classes of this type symbol (for classes/structs).
     *
     * @return list of base class symbols, empty list if no base classes
     */
    List<TypeSymbol> baseClasses();

    /**
     * List of symbols defined by this type (member variables, methods, nested types).
     *
     * <p>This will not return any inherited symbols.
     *
     * @return the collection of symbols defined by this type
     */
    Collection<Symbol> memberSymbols();

    /**
     * Lookup symbols accessible from this type with the name passed in parameter.
     *
     * @param name name of searched symbol
     * @return a collection of symbols matching the looked up name
     */
    Collection<Symbol> lookupSymbols(String name);

    /**
     * @return true if this type is a class
     */
    boolean isClass();

    /**
     * @return true if this type is a struct
     */
    boolean isStruct();

    /**
     * @return true if this type is a union
     */
    boolean isUnion();

    /**
     * @return true if this type is an enum
     */
    boolean isEnum();

    /**
     * @return true if this type is a typedef
     */
    boolean isTypedef();

    /**
     * @return true if this is a template class/struct
     */
    boolean isTemplate();
  }

  /**
   * Symbol for variables: local variables, parameters, fields, global variables.
   */
  interface VariableSymbol extends Symbol {

    /**
     * @return true if this variable is a local variable of a function
     */
    boolean isLocalVariable();

    /**
     * @return true if this variable is a parameter of a function
     */
    boolean isParameter();

    /**
     * @return true if this variable is a field (member variable)
     */
    boolean isField();

    /**
     * @return true if this variable is a global variable
     */
    boolean isGlobalVariable();
  }

  /**
   * Symbol for functions and methods.
   */
  interface FunctionSymbol extends Symbol {

    /**
     * Instance of {@link FunctionSymbol} representing an unknown function symbol.
     */
    FunctionSymbol UNKNOWN_FUNCTION = new UnknownFunctionSymbol();

    /**
     * Symbols of parameters declared by this function.
     *
     * @return list of parameter symbols, empty list if function has no parameters
     */
    List<VariableSymbol> parameters();

    /**
     * The return type of this function.
     *
     * <p>This provides type information for the function's return value,
     * enabling type-based matching in detection rules. Returns
     * {@link Type#UNKNOWN_TYPE} if the return type cannot be determined.
     *
     * @return the return type, never null
     */
    Type returnType();

    /**
     * The types of this function's parameters.
     *
     * <p>This is a convenience method equivalent to extracting types from
     * each parameter symbol. Useful for matching function signatures in
     * detection rules.
     *
     * @return list of parameter types in declaration order, empty list if no parameters
     */
    List<Type> parameterTypes();

    /**
     * @return true if this is a virtual function
     */
    boolean isVirtual();

    /**
     * @return true if this is a pure virtual function
     */
    boolean isPureVirtual();

    /**
     * @return true if this function is inline
     */
    boolean isInline();

    /**
     * @return true if this function is constexpr
     */
    boolean isConstexpr();

    /**
     * @return true if this is a constructor
     */
    boolean isConstructor();

    /**
     * @return true if this is a destructor
     */
    boolean isDestructor();

    /**
     * @return true if this is an operator overload
     */
    boolean isOperator();

    /**
     * @return true if this is a template function
     */
    boolean isTemplate();
  }

  /**
   * Represents a usage (reference) of a symbol in the code.
   */
  interface Usage {
    /**
     * The AST node representing this usage.
     *
     * @return the AstNode where the symbol is referenced
     */
    AstNode node();

    /**
     * The symbol being used.
     *
     * @return the symbol that is referenced
     */
    Symbol symbol();

    /**
     * Kind of usage.
     *
     * @return the kind of this usage
     */
    UsageKind kind();

    /**
     * Enumeration of usage kinds.
     */
    enum UsageKind {
      /** Symbol is being read/accessed */
      READ,
      /** Symbol is being written/modified */
      WRITE,
      /** Symbol is being both read and written (e.g., +=) */
      READ_WRITE,
      /** Symbol is being declared */
      DECLARATION,
      /** Other/unknown usage */
      OTHER
    }
  }

  /**
   * Default implementation for unknown symbols.
   */
  class UnknownSymbol implements Symbol {
    @Override public String name() { return "<unknown>"; }
    @Override public Symbol owner() { return null; }
    @Override public String fullyQualifiedName() { return null; }
    @Override public Kind kind() { return Kind.UNKNOWN; }
    @Override public boolean is(Kind... kinds) {
      for (Kind k : kinds) {
        if (k == Kind.UNKNOWN) return true;
      }
      return false;
    }
    @Override public boolean isVariableSymbol() { return false; }
    @Override public boolean isTypeSymbol() { return false; }
    @Override public boolean isFunctionSymbol() { return false; }
    @Override public boolean isNamespaceSymbol() { return false; }
    @Override public boolean isUnknown() { return true; }
    @Override public boolean isStatic() { return false; }
    @Override public boolean isConst() { return false; }
    @Override public boolean isVolatile() { return false; }
    @Override public boolean isPublic() { return false; }
    @Override public boolean isPrivate() { return false; }
    @Override public boolean isProtected() { return false; }
    @Override public TypeSymbol enclosingClass() { return null; }
    @Override public List<Usage> usages() { return List.of(); }
    @Override public AstNode declaration() { return null; }
    @Override public SourceCode sourceCode() { return null; }
  }

  /**
   * Default implementation for unknown type symbols.
   */
  class UnknownTypeSymbol extends UnknownSymbol implements TypeSymbol {
    @Override public List<TypeSymbol> baseClasses() { return List.of(); }
    @Override public Collection<Symbol> memberSymbols() { return List.of(); }
    @Override public Collection<Symbol> lookupSymbols(String name) { return List.of(); }
    @Override public boolean isClass() { return false; }
    @Override public boolean isStruct() { return false; }
    @Override public boolean isUnion() { return false; }
    @Override public boolean isEnum() { return false; }
    @Override public boolean isTypedef() { return false; }
    @Override public boolean isTemplate() { return false; }
  }

  /**
   * Default implementation for unknown function symbols.
   */
  class UnknownFunctionSymbol extends UnknownSymbol implements FunctionSymbol {
    @Override public List<VariableSymbol> parameters() { return List.of(); }
    @Override public Type returnType() { return Type.UNKNOWN_TYPE; }
    @Override public List<Type> parameterTypes() { return List.of(); }
    @Override public boolean isVirtual() { return false; }
    @Override public boolean isPureVirtual() { return false; }
    @Override public boolean isInline() { return false; }
    @Override public boolean isConstexpr() { return false; }
    @Override public boolean isConstructor() { return false; }
    @Override public boolean isDestructor() { return false; }
    @Override public boolean isOperator() { return false; }
    @Override public boolean isTemplate() { return false; }
  }
}
