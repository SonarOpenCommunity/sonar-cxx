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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

/**
 * Base implementation of Symbol that bridges to the existing SourceCode hierarchy.
 *
 * <p>This implementation wraps a SourceCode object and provides Symbol interface access
 * to its semantic information, creating a first-class abstraction while maintaining
 * backward compatibility with existing sonar-cxx infrastructure.
 */
public class SourceCodeSymbol implements Symbol {

  protected final SourceCode sourceCode;
  protected final String name;
  protected final Kind kind;
  protected final List<Usage> usagesList;
  protected AstNode declarationNode;
  protected Symbol ownerSymbol;

  /**
   * Creates a new SourceCodeSymbol.
   *
   * @param sourceCode the SourceCode object this symbol represents
   * @param kind the kind of symbol
   */
  public SourceCodeSymbol(SourceCode sourceCode, Kind kind) {
    this.sourceCode = sourceCode;
    this.name = sourceCode != null ? sourceCode.getName() : "<unknown>";
    this.kind = kind;
    this.usagesList = new ArrayList<>();
    this.declarationNode = null;
    this.ownerSymbol = null;
  }

  /**
   * Creates a new SourceCodeSymbol with explicit name.
   *
   * @param name the name of the symbol
   * @param kind the kind of symbol
   * @param sourceCode optional SourceCode object (may be null)
   */
  public SourceCodeSymbol(String name, Kind kind, @Nullable SourceCode sourceCode) {
    this.name = name;
    this.kind = kind;
    this.sourceCode = sourceCode;
    this.usagesList = new ArrayList<>();
    this.declarationNode = null;
    this.ownerSymbol = null;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  @Nullable
  public Symbol owner() {
    if (ownerSymbol != null) {
      return ownerSymbol;
    }
    if (sourceCode != null && sourceCode.getParent() != null) {
      SourceCode parent = sourceCode.getParent();
      Kind parentKind = deriveKindFromSourceCode(parent);
      return new SourceCodeSymbol(parent, parentKind);
    }
    return null;
  }

  /**
   * Sets the owner of this symbol.
   *
   * @param owner the owner symbol
   */
  public void setOwner(Symbol owner) {
    this.ownerSymbol = owner;
  }

  @Override
  @CheckForNull
  public String fullyQualifiedName() {
    if (sourceCode != null) {
      return sourceCode.getKey();
    }
    return buildQualifiedName();
  }

  private String buildQualifiedName() {
    Symbol owner = owner();
    if (owner != null && !owner.isUnknown()) {
      String ownerName = owner.fullyQualifiedName();
      if (ownerName != null && !ownerName.isEmpty()) {
        return ownerName + "::" + name;
      }
    }
    return name;
  }

  @Override
  public Kind kind() {
    return kind;
  }

  @Override
  public boolean is(Kind... kinds) {
    for (Kind k : kinds) {
      if (k == this.kind) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isVariableSymbol() {
    return kind == Kind.VARIABLE;
  }

  @Override
  public boolean isTypeSymbol() {
    return kind == Kind.TYPE;
  }

  @Override
  public boolean isFunctionSymbol() {
    return kind == Kind.FUNCTION;
  }

  @Override
  public boolean isNamespaceSymbol() {
    return kind == Kind.NAMESPACE;
  }

  @Override
  public boolean isUnknown() {
    return kind == Kind.UNKNOWN;
  }

  @Override
  public boolean isStatic() {
    return false;
  }

  @Override
  public boolean isConst() {
    return false;
  }

  @Override
  public boolean isVolatile() {
    return false;
  }

  @Override
  public boolean isPublic() {
    return false;
  }

  @Override
  public boolean isPrivate() {
    return false;
  }

  @Override
  public boolean isProtected() {
    return false;
  }

  @Override
  @Nullable
  public TypeSymbol enclosingClass() {
    if (sourceCode != null) {
      SourceCode parent = sourceCode.getParent(SourceClass.class);
      if (parent != null) {
        return new SourceCodeTypeSymbol((SourceClass) parent);
      }
    }
    return null;
  }

  @Override
  public List<Usage> usages() {
    return new ArrayList<>(usagesList);
  }

  /**
   * Adds a usage of this symbol.
   *
   * @param usage the usage to add
   */
  public void addUsage(Usage usage) {
    usagesList.add(usage);
  }

  @Override
  @Nullable
  public AstNode declaration() {
    return declarationNode;
  }

  /**
   * Sets the declaration node for this symbol.
   *
   * @param node the AST node representing the declaration
   */
  public void setDeclaration(AstNode node) {
    this.declarationNode = node;
  }

  @Override
  @Nullable
  public SourceCode sourceCode() {
    return sourceCode;
  }

  /**
   * Derives the symbol kind from a SourceCode object.
   *
   * @param sc the SourceCode object
   * @return the corresponding symbol kind
   */
  protected static Kind deriveKindFromSourceCode(SourceCode sc) {
    if (sc instanceof SourceClass) {
      return Kind.TYPE;
    } else if (sc instanceof SourceFunction) {
      return Kind.FUNCTION;
    }
    return Kind.UNKNOWN;
  }

  /**
   * Implementation of Usage interface.
   */
  public static class SourceCodeUsage implements Usage {
    private final AstNode node;
    private final Symbol symbol;
    private final UsageKind usageKind;

    public SourceCodeUsage(AstNode node, Symbol symbol, UsageKind usageKind) {
      this.node = node;
      this.symbol = symbol;
      this.usageKind = usageKind;
    }

    @Override
    public AstNode node() {
      return node;
    }

    @Override
    public Symbol symbol() {
      return symbol;
    }

    @Override
    public UsageKind kind() {
      return usageKind;
    }
  }

  /**
   * TypeSymbol implementation bridging to SourceClass.
   */
  public static class SourceCodeTypeSymbol extends SourceCodeSymbol implements TypeSymbol {

    public SourceCodeTypeSymbol(SourceClass sourceClass) {
      super(sourceClass, Kind.TYPE);
    }

    public SourceCodeTypeSymbol(String name, @Nullable SourceCode sourceCode) {
      super(name, Kind.TYPE, sourceCode);
    }

    @Override
    public boolean isTypeSymbol() {
      return true;
    }

    @Override
    public List<TypeSymbol> baseClasses() {
      return List.of();
    }

    @Override
    public Collection<Symbol> memberSymbols() {
      if (sourceCode != null && sourceCode.hasChildren()) {
        return sourceCode.getChildren().stream()
          .map(child -> {
            Kind childKind = deriveKindFromSourceCode(child);
            return (Symbol) new SourceCodeSymbol(child, childKind);
          })
          .collect(Collectors.toList());
      }
      return List.of();
    }

    @Override
    public Collection<Symbol> lookupSymbols(String symbolName) {
      return memberSymbols().stream()
        .filter(s -> symbolName.equals(s.name()))
        .collect(Collectors.toList());
    }

    @Override
    public boolean isClass() {
      return sourceCode instanceof SourceClass;
    }

    @Override
    public boolean isStruct() {
      return false;
    }

    @Override
    public boolean isUnion() {
      return false;
    }

    @Override
    public boolean isEnum() {
      return false;
    }

    @Override
    public boolean isTypedef() {
      return false;
    }

    @Override
    public boolean isTemplate() {
      return false;
    }
  }

  /**
   * VariableSymbol implementation.
   */
  public static class SourceCodeVariableSymbol extends SourceCodeSymbol implements VariableSymbol {

    private boolean isParameter;
    private boolean isField;
    private boolean isLocal;
    private boolean isGlobal;

    public SourceCodeVariableSymbol(String name, @Nullable SourceCode sourceCode) {
      super(name, Kind.VARIABLE, sourceCode);
      this.isParameter = false;
      this.isField = false;
      this.isLocal = false;
      this.isGlobal = false;
    }

    @Override
    public boolean isVariableSymbol() {
      return true;
    }

    @Override
    public boolean isLocalVariable() {
      return isLocal;
    }

    public void setLocalVariable(boolean local) {
      this.isLocal = local;
    }

    @Override
    public boolean isParameter() {
      return isParameter;
    }

    public void setParameter(boolean parameter) {
      this.isParameter = parameter;
    }

    @Override
    public boolean isField() {
      return isField;
    }

    public void setField(boolean field) {
      this.isField = field;
    }

    @Override
    public boolean isGlobalVariable() {
      return isGlobal;
    }

    public void setGlobalVariable(boolean global) {
      this.isGlobal = global;
    }
  }

  /**
   * FunctionSymbol implementation bridging to SourceFunction.
   */
  public static class SourceCodeFunctionSymbol extends SourceCodeSymbol implements FunctionSymbol {

    private final List<VariableSymbol> parameterList;
    private Type returnTypeValue = Type.UNKNOWN_TYPE;

    public SourceCodeFunctionSymbol(SourceFunction sourceFunction) {
      super(sourceFunction, Kind.FUNCTION);
      this.parameterList = new ArrayList<>();
    }

    public SourceCodeFunctionSymbol(String name, @Nullable SourceCode sourceCode) {
      super(name, Kind.FUNCTION, sourceCode);
      this.parameterList = new ArrayList<>();
    }

    @Override
    public boolean isFunctionSymbol() {
      return true;
    }

    @Override
    public List<VariableSymbol> parameters() {
      return new ArrayList<>(parameterList);
    }

    /**
     * Adds a parameter to this function.
     *
     * @param param the parameter symbol to add
     */
    public void addParameter(VariableSymbol param) {
      parameterList.add(param);
    }

    @Override
    public Type returnType() {
      return returnTypeValue;
    }

    /**
     * Sets the return type of this function.
     *
     * @param type the return type
     */
    public void setReturnType(Type type) {
      this.returnTypeValue = type != null ? type : Type.UNKNOWN_TYPE;
    }

    @Override
    public List<Type> parameterTypes() {
      List<Type> types = new ArrayList<>();
      for (VariableSymbol param : parameterList) {
        Type paramType = AstNodeTypeExtension.getType(param.declaration());
        types.add(paramType != null ? paramType : Type.UNKNOWN_TYPE);
      }
      return types;
    }

    @Override
    public boolean isVirtual() {
      return false;
    }

    @Override
    public boolean isPureVirtual() {
      return false;
    }

    @Override
    public boolean isInline() {
      return false;
    }

    @Override
    public boolean isConstexpr() {
      return false;
    }

    @Override
    public boolean isConstructor() {
      return false;
    }

    @Override
    public boolean isDestructor() {
      return false;
    }

    @Override
    public boolean isOperator() {
      return false;
    }

    @Override
    public boolean isTemplate() {
      return false;
    }
  }
}
