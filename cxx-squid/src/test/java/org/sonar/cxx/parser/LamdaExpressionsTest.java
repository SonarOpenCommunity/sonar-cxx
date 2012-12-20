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

import com.sonar.sslr.impl.Parser;
import org.junit.Test;
import org.sonar.cxx.api.CxxGrammar;

import static com.sonar.sslr.test.parser.ParserMatchers.parse;
import static org.junit.Assert.assertThat;

/**
 * @author jmecosta
 */
public class LamdaExpressionsTest {
  
  Parser<CxxGrammar> p = CxxParser.create();
  CxxGrammar g = p.getGrammar();
  
  @Test
  public void lambda_expression() {
    p.setRootRule(g.lambda_expression);
    
    g.lambda_introducer.mock(); 
    g.lambda_declarator.mock();
    g.compound_statement.mock();
    
    assertThat(p, parse("lambda_introducer compound_statement"));
    assertThat(p, parse("lambda_introducer lambda_declarator compound_statement"));
  }
  
  @Test
  public void lambda_expression_real() {
    p.setRootRule(g.lambda_expression);

    g.statement_seq.mock();
    
    assertThat(p, parse("[] ( ) { }"));
    assertThat(p, parse("[] (int n) { }"));
    assertThat(p, parse("[] (int n) { statement_seq }"));
    assertThat(p, parse("[&] ( ) { }"));
    assertThat(p, parse("[&foo] (int n) { statement_seq }"));    
    assertThat(p, parse("[=] (int n) { statement_seq }"));    
    assertThat(p, parse("[=,&foo] (int n) { statement_seq }"));   
    assertThat(p, parse("[&foo1,&foo2,&foo3] (int n, int y, int z) { statement_seq }"));    
    assertThat(p, parse("[] () throw () { statement_seq }"));
  }
  
  @Test
  public void lambda_expression_more_real() {
    p.setRootRule(g.lambda_expression);
    
    // examples taken from http://www.cprogramming.com/c++11/c++11-lambda-closures.html
    assertThat(p, parse("[] () -> int { return 1; }"));     
    assertThat(p, parse("[] (const string& addr) { return addr.find( \".org\" ) != string::npos; }"));   
    assertThat(p, parse("[this] () { cout << _x; }"));
    // function pointers c++11, todo: make this work
    // assertThat(p, parse("[] () -> { return 2; }"));
  }          
  
  @Test
  public void lambda_introducer() {
    p.setRootRule(g.lambda_introducer);
    g.lambda_capture.mock();
    
    assertThat(p, parse("[]"));        
    assertThat(p, parse("[lambda_capture]"));    
  }    

  @Test
  public void lambda_introducer_reallife() {
    p.setRootRule(g.lambda_introducer);
    
    assertThat(p, parse("[&]"));
    assertThat(p, parse("[=]"));
    assertThat(p, parse("[bar]"));
    assertThat(p, parse("[this]"));    
    assertThat(p, parse("[&foo]"));
    assertThat(p, parse("[=,&foo]"));    
  }      
    
  @Test
  public void lambda_capture() {
    p.setRootRule(g.lambda_capture);
    g.capture_default.mock();
    g.capture_list.mock();    
    
    assertThat(p, parse("capture_default"));
    assertThat(p, parse("capture_list"));
    assertThat(p, parse("capture_default , capture_list"));
  }    

  @Test
  public void capture_default() {
    p.setRootRule(g.capture_default);
    
    assertThat(p, parse("&"));
    assertThat(p, parse("="));
  }      
      
  @Test
  public void capture() {
    p.setRootRule(g.capture);
    
    assertThat(p, parse("foo"));
    assertThat(p, parse("&foo"));
    assertThat(p, parse("this"));
  }      

  @Test
  public void capture_list() {
    p.setRootRule(g.capture_list);
    g.capture.mock();
    
    assertThat(p, parse("capture")); // or 1, optional out        
    assertThat(p, parse("capture ...")); // or 1, optional in
    assertThat(p, parse("capture , capture")); // or 1, optional out            
    assertThat(p, parse("capture , capture ...")); // or 1, optional in                
  }      

  @Test
  public void lambda_declarator() {
    p.setRootRule(g.lambda_declarator);
    g.parameter_declaration_clause.mock();
    g.exception_specification.mock();
    g.attribute_specifier_seq.mock();
    g.trailing_return_type.mock();
    
    assertThat(p, parse("( parameter_declaration_clause ) ")); // all opt out
    assertThat(p, parse("( parameter_declaration_clause ) mutable")); // mutable in
    assertThat(p, parse("( parameter_declaration_clause ) exception_specification")); // exception_specification in
    assertThat(p, parse("( parameter_declaration_clause ) attribute_specifier_seq")); // attribute_specifier_seq in
    assertThat(p, parse("( parameter_declaration_clause ) trailing_return_type")); // trailing_return_type in
    assertThat(p, parse("( parameter_declaration_clause ) mutable exception_specification")); // complex 1    
    assertThat(p, parse("( parameter_declaration_clause ) mutable exception_specification attribute_specifier_seq")); // complex 2
    assertThat(p, parse("( parameter_declaration_clause ) mutable exception_specification attribute_specifier_seq trailing_return_type")); // complex 3   
    assertThat(p, parse("( parameter_declaration_clause ) exception_specification attribute_specifier_seq")); // complex 4
    assertThat(p, parse("( parameter_declaration_clause ) exception_specification attribute_specifier_seq trailing_return_type")); // complex 5
    assertThat(p, parse("( parameter_declaration_clause ) attribute_specifier_seq trailing_return_type")); // complex 6
  }
}
