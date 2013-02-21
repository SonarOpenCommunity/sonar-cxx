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
 * @author jmecosta
 */
public class LamdaExpressionsTest {
  
  Parser<CxxGrammar> p = CxxParser.create(mock(SquidAstVisitorContext.class));
  CxxGrammar g = p.getGrammar();
  
  @Test
  public void lambdaExpression() {
    p.setRootRule(g.lambdaExpression);
    
    g.lambdaIntroducer.mock(); 
    g.lambdaDeclarator.mock();
    g.compoundStatement.mock();
    
    assertThat(p, parse("lambdaIntroducer compoundStatement"));
    assertThat(p, parse("lambdaIntroducer lambdaDeclarator compoundStatement"));
  }
  
  @Test
  public void lambdaExpression_reallife() {
    p.setRootRule(g.lambdaExpression);

    assertThat(p, parse("[] ( ) { }"));
    assertThat(p, parse("[] (int n) { }"));
    assertThat(p, parse("[&] ( ) { }"));
    assertThat(p, parse("[&foo] (int n) { }"));    
    assertThat(p, parse("[=] (int n) { }"));    
    assertThat(p, parse("[=,&foo] (int n) { }"));   
    assertThat(p, parse("[&foo1,&foo2,&foo3] (int n, int y, int z) { }"));    
    assertThat(p, parse("[] () throw () { }"));
    assertThat(p, parse("[] () -> int { return 1; }"));     
    assertThat(p, parse("[] (const string& addr) { return addr.find( \".org\" ) != string::npos; }"));   
    assertThat(p, parse("[this] () { cout << _x; }"));
    // function pointers c++11, TODO: make this work
    // assertThat(p, parse("[] () -> { return 2; }"));
  }
  
  @Test
  public void lambdaIntroducer() {
    p.setRootRule(g.lambdaIntroducer);
    g.lambdaCapture.mock();
    
    assertThat(p, parse("[]"));        
    assertThat(p, parse("[lambdaCapture]"));    
  }    

  @Test
  public void lambdaIntroducer_reallife() {
    p.setRootRule(g.lambdaIntroducer);
    
    assertThat(p, parse("[&]"));
    assertThat(p, parse("[=]"));
    assertThat(p, parse("[bar]"));
    assertThat(p, parse("[this]"));    
    assertThat(p, parse("[&foo]"));
    assertThat(p, parse("[=,&foo]"));    
  }      
    
  @Test
  public void lambdaCapture() {
    p.setRootRule(g.lambdaCapture);
    g.captureDefault.mock();
    g.captureList.mock();    
    
    assertThat(p, parse("captureDefault"));
    assertThat(p, parse("captureList"));
    assertThat(p, parse("captureDefault , captureList"));
  }    

  @Test
  public void captureDefault() {
    p.setRootRule(g.captureDefault);
    
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
  public void captureList() {
    p.setRootRule(g.captureList);
    g.capture.mock();
    
    assertThat(p, parse("capture")); // or 1, optional out        
    assertThat(p, parse("capture ...")); // or 1, optional in
    assertThat(p, parse("capture , capture")); // or 1, optional out            
    assertThat(p, parse("capture , capture ...")); // or 1, optional in                
  }      

  @Test
  public void lambdaDeclarator() {
    p.setRootRule(g.lambdaDeclarator);
    g.parameterDeclarationClause.mock();
    g.exceptionSpecification.mock();
    g.attributeSpecifierSeq.mock();
    g.trailingReturnType.mock();
    
    assertThat(p, parse("( parameterDeclarationClause ) ")); // all opt out
    assertThat(p, parse("( parameterDeclarationClause ) mutable")); // mutable in
    assertThat(p, parse("( parameterDeclarationClause ) exceptionSpecification")); // exceptionSpecification in
    assertThat(p, parse("( parameterDeclarationClause ) attributeSpecifierSeq")); // attributeSpecifierSeq in
    assertThat(p, parse("( parameterDeclarationClause ) trailingReturnType")); // trailingReturnType in
    assertThat(p, parse("( parameterDeclarationClause ) mutable exceptionSpecification")); // complex 1    
    assertThat(p, parse("( parameterDeclarationClause ) mutable exceptionSpecification attributeSpecifierSeq")); // complex 2
    assertThat(p, parse("( parameterDeclarationClause ) mutable exceptionSpecification attributeSpecifierSeq trailingReturnType")); // complex 3   
    assertThat(p, parse("( parameterDeclarationClause ) exceptionSpecification attributeSpecifierSeq")); // complex 4
    assertThat(p, parse("( parameterDeclarationClause ) exceptionSpecification attributeSpecifierSeq trailingReturnType")); // complex 5
    assertThat(p, parse("( parameterDeclarationClause ) attributeSpecifierSeq trailingReturnType")); // complex 6
  }
}
