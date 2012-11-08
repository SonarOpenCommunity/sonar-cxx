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
package org.sonar.cxx.api;

import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Rule;

public class CxxGrammar extends Grammar {
  public Rule test;

  public Rule bool;
  public Rule literal;

  // Top-level components
  public Rule translation_unit;

  // Expressions
  public Rule primary_expression;
  public Rule id_expression;
  public Rule unqualified_id;
  public Rule qualified_id;
  public Rule nested_name_specifier;
  public Rule lambda_expression;
  public Rule lambda_introducer;
  public Rule lambda_capture;
  public Rule capture_default;
  public Rule capture_list;
  public Rule capture;
  public Rule lambda_declarator;
  public Rule postfix_expression;
  public Rule expression_list;
  public Rule pseudo_destructor_name;
  public Rule unary_expression;
  public Rule unary_operator;
  public Rule new_expression;
  public Rule new_placement;
  public Rule new_type_id;
  public Rule new_declarator;
  public Rule noptr_new_declarator;
  public Rule new_initializer;
  public Rule delete_expression;
  public Rule noexcept_expression;
  public Rule cast_expression;
  public Rule pm_expression;
  public Rule multiplicative_expression;
  public Rule additive_expression;
  public Rule shift_expression;
  public Rule relational_expression;
  public Rule equality_expression;
  public Rule and_expression;
  public Rule exclusive_or_expression;
  public Rule inclusive_or_expression;
  public Rule logical_and_expression;
  public Rule logical_or_expression;
  public Rule conditional_expression;
  public Rule assignment_expression;
  public Rule assignment_operator;
  public Rule expression;
  public Rule constant_expression;

  // Statements
  public Rule statement;
  public Rule labeled_statement;
  public Rule expression_statement;
  public Rule compound_statement;
  public Rule statement_seq;
  public Rule selection_statement;
  public Rule condition;
  public Rule iteration_statement;
  public Rule for_init_statement;
  public Rule for_range_declaration;
  public Rule for_range_initializer;
  public Rule jump_statement;
  public Rule declaration_statement;

  // Declarations
  public Rule declaration_seq;
  public Rule declaration;
  public Rule block_declaration;
  public Rule alias_declaration;
  public Rule simple_declaration;
  public Rule static_assert_declaration;
  public Rule empty_declaration;
  public Rule attribute_declaration;
  public Rule decl_specifier;
  public Rule decl_specifier_seq;
  public Rule storage_class_specifier;
  public Rule function_specifier;
  public Rule typedef_name;
  public Rule type_specifier;
  public Rule trailing_type_specifier;
  public Rule type_specifier_seq;
  public Rule trailing_type_specifier_seq;
  public Rule simple_type_specifier;
  public Rule type_name;
  public Rule decltype_specifier;
  public Rule elaborated_type_specifier;
  public Rule enum_name;
  public Rule enum_specifier;
  public Rule enum_head;
  public Rule opaque_enum_declaration;
  public Rule enum_key;
  public Rule enum_base;
  public Rule enumerator_list;
  public Rule enumerator_definition;
  public Rule enumerator;
  public Rule namespace_name;
  public Rule original_namespace_name;
  public Rule namespace_definition;
  public Rule named_namespace_definition;
  public Rule original_namespace_definition;
  public Rule extension_namespace_definition;
  public Rule unnamed_namespace_definition;
  public Rule namespace_body;
  public Rule namespace_alias;
  public Rule namespace_alias_definition;
  public Rule qualified_namespace_specifier;
  public Rule using_declaration;
  public Rule using_directive;
  public Rule asm_definition;
  public Rule linkage_specification;
  public Rule attribute_specifier_seq;
  public Rule attribute_specifier;
  public Rule attribute_list;
  public Rule attribute;
  public Rule attribute_token;
  public Rule attribute_scoped_token;
  public Rule attribute_namespace;
  public Rule attribute_argument_clause;
  public Rule balanced_token_seq;
  public Rule balanced_token;

  // Declarators
  public Rule init_declarator_list;
  public Rule init_declarator;
  public Rule declarator;
  public Rule ptr_declarator;
  public Rule noptr_declarator;
  public Rule parameters_and_qualifiers;
  public Rule trailing_return_type;
  public Rule ptr_operator;
  public Rule cv_qualifier_seq;
  public Rule cv_qualifier;
  public Rule ref_qualifier;
  public Rule declarator_id;
  public Rule type_id;
  public Rule abstract_declarator;
  public Rule ptr_abstract_declarator;
  public Rule noptr_abstract_declarator;
  public Rule abstract_pack_declarator;
  public Rule noptr_abstract_pack_declarator;
  public Rule parameter_declaration_clause;
  public Rule parameter_declaration_list;
  public Rule parameter_declaration;
  public Rule function_definition;
  public Rule function_body;
  public Rule initializer;
  public Rule brace_or_equal_initializer;
  public Rule initializer_clause;
  public Rule initializer_list;
  public Rule braced_init_list;

  // Classes
  public Rule class_name;
  public Rule class_specifier;
  public Rule class_head;
  public Rule class_head_name;
  public Rule class_virt_specifier;
  public Rule class_key;
  public Rule member_specification;
  public Rule member_declaration;
  public Rule member_declarator_list;
  public Rule member_declarator;
  public Rule virt_specifier_seq;
  public Rule virt_specifier;
  public Rule pure_specifier;

  // Derived classes
  public Rule base_clause;
  public Rule base_specifier_list;
  public Rule base_specifier;
  public Rule class_or_decltype;
  public Rule base_type_specifier;
  public Rule access_specifier;

  // Special member functions
  public Rule conversion_function_id;
  public Rule conversion_type_id;
  public Rule conversion_declarator;
  public Rule ctor_initializer;
  public Rule mem_initializer_list;
  public Rule mem_initializer;
  public Rule mem_initializer_id;

  // Overloading
  public Rule operator_function_id;
  public Rule operator;
  public Rule literal_operator_id;

  // Templates
  public Rule template_declaration;
  public Rule template_parameter_list;
  public Rule template_parameter;
  public Rule type_parameter;
  public Rule simple_template_id;
  public Rule template_id;
  public Rule template_name;
  public Rule template_argument_list;
  public Rule template_argument;
  public Rule typename_specifier;
  public Rule explicit_instantiation;
  public Rule explicit_specialization;

  // Exception handling
  public Rule try_block;
  public Rule function_try_block;
  public Rule handler_seq;
  public Rule handler;
  public Rule exception_declaration;
  public Rule throw_expression;
  public Rule exception_specification;
  public Rule dynamic_exception_specification;
  public Rule type_id_list;
  public Rule noexcept_specification;

  @Override
  public Rule getRootRule() {
    return translation_unit;
  }
}
