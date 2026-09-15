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
package org.sonar.cxx.squidbridge.api;

import com.sonar.cxx.sslr.api.AstNode;
import java.lang.ref.WeakReference;
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
 *
 * <p>{@link AstNodeSymbolExtension} keys a process-wide {@link java.util.WeakHashMap} on the very
 * {@link AstNode} passed to {@link #setDeclaration}, and every caller in {@code
 * CxxSymbolResolverVisitor} registers a symbol under its own declaration node ({@code
 * AstNodeSymbolExtension.setSymbol(declaratorId, symbol)} where {@code symbol.declaration() ==
 * declaratorId}). A strong {@code declarationNode} field would therefore hold a reference back to
 * its own map key, which keeps the entry permanently reachable (key -&gt; value -&gt; key) and
 * defeats {@code WeakHashMap} eviction entirely, regardless of any caller-side {@code clear()}
 * discipline. Storing it behind a {@link WeakReference} instead breaks that cycle at its source:
 * once nothing outside these maps holds the node, both the key and this reference clear together.
 */
public class SourceCodeSymbol implements Symbol {

  protected final SourceCode sourceCode;
  protected final String name;
  protected final Kind kind;
  protected final List<Usage> usagesList;
  @Nullable
  protected WeakReference<AstNode> declarationNode;
  protected Symbol ownerSymbol;
  private TypeSymbol enclosingClassSymbol;
  private boolean enclosingClassResolved;

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
    if (ownerSymbol == null && sourceCode != null && sourceCode.getParent() != null) {
      // Cached so mutations through the returned instance (e.g. setMemberScope()) persist.
      SourceCode parent = sourceCode.getParent();
      ownerSymbol = new SourceCodeSymbol(parent, deriveKindFromSourceCode(parent));
    }
    return ownerSymbol;
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
    if (!enclosingClassResolved) {
      // Cached for the same reason as owner() above.
      enclosingClassResolved = true;
      if (sourceCode != null) {
        SourceCode parent = sourceCode.getParent(SourceClass.class);
        if (parent != null) {
          enclosingClassSymbol = new SourceCodeTypeSymbol((SourceClass) parent);
        }
      }
    }
    return enclosingClassSymbol;
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
    return declarationNode != null ? declarationNode.get() : null;
  }

  /**
   * Sets the declaration node for this symbol.
   *
   * <p>Held via a {@link WeakReference} (see the class javadoc): this symbol is itself the value
   * registered under {@code node} as the key in {@link AstNodeSymbolExtension}'s process-wide map,
   * so a strong reference here would keep that map entry permanently reachable.
   *
   * @param node the AST node representing the declaration
   */
  public void setDeclaration(@Nullable AstNode node) {
    this.declarationNode = node != null ? new WeakReference<>(node) : null;
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
   *
   * <p>{@code node} is stored behind a {@link WeakReference} for the same reason as {@link
   * SourceCodeSymbol#declarationNode}: a usage is appended to the owning symbol's own {@code
   * usagesList} (see {@code CxxSymbolResolverVisitor#resolveIdentifierUsage}) under the very node
   * that symbol was just registered against in {@link AstNodeSymbolExtension}, so a strong
   * reference here would create the same key -&gt; value -&gt; key cycle.
   */
  public static class SourceCodeUsage implements Usage {
    private final WeakReference<AstNode> node;
    private final Symbol symbol;
    private final UsageKind usageKind;

    public SourceCodeUsage(AstNode node, Symbol symbol, UsageKind usageKind) {
      this.node = new WeakReference<>(node);
      this.symbol = symbol;
      this.usageKind = usageKind;
    }

    @Override
    @Nullable
    public AstNode node() {
      return node.get();
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

    /**
     * What kind of type declaration a {@link SourceCodeTypeSymbol} represents. Exactly one at a
     * time; {@link #isClass()}, {@link #isStruct()}, etc. are derived from it.
     */
    public enum TypeKind {
      UNKNOWN,
      CLASS,
      STRUCT,
      UNION,
      ENUM,
      TYPEDEF
    }

    private TypeKind typeKind = TypeKind.UNKNOWN;
    private boolean isScopedEnumFlag;
    private SymbolTable memberScopeTable;

    public SourceCodeTypeSymbol(SourceClass sourceClass) {
      super(sourceClass, Kind.TYPE);
      this.typeKind = TypeKind.CLASS;
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
      return typeKind == TypeKind.CLASS;
    }

    @Override
    public boolean isStruct() {
      return typeKind == TypeKind.STRUCT;
    }

    @Override
    public boolean isUnion() {
      return typeKind == TypeKind.UNION;
    }

    @Override
    public boolean isEnum() {
      return typeKind == TypeKind.ENUM;
    }

    /**
     * @return this type symbol's declaration kind
     */
    public TypeKind typeKind() {
      return typeKind;
    }

    /**
     * @param kind the declaration kind, replacing any previously set one
     */
    public void setTypeKind(TypeKind kind) {
      this.typeKind = kind;
    }

    @Override
    public boolean isScopedEnum() {
      return isScopedEnumFlag;
    }

    /**
     * Marks this type symbol as representing a scoped enum ({@code enum class}/{@code enum
     * struct}).
     *
     * @param value true if this is a scoped enum
     */
    public void setScopedEnum(boolean value) {
      this.isScopedEnumFlag = value;
    }

    @Override
    @Nullable
    public SymbolTable memberScope() {
      return memberScopeTable;
    }

    /**
     * Sets the scope containing this type's qualified-access-only members.
     *
     * @param scope the member scope, or null to clear it
     */
    public void setMemberScope(@Nullable SymbolTable scope) {
      this.memberScopeTable = scope;
    }

    @Override
    public boolean isTypedef() {
      return typeKind == TypeKind.TYPEDEF;
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
    @Nullable
    private WeakReference<AstNode> initializerNode;
    @Nullable
    private TypeSymbol declaredTypeSymbol;

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

    @Override
    @Nullable
    public AstNode initializer() {
      return initializerNode != null ? initializerNode.get() : null;
    }

    /**
     * Sets the initializer expression of this variable's declaration.
     *
     * <p>Held via a {@link WeakReference} for the same reason as {@link
     * SourceCodeSymbol#declarationNode}: this symbol is the value registered in {@link
     * AstNodeSymbolExtension} under its own declaration node, and this initializer node belongs to
     * the same file's AST, so a strong reference here would keep that file's tree reachable for as
     * long as the map entry exists.
     *
     * @param node the initializer expression AstNode
     */
    public void setInitializer(@Nullable AstNode node) {
      this.initializerNode = node != null ? new WeakReference<>(node) : null;
    }

    @Override
    @Nullable
    public TypeSymbol declaredType() {
      return declaredTypeSymbol;
    }

    /**
     * Sets this variable's own declared class/struct/union type.
     *
     * @param typeSymbol the declared type's TypeSymbol
     */
    public void setDeclaredType(@Nullable TypeSymbol typeSymbol) {
      this.declaredTypeSymbol = typeSymbol;
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
