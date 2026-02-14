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

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TypeTest {

  @Test
  void testUnknownType() {
    var type = Type.UNKNOWN_TYPE;
    assertThat(type.fullyQualifiedName()).isNull();
    assertThat(type.isUnknown()).isTrue();
    assertThat(type.is("anything")).isFalse();
    assertThat(type.isSubtypeOf("anything")).isFalse();
    assertThat(type.isArray()).isFalse();
    assertThat(type.isPrimitive()).isFalse();
    assertThat(type.isClass()).isFalse();
    assertThat(type.isPointer()).isFalse();
    assertThat(type.isReference()).isFalse();
    assertThat(type.symbol()).isNull();
    assertThat(type.toString()).isEqualTo("<unknown>");
  }

  @Test
  void testCxxTypeFromFqn() {
    var type = new Type.CxxType("std::string");
    assertThat(type.fullyQualifiedName()).isEqualTo("std::string");
    assertThat(type.is("std::string")).isTrue();
    assertThat(type.is("std::vector")).isFalse();
    assertThat(type.isUnknown()).isFalse();
    assertThat(type.isSubtypeOf("std::string")).isTrue();
    assertThat(type.isSubtypeOf("std::vector")).isFalse();
  }

  @Test
  void testCxxTypeIsWithNull() {
    var type = new Type.CxxType("int");
    assertThat(type.is(null)).isFalse();

    var nullType = new Type.CxxType(null);
    assertThat(nullType.is("int")).isFalse();
    assertThat(nullType.is(null)).isFalse();
  }

  @Test
  void testCxxTypePrimitive() {
    var type = new Type.CxxType("int", true, false, false, false, false, null);
    assertThat(type.isPrimitive()).isTrue();
    assertThat(type.isClass()).isFalse();
    assertThat(type.isArray()).isFalse();
    assertThat(type.isPointer()).isFalse();
    assertThat(type.isReference()).isFalse();
  }

  @Test
  void testCxxTypeClass() {
    var type = new Type.CxxType("MyClass", false, false, true, false, false, null);
    assertThat(type.isClass()).isTrue();
    assertThat(type.isPrimitive()).isFalse();
  }

  @Test
  void testCxxTypePointer() {
    var type = new Type.CxxType("int*", false, false, false, true, false, null);
    assertThat(type.isPointer()).isTrue();
    assertThat(type.isReference()).isFalse();
  }

  @Test
  void testCxxTypeReference() {
    var type = new Type.CxxType("int&", false, false, false, false, true, null);
    assertThat(type.isReference()).isTrue();
    assertThat(type.isPointer()).isFalse();
  }

  @Test
  void testCxxTypeArray() {
    var type = new Type.CxxType("int[]", false, true, false, false, false, null);
    assertThat(type.isArray()).isTrue();
  }

  @Test
  void testCxxTypeWithTypeSymbol() {
    var typeSymbol = Symbol.TypeSymbol.UNKNOWN_TYPE;
    var type = new Type.CxxType("MyClass", typeSymbol);
    assertThat(type.symbol()).isEqualTo(typeSymbol);
  }

  @Test
  void testCxxTypeEquality() {
    var type1 = new Type.CxxType("std::string");
    var type2 = new Type.CxxType("std::string");
    var type3 = new Type.CxxType("std::vector");

    assertThat(type1).isEqualTo(type2);
    assertThat(type1).isNotEqualTo(type3);
    assertThat(type1.hashCode()).isEqualTo(type2.hashCode());
  }

  @Test
  void testCxxTypeEqualityWithNull() {
    var type1 = new Type.CxxType(null);
    var type2 = new Type.CxxType(null);
    var type3 = new Type.CxxType("int");

    assertThat(type1).isEqualTo(type2);
    assertThat(type1).isNotEqualTo(type3);
  }

  @Test
  void testCxxTypeEqualityWithSelf() {
    var type = new Type.CxxType("int");
    assertThat(type).isEqualTo(type);
  }

  @Test
  void testCxxTypeEqualityWithOtherType() {
    var type = new Type.CxxType("int");
    assertThat(type).isNotEqualTo("int");
  }

  @Test
  void testCxxTypeToString() {
    assertThat(new Type.CxxType("std::string").toString()).isEqualTo("std::string");
    assertThat(new Type.CxxType(null).toString()).isEqualTo("<unknown>");
  }

  @Test
  void testTypesOf() {
    var type = Type.Types.of("std::string");
    assertThat(type.is("std::string")).isTrue();
    assertThat(type.isUnknown()).isFalse();
  }

  @Test
  void testTypesOfNull() {
    var type = Type.Types.of(null);
    assertThat(type.isUnknown()).isTrue();
    assertThat(type).isSameAs(Type.UNKNOWN_TYPE);
  }

  @Test
  void testTypesFromSymbol() {
    var symbol = new SourceCodeSymbol("myVar", Symbol.Kind.VARIABLE, null);
    var type = Type.Types.fromSymbol(symbol);
    assertThat(type.fullyQualifiedName()).isEqualTo("myVar");
    assertThat(type.isUnknown()).isFalse();
  }

  @Test
  void testTypesFromNullSymbol() {
    assertThat(Type.Types.fromSymbol(null)).isSameAs(Type.UNKNOWN_TYPE);
  }

  @Test
  void testTypesFromUnknownSymbol() {
    assertThat(Type.Types.fromSymbol(Symbol.UNKNOWN_SYMBOL)).isSameAs(Type.UNKNOWN_TYPE);
  }

  @Test
  void testTypesFromTypeSymbol() {
    var typeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol("MyClass", null);
    var type = Type.Types.fromSymbol(typeSymbol);
    assertThat(type.fullyQualifiedName()).isEqualTo("MyClass");
    assertThat(type.symbol()).isEqualTo(typeSymbol);
  }

  @Test
  void testTypesPrimitive() {
    var type = Type.Types.primitive("int");
    assertThat(type.is("int")).isTrue();
    assertThat(type.isPrimitive()).isTrue();
    assertThat(type.isClass()).isFalse();
  }

  @Test
  void testTypesClassType() {
    var type = Type.Types.classType("std::string");
    assertThat(type.is("std::string")).isTrue();
    assertThat(type.isClass()).isTrue();
    assertThat(type.isPrimitive()).isFalse();
  }

  @Test
  void testTypesClassTypeWithSymbol() {
    var typeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol("MyClass", null);
    var type = Type.Types.classType("MyClass", typeSymbol);
    assertThat(type.is("MyClass")).isTrue();
    assertThat(type.isClass()).isTrue();
    assertThat(type.symbol()).isEqualTo(typeSymbol);
  }

  @Test
  void testIsSubtypeOfWithBaseClasses() {
    // CxxType.isSubtypeOf checks baseClasses when a TypeSymbol is available
    // Since SourceCodeTypeSymbol.baseClasses() returns empty by default,
    // isSubtypeOf falls back to exact match
    var typeSymbol = new SourceCodeSymbol.SourceCodeTypeSymbol("DerivedClass", null);
    var type = new Type.CxxType("DerivedClass", false, false, true, false, false, typeSymbol);
    assertThat(type.isSubtypeOf("DerivedClass")).isTrue();
    assertThat(type.isSubtypeOf("BaseClass")).isFalse();
  }
}
