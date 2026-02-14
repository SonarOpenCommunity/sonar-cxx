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

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

/**
 * Interface representing the type of an expression or symbol in C/C++ code.
 *
 * <p>This abstraction provides type information for expressions, variables, and function
 * return values, enabling type-based matching for detection rules.
 *
 * <p>For C++, type resolution is based on fully qualified names (FQN) of symbols.
 * The initial implementation uses exact FQN string matching via {@link #is(String)}.
 * Subtype checking via {@link #isSubtypeOf(String)} is supported but initially falls
 * back to exact matching, which can be enhanced incrementally as the semantic analysis
 * engine matures.
 *
 * <p>Usage example:
 * <pre>
 * Type type = AstNodeTypeExtension.getType(expressionNode);
 * if (type != null &amp;&amp; type.is("std::string")) {
 *     // Handle std::string type
 * }
 * </pre>
 */
public interface Type {

  /**
   * An instance representing an unknown type.
   */
  Type UNKNOWN_TYPE = new UnknownType();

  /**
   * The fully qualified name of this type.
   *
   * <p>For C++, this includes namespace and class qualifiers.
   * Examples: "int", "std::string", "std::vector", "MyNamespace::MyClass"
   *
   * @return the fully qualified type name, or null if unknown
   */
  @CheckForNull
  String fullyQualifiedName();

  /**
   * Check if this type matches the given fully qualified name.
   *
   * <p>This performs an exact string comparison against the fully qualified
   * name of the type. For C++, this is the primary type-matching strategy.
   *
   * @param fullyQualifiedName the fully qualified name to compare against
   * @return true if this type's FQN matches the given name
   */
  boolean is(String fullyQualifiedName);

  /**
   * Check if this type is a subtype of the type identified by the given fully qualified name.
   *
   * <p>For C++ classes/structs, this checks the inheritance hierarchy (base classes).
   * In the initial implementation, this falls back to exact matching ({@link #is(String)}).
   * As the semantic analysis engine matures, full subtype checking can be implemented.
   *
   * @param fullyQualifiedName the fully qualified name of the potential supertype
   * @return true if this type is a subtype of the given type
   */
  boolean isSubtypeOf(String fullyQualifiedName);

  /**
   * @return true if this type is unknown (no type information available)
   */
  boolean isUnknown();

  /**
   * @return true if this type represents an array type
   */
  boolean isArray();

  /**
   * @return true if this type represents a primitive type (int, char, bool, etc.)
   */
  boolean isPrimitive();

  /**
   * @return true if this type represents a class or struct type
   */
  boolean isClass();

  /**
   * @return true if this type represents a pointer type
   */
  boolean isPointer();

  /**
   * @return true if this type represents a reference type
   */
  boolean isReference();

  /**
   * Get the associated TypeSymbol for this type, if available.
   *
   * @return the TypeSymbol, or null if this type has no associated symbol
   */
  @CheckForNull
  Symbol.TypeSymbol symbol();

  /**
   * Default implementation for FQN-based type comparison.
   *
   * <p>This implementation stores a fully qualified name string and performs
   * exact string matching. It is the standard Type implementation for C++.
   */
  class CxxType implements Type {

    private final String fqn;
    private final boolean primitive;
    private final boolean array;
    private final boolean classType;
    private final boolean pointer;
    private final boolean reference;
    private final Symbol.TypeSymbol typeSymbol;

    /**
     * Create a type from a fully qualified name.
     *
     * @param fullyQualifiedName the FQN of the type
     */
    public CxxType(String fullyQualifiedName) {
      this(fullyQualifiedName, false, false, false, false, false, null);
    }

    /**
     * Create a type from a fully qualified name with a TypeSymbol.
     *
     * @param fullyQualifiedName the FQN of the type
     * @param typeSymbol the associated TypeSymbol
     */
    public CxxType(String fullyQualifiedName, @Nullable Symbol.TypeSymbol typeSymbol) {
      this(fullyQualifiedName, false, false, false, false, false, typeSymbol);
    }

    /**
     * Create a type with full metadata.
     */
    public CxxType(String fullyQualifiedName, boolean primitive, boolean array,
                   boolean classType, boolean pointer, boolean reference,
                   @Nullable Symbol.TypeSymbol typeSymbol) {
      this.fqn = fullyQualifiedName;
      this.primitive = primitive;
      this.array = array;
      this.classType = classType;
      this.pointer = pointer;
      this.reference = reference;
      this.typeSymbol = typeSymbol;
    }

    @Override
    public String fullyQualifiedName() {
      return fqn;
    }

    @Override
    public boolean is(String fullyQualifiedName) {
      if (fqn == null || fullyQualifiedName == null) {
        return false;
      }
      return fqn.equals(fullyQualifiedName);
    }

    @Override
    public boolean isSubtypeOf(String fullyQualifiedName) {
      // Initial implementation: exact match
      // Can be enhanced with inheritance checking when TypeSymbol.baseClasses() is populated
      if (is(fullyQualifiedName)) {
        return true;
      }
      if (typeSymbol != null) {
        for (Symbol.TypeSymbol base : typeSymbol.baseClasses()) {
          String baseFqn = base.fullyQualifiedName();
          if (baseFqn != null && baseFqn.equals(fullyQualifiedName)) {
            return true;
          }
        }
      }
      return false;
    }

    @Override
    public boolean isUnknown() {
      return false;
    }

    @Override
    public boolean isArray() {
      return array;
    }

    @Override
    public boolean isPrimitive() {
      return primitive;
    }

    @Override
    public boolean isClass() {
      return classType;
    }

    @Override
    public boolean isPointer() {
      return pointer;
    }

    @Override
    public boolean isReference() {
      return reference;
    }

    @Override
    public Symbol.TypeSymbol symbol() {
      return typeSymbol;
    }

    @Override
    public String toString() {
      return fqn != null ? fqn : "<unknown>";
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj instanceof CxxType other) {
        if (fqn == null) {
          return other.fqn == null;
        }
        return fqn.equals(other.fqn);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return fqn != null ? fqn.hashCode() : 0;
    }
  }

  /**
   * Factory methods for creating common Type instances.
   */
  class Types {

    private Types() {
    }

    /**
     * Create a type from a fully qualified name string.
     *
     * @param fullyQualifiedName the FQN of the type
     * @return a new Type instance
     */
    public static Type of(String fullyQualifiedName) {
      if (fullyQualifiedName == null) {
        return UNKNOWN_TYPE;
      }
      return new CxxType(fullyQualifiedName);
    }

    /**
     * Create a type from a Symbol.
     *
     * @param symbol the symbol to derive the type from
     * @return a Type based on the symbol's FQN
     */
    public static Type fromSymbol(@Nullable Symbol symbol) {
      if (symbol == null || symbol.isUnknown()) {
        return UNKNOWN_TYPE;
      }
      String fqn = symbol.fullyQualifiedName();
      if (fqn == null) {
        fqn = symbol.name();
      }
      Symbol.TypeSymbol typeSymbol = symbol.isTypeSymbol() && symbol instanceof Symbol.TypeSymbol ts
        ? ts : null;
      return new CxxType(fqn, typeSymbol);
    }

    /**
     * Create a primitive type.
     *
     * @param name the primitive type name (e.g., "int", "char", "bool")
     * @return a Type representing the primitive
     */
    public static Type primitive(String name) {
      return new CxxType(name, true, false, false, false, false, null);
    }

    /**
     * Create a class/struct type.
     *
     * @param fullyQualifiedName the FQN of the class/struct
     * @return a Type representing the class
     */
    public static Type classType(String fullyQualifiedName) {
      return new CxxType(fullyQualifiedName, false, false, true, false, false, null);
    }

    /**
     * Create a class/struct type with a TypeSymbol.
     *
     * @param fullyQualifiedName the FQN of the class/struct
     * @param typeSymbol the associated TypeSymbol
     * @return a Type representing the class
     */
    public static Type classType(String fullyQualifiedName, Symbol.TypeSymbol typeSymbol) {
      return new CxxType(fullyQualifiedName, false, false, true, false, false, typeSymbol);
    }
  }

  /**
   * Default implementation for unknown types.
   */
  class UnknownType implements Type {
    @Override public String fullyQualifiedName() { return null; }
    @Override public boolean is(String fullyQualifiedName) { return false; }
    @Override public boolean isSubtypeOf(String fullyQualifiedName) { return false; }
    @Override public boolean isUnknown() { return true; }
    @Override public boolean isArray() { return false; }
    @Override public boolean isPrimitive() { return false; }
    @Override public boolean isClass() { return false; }
    @Override public boolean isPointer() { return false; }
    @Override public boolean isReference() { return false; }
    @Override public Symbol.TypeSymbol symbol() { return null; }
    @Override public String toString() { return "<unknown>"; }
  }
}
