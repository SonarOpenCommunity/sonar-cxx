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
package org.sonar.cxx.preprocessor;

import com.google.common.collect.Lists;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Preprocessor;
import com.sonar.sslr.api.PreprocessorAction;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.TokenType;
import com.sonar.sslr.api.Trivia;
import com.sonar.sslr.impl.Lexer;
import com.sonar.sslr.impl.Parser;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.api.CxxTokenType;
import org.sonar.cxx.lexer.CxxLexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.sonar.sslr.api.GenericTokenType.IDENTIFIER;

public class CxxPreprocessor extends Preprocessor {
  static class MismatchException extends Exception {
    private String why;
    MismatchException(String why){
      this.why = why;
    }
    
    public String toString() { return why; }
  }
  
  class Macro {
    public Macro(String name, List<Token> params, List<Token> body) {
      this.name = name;
      this.params = params;
      this.body = body;
    }
    
    public String toString(){
      return name
        + (params.size() == 0 ? "" : "(" + serialize(params) + ")")
        + " -> '" + serialize(body) + "'";
    }

    public String name;
    public List<Token> params;
    public List<Token> body;
  }
  
  public static final Logger LOG = LoggerFactory.getLogger("CxxPreprocessor");
  private CppGrammar grammar = new CppGrammar();
  private Parser<CppGrammar> defineParser = null;
  private Map<String, Macro> macros = new HashMap<String, Macro>();

  public CxxPreprocessor() {
    this(new CxxConfiguration());
  }
  
  public CxxPreprocessor(CxxConfiguration conf) {
    CxxConfiguration defineLexerConf = new CxxConfiguration();
    defineLexerConf.setPreprocessorChannelEnabled(false);
    Lexer cppLexer = CxxLexer.create(defineLexerConf);
    defineParser = Parser.builder(grammar).withLexer(cppLexer).build();
    
    // parse the configured defines and store into the macro library
    for(String define: conf.getDefines()){
      LOG.debug("parsing external macro: '{}'", define);
      Macro macro = parseMacroDefinition(define);
      if(macro != null){
        LOG.info("storing external macro: " + macro);
        macros.put(macro.name, macro);
      }
    }
  }

  @Override
  public PreprocessorAction process(List<Token> tokens) {
    Token token = tokens.get(0);
    TokenType ttype = token.getType();

    if (ttype == CxxTokenType.PREPROCESSOR) {

      // For now, we just ignore all preprocessor directives except the defines
      // and strip them from the stream

      return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
    } else if (ttype == CxxTokenType.PREPROCESSOR_DEFINE) {

      // Here we have a define directive. Parse it and store the result in a dictionary.
      // TODO: Open question: how to ensure that (most?) macros are known before hitting
      // instances of them in the code?
      // Alternative 1: we behave like a compiler and follow (recursively) all the the #includes
      // Alternative 2: we are lazy, expand all stuff what we know, and let the user provide
      // the expansions for all the macros we dont.

      //the parse routine cannot handle the '#define' prefix
      String definition = token.getValue().substring(7);

      Macro macro = parseMacroDefinition(definition);
      if(macro != null){
        LOG.trace("storing macro: " + macro);
        macros.put(macro.name, macro);
      }

      return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
    } else if(ttype != CxxTokenType.STRING && ttype != CxxTokenType.NUMBER){

      //
      // Every identifier and every keyword can be a macro instance.
      // Pipe the resulting string through a lexer to create proper Tokens
      // and to expand recursively all macros which may be in there.
      //

      Macro macro = macros.get(token.getValue());
      if (macro != null) {
        List<Token> replTokens = null;
        int tokensConsumed = 0;
        List<Token> arguments = new ArrayList<Token>();

        if (macro.params.size() == 0) {
          tokensConsumed = 1;
          replTokens = stripEOF(CxxLexer.create(this).lex(serialize(macro.body)));
        }
        else {
          int tokensConsumedMatchingParams = matchArguments(tokens.subList(1, tokens.size()), arguments);
          if (tokensConsumedMatchingParams > 0 && macro.params.size() == arguments.size()) {
            tokensConsumed = tokensConsumedMatchingParams + 1;
            String replacement = replaceParams(macro.body, macro.params, arguments);

            LOG.trace("lexing macro body: '{}'", replacement);

            replTokens = stripEOF(CxxLexer.create(this).lex(replacement));
          }
        }

        if (tokensConsumed > 0) {
          replTokens = reallocate(replTokens, token);

          LOG.trace("replacing '" + token.getValue()
            + (arguments.size() == 0
                ? ""
                : "(" + serialize(arguments) + ")") + "' --> '" + serialize(replTokens) + "'");

          return new PreprocessorAction(
              tokensConsumed,
              Lists.newArrayList(Trivia.createSkippedText(token)), // TODO: should we put all replaced tokens herein?
              replTokens);
        }
      }
    }

    return PreprocessorAction.NO_OPERATION;
  }

  private List<Token> stripEOF(List<Token> tokens) {
    return tokens.subList(0, tokens.size() - 1);
  }

  private String serialize(List<Token> tokens) {
    List<String> values = new LinkedList<String>();
    for (Token t : tokens) {
      values.add(t.getValue());
    }
    return StringUtils.join(values, " ");
  }

  private int matchArguments(List<Token> tokens, List<Token> arguments) {
    List<Token> rest = tokens;
    try{
      rest = match(rest, "(");
      do{
        rest = matchArgument(rest, arguments);
        try{
          rest = match(rest, ",");
        }
        catch(MismatchException me){
          break;
        }
      }
      while(true);
      
      rest = match(rest, ")");
    } catch(MismatchException me){
      LOG.error(me.toString());
      return 0;
    }
    
    return tokens.size() - rest.size();
  }
  
  List<Token> match(List<Token> tokens, String str) throws MismatchException {
    if(!tokens.get(0).getValue().equals(str)){
      throw new MismatchException("Mismatch: expected '" + str + "' got: '"
                                  + tokens.get(0).getValue() + "'");
    }
    return tokens.subList(1, tokens.size());
  }

  List<Token> matchArgument(List<Token> tokens, List<Token> arguments) throws MismatchException {
    int nestingLevel = 0;
    int tokensConsumed = 0;
    int noTokens = tokens.size();
    Token firstToken = tokens.get(0);
    Token currToken = firstToken;
    String curr = currToken.getValue();
    List<Token> matchedTokens = new LinkedList<Token>();
    
    while (true){
      if(nestingLevel == 0 && (",".equals(curr) || ")".equals(curr))){
        if(tokensConsumed > 0){
          arguments.add(Token.builder()
                       .setLine(firstToken.getLine())
                       .setColumn(firstToken.getColumn())
                       .setURI(firstToken.getURI())
                       .setValueAndOriginalValue(serialize(matchedTokens))
                       .setType(CxxTokenType.STRING)
                       .build());
        }
        return tokens.subList(tokensConsumed, noTokens);
      }
      
      if (curr.equals("(")) {
        nestingLevel++;
      }
      if (curr.equals(")")) {
        nestingLevel--;
      }
      
      tokensConsumed++;
      if(tokensConsumed == noTokens){
        throw new MismatchException("reached the end of the stream while matching a macro argument");
      }

      matchedTokens.add(currToken);
      currToken = tokens.get(tokensConsumed);
      curr = currToken.getValue();
    }
  }
    
  List<Token> getParams(AstNode macroAst) {
    List<Token> params = new ArrayList<Token>();
    AstNode paramAst = macroAst.findFirstChild(defineParser.getGrammar().identifier_list);
    if (paramAst != null) {
      for (AstNode node : paramAst.findDirectChildren(IDENTIFIER)) {
        params.add(node.getToken());
      }
    }

    return params;
  }

  String replaceParams(List<Token> body, List<Token>  parameters, List<Token> arguments) {
    // Replace all parameters by according arguments
    // "Stringify" the argument if the according parameter is preceded by an #

    if (body.size() == 0) {
      return "";
    }

    List<String> defParamValues = new ArrayList<String>();
    for (Token t : parameters) {
      defParamValues.add(t.getValue());
    }

    List<Token> newTokens = new ArrayList<Token>();

    for (int i = 0; i < body.size(); ++i) {
      Token curr = body.get(i);
      int index = defParamValues.indexOf(curr.getValue());
      if (index != -1) {
        Token replacement = arguments.get(index);
        String newValue = replacement.getValue();
        if (i > 0 && body.get(i - 1).getValue().equals("#")) {
          newTokens.remove(newTokens.size() - 1);
          newValue = encloseWithQuotes(quote(newValue));
        }
        newTokens.add(Token.builder()
            .setLine(replacement.getLine())
            .setColumn(replacement.getColumn())
            .setURI(replacement.getURI())
            .setValueAndOriginalValue(newValue)
            .setType(replacement.getType())
            .setGeneratedCode(true)
            .build());
      }
      else {
        newTokens.add(curr);
      }
    }

    return serialize(newTokens);
  }

  private String quote(String str) {
    return StringUtils.replaceEach(str, new String[] {"\\", "\""}, new String[] {"\\\\", "\\\""});
  }

  private String encloseWithQuotes(String str) {
    return "\"" + str + "\"";
  }

  private List<Token> reallocate(List<Token> tokens, Token token) {
    List<Token> reallocated = new LinkedList<Token>();
    for (Token t : tokens) {
      reallocated.add(Token.builder()
          .setLine(token.getLine())
          .setColumn(token.getColumn())
          .setURI(token.getURI())
          .setValueAndOriginalValue(t.getValue())
          .setType(t.getType())
          .setGeneratedCode(true)
          .build());
    }

    return reallocated;
  }
  
  private Macro parseMacroDefinition(String macroDef){
    Macro macro = null;
    
    AstNode ast = defineParser.parse(macroDef);
    AstNode replList = ast.findFirstChild(defineParser.getGrammar().replacement_list);
      
    if (!replList.getTokenValue().equals("")) {
      String macroName = ast.getFirstChild().getTokenValue();
      List<Token> macroBody = replList.getTokens().subList(0, replList.getTokens().size() - 1);
      List<Token> macroParams = getParams(ast);
      
      macro = new Macro(macroName, macroParams, macroBody);
    }
    return macro;
  }
}
