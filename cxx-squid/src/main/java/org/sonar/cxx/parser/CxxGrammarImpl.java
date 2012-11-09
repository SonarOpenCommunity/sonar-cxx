/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns
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
import static org.sonar.cxx.api.CxxTokenType.CHARACTER;
import static org.sonar.cxx.api.CxxTokenType.NUMBER;
import static org.sonar.cxx.api.CxxTokenType.STRING;

/**
 * Based on the C++ Standard, Appendix A
 */
public class CxxGrammarImpl extends CxxGrammar {
  public CxxGrammarImpl() {
    toplevel();
    expressions();
    statements();
    declarations();
    declarators();
    classes();
    derived_classes();
    special_member_functions();
    overloading();
    templates();
    exception_handling();

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
    translation_unit.is(o2n(declaration), EOF);
  }

  private void expressions() {
    primary_expression.is(
        or(
            literal,
            "this",
            and("(", expression, ")"),
            id_expression,
            lambda_expression
        )
        );

    id_expression.is(
        or(
            qualified_id,
            unqualified_id
        )
        );

    unqualified_id.is(
        or(
            operator_function_id,
            conversion_function_id,
            literal_operator_id,
            and("~", class_name),
            and("~", decltype_specifier),
            template_id,
            IDENTIFIER
        )
        );

    qualified_id.is(
        or(
            and(nested_name_specifier, opt("template"), unqualified_id),
            and("::", IDENTIFIER),
            and("::", operator_function_id),
            and("::", literal_operator_id),
            and("::", template_id)
        )
        );

    nested_name_specifier.is(
        or(
            and(opt("::"), type_name, "::"),
            and(opt("::"), namespace_name, "::"),
            and(decltype_specifier, "::")
        ),
        o2n(
        or(
            and(IDENTIFIER, "::"),
            and(opt("template"), simple_template_id, "::")
        )
        )
        );

    lambda_expression.is(lambda_introducer, opt(lambda_declarator), compound_statement);

    lambda_introducer.is("[", opt(lambda_capture), "]");

    lambda_capture.is(
        or(
            and(capture_default, ",", capture_list),
            capture_list,
            capture_default
        ));

    capture_default.is(
        or(
            "&",
            "="
        ));

    capture_list.is(and(capture, opt("...")), o2n(",", and(capture, opt("..."))));

    capture.is(
        or(
            IDENTIFIER,
            and("&", IDENTIFIER),
            "this"
        ));

    lambda_declarator.is(
        "(", parameter_declaration_clause, ")", opt("mutable"),
        opt(exception_specification), opt(attribute_specifier_seq), opt(trailing_return_type)
        );

    postfix_expression.is(
        or(
            and(simple_type_specifier, "(", opt(expression_list), ")",
                // TODO: thats a clumsy trial to disabmiguate that rule from a fuction call.
                // make it smarter.
                not(or(".", "->"))),
            and(simple_type_specifier, braced_init_list),
            and(typename_specifier, "(", opt(expression_list), ")"),
            and(typename_specifier, braced_init_list),

            primary_expression,

            and("dynamic_cast", "<", type_id, ">", "(", expression, ")"),
            and("static_cast", "<", type_id, ">", "(", expression, ")"),
            and("reinterpret_cast", "<", type_id, ">", "(", expression, ")"),
            and("const_cast", "<", type_id, ">", "(", expression, ")"),
            and("typeid", "(", expression, ")"),
            and("typeid", "(", type_id, ")")
        ),

        // postfix_expression [ expression ]
        // postfix_expression [ braced_init_list ]
        // postfix_expression ( expression_listopt )
        // postfix_expression . templateopt id_expression
        // postfix_expression -> templateopt id_expression
        // postfix_expression . pseudo_destructor_name
        // postfix_expression -> pseudo_destructor_name
        // postfix_expression ++
        // postfix_expression --

        // should replace the left recursive stuff above

        o2n(
        or(
            and("[", expression, "]"),
            and("(", opt(expression_list), ")"),
            and(or(".", "->"),
                or(and(opt("template"), id_expression),
                    pseudo_destructor_name)),
            "++",
            "--"
        )
        )
        );

    expression_list.is(initializer_list);

    pseudo_destructor_name.is(
        or(
            and(opt(nested_name_specifier), type_name, "::", "~", type_name),
            and(nested_name_specifier, "template", simple_template_id, "::", "~", type_name),
            and(opt(nested_name_specifier), "~", type_name),
            and("~", decltype_specifier)
        )
        );

    unary_expression.is(
        or(
            and(unary_operator, cast_expression),
            postfix_expression,
            and("++", cast_expression),
            and("--", cast_expression),
            and("sizeof", unary_expression),
            and("sizeof", "(", type_id, ")"),
            and("sizeof", "...", "(", IDENTIFIER, ")"),
            and("alignof", "(", type_id, ")"),
            noexcept_expression,
            new_expression,
            delete_expression
        )
        );

    unary_operator.is(
        or("*", "&", "+", "-", "!", "~")
        );

    new_expression.is(
        or(
            and(opt("::"), "new", opt(new_placement), new_type_id, opt(new_initializer)),
            and(opt("::"), "new", new_placement, "(", type_id, ")", opt(new_initializer)),
            and(opt("::"), "new", "(", type_id, ")", opt(new_initializer))
        )
        );

    new_placement.is("(", expression_list, ")");

    new_type_id.is(type_specifier_seq, opt(new_declarator));

    new_declarator.is(
        or(
            noptr_new_declarator,
            and(ptr_operator, opt(new_declarator))
        )
        );

    noptr_new_declarator.is("[", expression, "]", opt(attribute_specifier_seq), o2n("[", constant_expression, "]", opt(attribute_specifier_seq)));

    new_initializer.is(
        or(
            and("(", opt(expression_list), ")"),
            braced_init_list
        )
        );

    delete_expression.is(opt("::"), "delete", opt("[", "]"), cast_expression);

    noexcept_expression.is("noexcept", "(", expression, ")");

    cast_expression.is(
        or(
            and(next("(", type_id, ")"), "(", type_id, ")", cast_expression),
            unary_expression
        )
        );

    pm_expression.is(cast_expression, o2n(or(".*", "->*"), cast_expression));

    multiplicative_expression.is(pm_expression, o2n(or("*", "/", "%"), pm_expression));

    additive_expression.is(multiplicative_expression, o2n(or("+", "-"), multiplicative_expression));

    shift_expression.is(additive_expression, o2n(or("<<", ">>"), additive_expression));

    relational_expression.is(shift_expression, o2n(or("<", ">", "<=", ">="), shift_expression));

    equality_expression.is(relational_expression, o2n(or("==", "!="), relational_expression));

    and_expression.is(equality_expression, o2n("&", equality_expression));

    exclusive_or_expression.is(and_expression, o2n("^", and_expression));

    inclusive_or_expression.is(exclusive_or_expression, o2n("|", exclusive_or_expression));

    logical_and_expression.is(inclusive_or_expression, o2n("&&", inclusive_or_expression));

    logical_or_expression.is(logical_and_expression, o2n("||", logical_and_expression));

    conditional_expression.is(
        or(
            and(logical_or_expression, "?", expression, ":", assignment_expression),
            logical_or_expression
        )
        );

    assignment_expression.is(
        or(
            and(logical_or_expression, assignment_operator, initializer_clause),
            conditional_expression,
            throw_expression
        )
        );

    assignment_operator.is(or("=", "*=", "/=", "%=", "+=", "-=", ">>=", "<<=", "&=", "ˆ=", "|="));

    expression.is(assignment_expression, o2n(",", assignment_expression));

    constant_expression.is(conditional_expression);
  }

  private void statements() {
    statement.is(
        or(
            labeled_statement,
            and(opt(attribute_specifier_seq), expression_statement),
            and(opt(attribute_specifier_seq), compound_statement),
            and(opt(attribute_specifier_seq), selection_statement),
            and(opt(attribute_specifier_seq), iteration_statement),
            and(opt(attribute_specifier_seq), jump_statement),
            declaration_statement,
            and(opt(attribute_specifier_seq), try_block)
        )
        );

    labeled_statement.is(opt(attribute_specifier_seq), or(IDENTIFIER, and("case", constant_expression), "default"), ":", statement);

    expression_statement.is(opt(expression), ";");

    compound_statement.is("{", opt(statement_seq), "}");

    statement_seq.is(one2n(statement));

    selection_statement.is(
        or(
            and("if", "(", condition, ")", statement, opt("else", statement)),
            and("switch", "(", condition, ")", statement)
        )
        );

    condition.is(
        or(
            expression,
            and(opt(attribute_specifier_seq), decl_specifier_seq, declarator, or(and("=", initializer_clause), braced_init_list))
        )
        );

    iteration_statement.is(
        or(
            and("while", "(", condition, ")", statement),
            and("do", statement, "while", "(", expression, ")", ";"),
            and("for", "(", for_init_statement, opt(condition), ";", opt(expression), ")", statement),
            and("for", "(", for_range_declaration, ":", for_range_initializer, ")", statement)
        )
        );

    for_init_statement.is(
        or(
            expression_statement,
            simple_declaration
        )
        );

    for_range_declaration.is(opt(attribute_specifier_seq), decl_specifier_seq, declarator);

    for_range_initializer.is(
        or(
            expression,
            braced_init_list
        )
        );

    jump_statement.is(
        or(
            and("break", ";"),
            and("continue", ";"),
            and("return", opt(expression), ";"),
            and("return", braced_init_list, ";"),
            and("goto", IDENTIFIER, ";")
        )
        );

    declaration_statement.is(block_declaration);
  }

  private void declarations() {
    declaration_seq.is(one2n(declaration));

    declaration.is(
        or(
            function_definition,
            block_declaration,
            template_declaration,
            explicit_instantiation,
            explicit_specialization,
            linkage_specification,
            namespace_definition,
            empty_declaration,
            attribute_declaration
        )
        );

    block_declaration.is(
        or(
            simple_declaration,
            asm_definition,
            namespace_alias_definition,
            using_declaration,
            using_directive,
            static_assert_declaration,
            alias_declaration,
            opaque_enum_declaration
        )
        );

    alias_declaration.is("using", IDENTIFIER, opt(attribute_specifier_seq), "=", type_id);

    simple_declaration.is(
      or(
            and(opt(decl_specifier_seq), opt(init_declarator_list), ";"),
            and(attribute_specifier_seq, opt(decl_specifier_seq), init_declarator_list, ";")
        )
        );

    static_assert_declaration.is("static_assert", "(", constant_expression, ",", STRING, ")", ";");

    empty_declaration.is(";");

    attribute_declaration.is(attribute_specifier_seq, ";");

    decl_specifier.is(
        or(
            "friend", "typedef", "constexpr",
            storage_class_specifier,
            function_specifier,
            type_specifier
        )
        );

    decl_specifier_seq.is(
      // FIXME: this implementation failes for constructs like 'Result
      // (*ptr)()'. The problem here is that 'Result' is not consumed
      // as a decl_specifier because of the predicate recognizing
      // 'Result(' wrongly as a declarator followed by '('. Seems like
      // the lookahead logic in decl_specifier_seq has to get smarter,
      // somehow...
      
      one2n(
        not(and(declarator, or("=", ";", "{", "(", ":", ",", virt_specifier))),
        decl_specifier
        ),
      opt(attribute_specifier_seq)
      );

    storage_class_specifier.is(
        or("register", "static", "thread_local", "extern", "mutable")
        );

    function_specifier.is(
        or("inline", "virtual", "explicit")
        );

    typedef_name.is(IDENTIFIER);

    type_specifier.is(
        or(
            class_specifier,
            enum_specifier,
            trailing_type_specifier
        )
        );

    trailing_type_specifier.is(
        or(
            simple_type_specifier,
            elaborated_type_specifier,
            typename_specifier,
            cv_qualifier)
        );

    type_specifier_seq.is(one2n(type_specifier), opt(attribute_specifier_seq));

    trailing_type_specifier_seq.is(one2n(trailing_type_specifier), opt(attribute_specifier_seq));

    simple_type_specifier.is(
        or(
            "char", "char16_t", "char32_t", "wchar_t", "bool", "short", "int", "long", "signed", "unsigned", "float", "double", "void", "auto",
            decltype_specifier,
            and(nested_name_specifier, "template", simple_template_id),
            and(opt(nested_name_specifier), type_name)
        )
        );

    type_name.is(
        or(
            simple_template_id,
            class_name,
            enum_name,
            typedef_name)
        );

    decltype_specifier.is("decltype", "(", expression, ")");

    elaborated_type_specifier.is(
        or(
            and(class_key, opt(nested_name_specifier), opt("template"), simple_template_id),
            
            // TODO: the "::"-Alternative to nested-name-specifier is because of need to parse
            // stuff like "friend class ::A". Figure out if there is another way
            and(class_key, opt(attribute_specifier_seq), opt(or(nested_name_specifier, "::")), IDENTIFIER),

            and("enum", opt(nested_name_specifier), IDENTIFIER)
        )
        );

    enum_name.is(IDENTIFIER);

    enum_specifier.is(
        or(
            and(enum_head, "{", opt(enumerator_list), "}"),
            and(enum_head, "{", enumerator_list, ",", "}")
        )
        );

    enum_head.is(enum_key, opt(attribute_specifier_seq), or(and(nested_name_specifier, IDENTIFIER), opt(IDENTIFIER)), opt(enum_base));

    opaque_enum_declaration.is(enum_key, opt(attribute_specifier_seq), IDENTIFIER, opt(enum_base), ";");

    enum_key.is("enum", opt("class", "struct"));

    enum_base.is(":", type_specifier_seq);

    enumerator_list.is(enumerator_definition, o2n(",", enumerator_definition));

    enumerator_definition.is(enumerator, opt("=", constant_expression));

    enumerator.is(IDENTIFIER);

    namespace_name.is(
        or(
            original_namespace_name,
            namespace_alias
        )
        );

    original_namespace_name.is(IDENTIFIER);

    namespace_definition.is(
        or(
            named_namespace_definition,
            unnamed_namespace_definition
        )
        );

    named_namespace_definition.is(
        or(
            original_namespace_definition,
            extension_namespace_definition
        )
        );

    original_namespace_definition.is(opt("inline"), "namespace", IDENTIFIER, "{", namespace_body, "}");

    extension_namespace_definition.is(opt("inline"), "namespace", original_namespace_name, "{", namespace_body, "}");

    unnamed_namespace_definition.is(opt("inline"), "namespace", "{", namespace_body, "}");

    namespace_body.is(opt(declaration_seq));

    namespace_alias.is(IDENTIFIER);

    namespace_alias_definition.is("namespace", IDENTIFIER, "=", qualified_namespace_specifier, ";");

    qualified_namespace_specifier.is(opt(nested_name_specifier), namespace_name);

    using_declaration.is(
        or(
            and("using", opt("typename"), nested_name_specifier, unqualified_id, ";"),
            and("using", "::", unqualified_id, ";")
        )
        );

    using_directive.is(opt(attribute_specifier), "using", "namespace", opt("::"), opt(nested_name_specifier), namespace_name, ";");

    asm_definition.is("asm", "(", STRING, ")", ";");

    linkage_specification.is("extern", STRING, or(and("{", opt(declaration_seq), "}"), declaration));

    attribute_specifier_seq.is(one2n(attribute_specifier));
    
    attribute_specifier.is(
            or(
                and("[","[", attribute_list, "]", "]"),
                alignment_specifier               
            ));

    alignment_specifier.is(
            or(
                and("alignas", "(", type_id, opt("..."), ")"),
                and("alignas", "(", assignment_expression, opt("..."), ")")
            ));
       
    attribute_list.is(
            or(
                and(attribute, "...", o2n(",", attribute, "...")),            
                and(opt(attribute), o2n(",", opt(attribute)))                  
            ));
    
    attribute.is(attribute_token, opt(attribute_argument_clause));

    attribute_token.is(
            or(
                attribute_scoped_token,
                IDENTIFIER            
            ));
    
    attribute_scoped_token.is(attribute_namespace, "::", IDENTIFIER);
    
    attribute_namespace.is(IDENTIFIER);
    
    attribute_argument_clause.is("(", balanced_token_seq ,")");
        
    balanced_token_seq.is(o2n(balanced_token));
    
    balanced_token.is(
            or(
                IDENTIFIER,
                and("(", balanced_token_seq, ")"),            
                and("{", balanced_token_seq, "}"),
                and("[", balanced_token_seq, "]")                     
            ));
  }

  private void declarators() {
    init_declarator_list.is(init_declarator, o2n(",", init_declarator));

    init_declarator.is(declarator, opt(initializer));

    declarator.is(
        or(
            ptr_declarator,
            and(noptr_declarator, parameters_and_qualifiers, trailing_return_type)
        )
        );

    ptr_declarator.is(
        or(
            noptr_declarator,
            and(ptr_operator, ptr_declarator)
        )
        );

    noptr_declarator.is(
        or(
            and(declarator_id, opt(attribute_specifier_seq)),
            and("(", ptr_declarator, ")")
        ),
        o2n(
        or(
            parameters_and_qualifiers,
            and("[", opt(constant_expression), "]", opt(attribute_specifier_seq))
        )
        )
        );

    parameters_and_qualifiers.is("(", parameter_declaration_clause, ")", opt(attribute_specifier_seq), opt(cv_qualifier_seq), opt(ref_qualifier), opt(exception_specification));

    trailing_return_type.is("->", trailing_type_specifier_seq, opt(abstract_declarator));

    ptr_operator.is(
        or(
            and("*", opt(attribute_specifier_seq), opt(cv_qualifier_seq)),
            and("&", opt(attribute_specifier_seq)),
            and("&&", opt(attribute_specifier_seq)),
            and(nested_name_specifier, "*", opt(attribute_specifier_seq), opt(cv_qualifier_seq))
        )
        );

    cv_qualifier_seq.is(one2n(cv_qualifier));

    cv_qualifier.is(
        or("const", "volatile")
        );

    ref_qualifier.is(
        or("&", "&&")
        );

    declarator_id.is(
        or(
            and(opt(nested_name_specifier), class_name),
            and(opt("..."), id_expression)
        )
        );

    type_id.is(type_specifier_seq, opt(abstract_declarator));

    abstract_declarator.is(
        or(
            ptr_abstract_declarator,
            and(opt(noptr_abstract_declarator), parameters_and_qualifiers, trailing_return_type),
            abstract_pack_declarator
        )
        );

    ptr_abstract_declarator.is(o2n(ptr_operator), opt(noptr_abstract_declarator));

    noptr_abstract_declarator.is(
        opt("(", ptr_abstract_declarator, ")"),
        o2n(
        or(
            parameters_and_qualifiers,
            and("[", opt(constant_expression), "]", opt(attribute_specifier_seq))
        )
        )
        );

    abstract_pack_declarator.is(o2n(ptr_operator), noptr_abstract_pack_declarator);

    noptr_abstract_pack_declarator.is(
        "...",
        o2n(or(parameters_and_qualifiers,
            and("[", opt(constant_expression), "]", opt(attribute_specifier_seq))
        )
        )
        );

    parameter_declaration_clause.is(
        or(
            and(parameter_declaration_list, ",", "..."),
            and(opt(parameter_declaration_list), opt("...")),
            "..."
        )
        );

    parameter_declaration_list.is(parameter_declaration, o2n(",", parameter_declaration));

    parameter_declaration.is(
        or(
            and(opt(attribute_specifier_seq), decl_specifier_seq, declarator, opt("=", initializer_clause)),
            and(opt(attribute_specifier_seq), decl_specifier_seq, opt(abstract_declarator), opt("=", initializer_clause)),

            // FIXME: this case should actually be covered by the previous rule.
            // But it doesnt match because of the decl_specifier_seq being to greedy
            and(type_specifier, opt("=", initializer_clause))
        )
        );

    function_definition.is(opt(attribute_specifier_seq), opt(decl_specifier_seq), declarator, opt(virt_specifier_seq), function_body);

    function_body.is(
        or(
            and(opt(ctor_initializer), compound_statement),
            function_try_block,
            and("=", "delete", ";"),
            and("=", "default", ";")
        )
        );

    initializer.is(
        or(
            and("(", expression_list, ")"),
            brace_or_equal_initializer
        )
        );

    brace_or_equal_initializer.is(
        or(
            and("=", initializer_clause),
            braced_init_list
        )
        );

    initializer_clause.is(
        or(
            assignment_expression,
            braced_init_list
        )
        );

    initializer_list.is(initializer_clause, opt("..."), o2n(",", initializer_clause, opt("...")));

    braced_init_list.is("{", opt(initializer_list), opt(","), "}");
  }

  private void classes() {
    class_name.is(
        or(
            simple_template_id,
            IDENTIFIER
        )
        );

    class_specifier.is(class_head, "{", opt(member_specification), "}");

    class_head.is(
        or(
            and(class_key, opt(attribute_specifier_seq), class_head_name, opt(class_virt_specifier), opt(base_clause)),
            and(class_key, opt(attribute_specifier_seq), opt(base_clause))
        )
        );

    class_head_name.is(opt(nested_name_specifier), class_name);

    class_virt_specifier.is("final");

    class_key.is(
        or("class", "struct", "union")
        );

    member_specification.is(
        one2n(
        or(
            member_declaration,
            and(access_specifier, ":")
        )
        )
        );

    member_declaration.is(
        or(
            //TODO: remote after the decl_specifier_seq has been made smarter.
            and(opt(attribute_specifier_seq), decl_specifier, opt(member_declarator_list), ";"),

            and(opt(attribute_specifier_seq), opt(decl_specifier_seq), opt(member_declarator_list), ";"),
            and(function_definition, opt(";")),
            and(opt("::"), nested_name_specifier, opt("template"), unqualified_id, ";"),
            using_declaration,
            static_assert_declaration,
            template_declaration,
            alias_declaration
        )
        );

    member_declarator_list.is(member_declarator, o2n(",", member_declarator));

    member_declarator.is(
        or(
            and(declarator, brace_or_equal_initializer),
            and(declarator, virt_specifier_seq, opt(pure_specifier)),
            declarator,
            and(opt(IDENTIFIER), opt(attribute_specifier_seq), ":", constant_expression)
        )
        );

    virt_specifier_seq.is(one2n(virt_specifier));

    virt_specifier.is(
        or("override", "final")
        );

    pure_specifier.is("=", "0");
  }

  private void derived_classes() {
    base_clause.is(":", base_specifier_list);

    base_specifier_list.is(base_specifier, opt("..."), o2n(",", base_specifier, opt("...")));

    base_specifier.is(
        or(
            and(opt(attribute_specifier_seq), base_type_specifier),
            and(opt(attribute_specifier_seq), "virtual", opt(access_specifier), base_type_specifier),
            and(opt(attribute_specifier_seq), access_specifier, opt("virtual"), base_type_specifier)
        )
        );

    class_or_decltype.is(
        or(
            and(opt(nested_name_specifier), class_name),
            decltype_specifier)
        );

    base_type_specifier.is(class_or_decltype);

    access_specifier.is(
        or("private", "protected", "public")
        );
  }

  private void special_member_functions() {
    conversion_function_id.is("operator", conversion_type_id);

    conversion_type_id.is(type_specifier_seq, opt(conversion_declarator));

    conversion_declarator.is(one2n(ptr_operator));

    ctor_initializer.is(":", mem_initializer_list);

    mem_initializer_list.is(mem_initializer, opt("..."), o2n(",", mem_initializer, opt("...")));

    mem_initializer.is(mem_initializer_id, or(and("(", opt(expression_list), ")"), braced_init_list));

    mem_initializer_id.is(
        or(
            class_or_decltype,
            IDENTIFIER
        )
        );
  }

  private void overloading() {
    operator_function_id.is("operator", operator);

    operator.is(
        or(
            and("new", "[", "]"),
            and("delete", "[", "]"),
            "new", "delete",
            "+", "-", "!", "=", "ˆ=", "&=", "<=", ">=",
            and("(", ")"),
            and("[", "]"),
            "*", "<", "|=", "&&", "/",
            ">", "<<", "||", "%", "+=", ">>", "++", "ˆ", "-=", ">>=", "--", "&", "*=", "<<=",
            ",", "|", "/=", "==", "->*", "∼", "%=", "!=", "->"
        )
        );

    literal_operator_id.is("operator", "\"\"", IDENTIFIER);
  }

  private void templates() {
    template_declaration.is("template", "<", template_parameter_list, ">", declaration);

    template_parameter_list.is(template_parameter, o2n(",", template_parameter));

    template_parameter.is(
        or(
            type_parameter,
            parameter_declaration
        )
        );

    type_parameter.is(
        or(
            and("class", opt(IDENTIFIER), "=", type_id),
            and("class", opt("..."), opt(IDENTIFIER)),
            and("typename", opt(IDENTIFIER), "=", type_id),
            and("typename", opt("..."), opt(IDENTIFIER)),
            and("template", "<", template_parameter_list, ">", "class", opt(IDENTIFIER), "=", id_expression),
            and("template", "<", template_parameter_list, ">", "class", opt("..."), opt(IDENTIFIER))
        )
        );

    simple_template_id.is(template_name, "<", opt(template_argument_list), ">");

    template_id.is(
        or(
            simple_template_id,
            and(operator_function_id, "<", opt(template_argument_list), ">"),
            and(literal_operator_id, "<", opt(template_argument_list), ">")
        )
        );

    template_name.is(IDENTIFIER);

    template_argument_list.is(template_argument, opt("..."), o2n(",", template_argument, opt("...")));

    template_argument.is(
        or(
            type_id,

            // FIXME: workaround to parse stuff like "carray<int, 10>"
            // actually, it should be covered by the next rule (constant_expression)
            // but it doesnt work. 
            literal,
            
            constant_expression,
            id_expression
        )
        );

    typename_specifier.is(
        "typename", nested_name_specifier,
        or(and(opt("template"), simple_template_id), IDENTIFIER));

    explicit_instantiation.is(opt("extern"), "template", declaration);

    explicit_specialization.is("template", "<", ">", declaration);
  }

  private void exception_handling() {
    try_block.is("try", compound_statement, handler_seq);

    function_try_block.is("try", opt(ctor_initializer), compound_statement, handler_seq);

    handler_seq.is(one2n(handler));

    handler.is("catch", "(", exception_declaration, ")", compound_statement);

    exception_declaration.is(
        or(
            and(opt(attribute_specifier_seq), type_specifier_seq, or(declarator, opt(abstract_declarator))),
            "..."
        )
        );

    throw_expression.is("throw", opt(assignment_expression));

    exception_specification.is(
        or(
            dynamic_exception_specification,
            noexcept_specification
        )
        );

    dynamic_exception_specification.is("throw", "(", opt(type_id_list), ")");

    type_id_list.is(type_id, opt("..."), o2n(",", type_id, opt("...")));

    noexcept_specification.is("noexcept", opt("(", constant_expression, ")"));
  }
}
