/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
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
import com.sonar.sslr.squid.SquidAstVisitorContext;
import org.junit.Test;
import org.sonar.cxx.api.CxxGrammar;

import static com.sonar.sslr.test.parser.ParserMatchers.parse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 *
 * @author jmecosta
 */
public class BalancedTokensTest {
    
  Parser<CxxGrammar> p = CxxParser.create(mock(SquidAstVisitorContext.class));
  CxxGrammar g = p.getGrammar();
      
  @Test
  public void attribute_specifier_seq() {
      p.setRootRule(g.attribute_specifier_seq);
      g.attribute_specifier.mock();
      
      assertThat(p, parse("attribute_specifier"));      
      assertThat(p, parse("attribute_specifier attribute_specifier"));
  }

  @Test
  public void attribute_specifier_seq_real() {
      p.setRootRule(g.attribute_specifier_seq);
          
      assertThat(p, parse("[ [ foo :: bar ( { foo }  [ bar ] ) ] ] [ [ foo :: bar ( { foo }  [ bar ] ) ] ]"));
  }  
  
  @Test
  public void attribute_specifier() {
      p.setRootRule(g.attribute_specifier);
      g.attribute_list.mock();
      
      assertThat(p, parse("[ [ attribute_list ] ]"));      
  }

  @Test
  public void attribute_specifier_real() {
      p.setRootRule(g.attribute_specifier);
      
      assertThat(p, parse("[ [ foo :: bar ( { foo }  [ bar ] ) ] ]"));      
  }
  
  @Test
  public void alignment_specifier() {
    p.setRootRule(g.alignment_specifier);
    g.type_id.mock();
    g.assignment_expression.mock();
    
    assertThat(p, parse("alignas ( type_id )"));
    assertThat(p, parse("alignas ( type_id ... )"));
    assertThat(p, parse("alignas ( assignment_expression )"));
    assertThat(p, parse("alignas ( assignment_expression ... )"));
  } 
  
  @Test
  public void attribute_list() {
    p.setRootRule(g.attribute_list);
    g.attribute.mock();  
        
    assertThat(p, parse(""));
    assertThat(p, parse("attribute"));
    assertThat(p, parse("attribute , attribute"));    
    assertThat(p, parse("attribute , "));
    assertThat(p, parse("attribute , attribute , attribute"));
    assertThat(p, parse("attribute ..."));
    assertThat(p, parse("attribute ... , attribute ..."));
  } 
  
  @Test
  public void attribute_list_real() {
    p.setRootRule(g.attribute_list);
        
    assertThat(p, parse("foo :: bar ( { foo }  [ bar ] )"));
    assertThat(p, parse("foo :: bar ( { foo }  [ bar ] ) , foo :: bar ( { foo }  [ bar ] )"));    
    assertThat(p, parse("foo :: bar ( { foo }  [ bar ] ) , "));
    assertThat(p, parse("foo :: bar ( { foo }  [ bar ] ) ..."));
    assertThat(p, parse("foo :: bar ( { foo }  [ bar ] ) ... , foo :: bar ( { foo }  [ bar ] ) ..."));
  }  
  
  @Test
  public void attribute() {
    p.setRootRule(g.attribute);
    g.attribute_token.mock();  
    g.attribute_argument_clause.mock();
        
    assertThat(p, parse("attribute_token attribute_argument_clause"));
    assertThat(p, parse("attribute_token"));
  }
      
  @Test
  public void attribute_real() {
    p.setRootRule(g.attribute);
        
    assertThat(p, parse("foo :: bar ( { foo }  [ bar ] )"));
  }  
  
  @Test
  public void attribute_token() {
    p.setRootRule(g.attribute_token);
    g.attribute_scoped_token.mock();  

    assertThat(p, parse("foo"));
    assertThat(p, parse("attribute_scoped_token"));
  }

  @Test
  public void attribute_token_real() {
    p.setRootRule(g.attribute_token);

    assertThat(p, parse("foo"));
    assertThat(p, parse("foo :: bar"));
  }  
  
  @Test
  public void attribute_scoped_token() {
    p.setRootRule(g.attribute_scoped_token);
    g.attribute_namespace.mock();  
        
    assertThat(p, parse("attribute_namespace :: foo"));
  }

  @Test
  public void attribute_scoped_token_real() {
    p.setRootRule(g.attribute_scoped_token);
        
    assertThat(p, parse("foo :: bar"));
  }  
  
  @Test
  public void attribute_namespace() {
    p.setRootRule(g.attribute_namespace);
    
    assertThat(p, parse("foo"));
  }
  
  @Test
  public void attribute_argument_clause() {
    p.setRootRule(g.attribute_argument_clause);
    g.balanced_token_seq.mock();  
    
    assertThat(p, parse("( balanced_token_seq )"));
  }

  public void attribute_argument_clause_real() {
    p.setRootRule(g.attribute_argument_clause);
    
    assertThat(p, parse("( foo )"));
  }

  @Test
  public void balanced_token_seq() {
    p.setRootRule(g.balanced_token_seq);
    g.balanced_token.mock();  
    
    assertThat(p, parse("balanced_token"));        
    assertThat(p, parse("balanced_token balanced_token"));        
    assertThat(p, parse("balanced_token balanced_token balanced_token"));
  }

  @Test
  public void balanced_token_seq_real() {
    p.setRootRule(g.balanced_token_seq);
    
    assertThat(p, parse("[ ( foo ) { } ( bar ) ]"));        
  }
  
  @Test
  public void balanced_token() {
    p.setRootRule(g.balanced_token);
    g.balanced_token_seq.mock();    
    
    assertThat(p, parse("foo"));        
    assertThat(p, parse("( balanced_token_seq )"));        
    assertThat(p, parse("[ balanced_token_seq ]"));        
    assertThat(p, parse("{ balanced_token_seq }"));
  }  
  
  @Test
  public void balanced_token_real() {
    p.setRootRule(g.balanced_token);    
    
    assertThat(p, parse("[ foo ]"));        
    assertThat(p, parse("{ foo }"));
    assertThat(p, parse("( foo )"));
    assertThat(p, parse("( ( foo ) ( bar ) )"));
    assertThat(p, parse("[ ( foo ) { } ( bar ) ]"));
  }  
  
}
