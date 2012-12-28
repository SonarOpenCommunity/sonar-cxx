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
import com.sonar.sslr.squid.SquidAstVisitorContext;
import com.sonar.sslr.squid.SquidAstVisitorContextImpl;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.api.CxxGrammar;
import org.sonar.cxx.lexer.CxxLexer;
import org.sonar.squid.api.SourceProject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static com.sonar.sslr.api.GenericTokenType.IDENTIFIER;
import static org.sonar.cxx.api.CppPunctuator.LT;
import static org.sonar.cxx.api.CxxTokenType.NUMBER;
import static org.sonar.cxx.api.CxxTokenType.PREPROCESSOR;
import static org.sonar.cxx.api.CxxTokenType.PREPROCESSOR_DEFINE;
import static org.sonar.cxx.api.CxxTokenType.PREPROCESSOR_INCLUDE;
import static org.sonar.cxx.api.CxxTokenType.PREPROCESSOR_IFDEF;
import static org.sonar.cxx.api.CxxTokenType.PREPROCESSOR_IFNDEF;
import static org.sonar.cxx.api.CxxTokenType.PREPROCESSOR_IF;
import static org.sonar.cxx.api.CxxTokenType.PREPROCESSOR_ELSE;
import static org.sonar.cxx.api.CxxTokenType.PREPROCESSOR_ENDIF;
import static org.sonar.cxx.api.CxxTokenType.STRING;

public class CxxPreprocessor extends Preprocessor {
  static class MismatchException extends Exception {
    private String why;

    MismatchException(String why) {
      this.why = why;
    }

    public String toString() {
      return why;
    }
  }

  class Macro {
    public Macro(String name, List<Token> params, List<Token> body) {
      this.name = name;
      this.params = params;
      this.body = body;
    }

    public String toString(){
      return name
        + (params == null ? "" : "(" + serialize(params, ", ") + ")")
        + " -> '" + serialize(body) + "'";
    }

    public String name;
    public List<Token> params;
    public List<Token> body;
  }

  public static final Logger LOG = LoggerFactory.getLogger("CxxPreprocessor");
  private CppGrammar grammar = new CppGrammar();
  private Parser<CppGrammar> pplineParser = null;
  private Map<String, Macro> macros = new HashMap<String, Macro>();
  private Map<String, List<String>> file2macros = new HashMap<String, List<String>>();
  private Set<File> analysedFiles = new HashSet<File>();
  private SourceCodeProvider codeProvider = new SourceCodeProvider();
  private SquidAstVisitorContext context;
  private Stack<File> headersUnderAnalysis = new Stack<File>();
  boolean skipping = false;
  int nestedIfdefs = 0;
  
  public CxxPreprocessor() {
    this(new CxxConfiguration(),
         new SquidAstVisitorContextImpl<CxxGrammar>(new SourceProject("Cxx Project")),
         new SourceCodeProvider()
      );
  }

  public CxxPreprocessor(CxxConfiguration conf) {
    this(conf,
         new SquidAstVisitorContextImpl<CxxGrammar>(new SourceProject("Cxx Project")),
         new SourceCodeProvider()
      );
  }

  public CxxPreprocessor(CxxConfiguration conf, SquidAstVisitorContext context) {
    this(conf, context, new SourceCodeProvider());
  }

  public CxxPreprocessor(CxxConfiguration conf,
                         SquidAstVisitorContext context,
                         SourceCodeProvider sourceCodeProvider) {
    this.context = context;
    codeProvider = sourceCodeProvider;
    codeProvider.setIncludeRoots(conf.getIncludeDirectories(), conf.getBaseDir());
    
    Lexer cppLexer = CppLexer.create(conf);
    pplineParser = Parser.builder(grammar).withLexer(cppLexer).build();

    // parse the configured defines and store into the macro library
    for(String define: conf.getDefines()){
      LOG.debug("parsing external macro: '{}'", define);
      Macro macro = parseMacroDefinition("#define " + define);
      if (macro != null) {
        LOG.info("storing external macro: '{}'", macro);
        macros.put(macro.name, macro);
      }
    }
  }

  @Override
  public PreprocessorAction process(List<Token> tokens) {
    Token token = tokens.get(0);
    TokenType ttype = token.getType();
    File file = getFileUnderAnalysis();
    String filePath = file == null? token.getURI().toString() : file.getAbsolutePath();
    
    if (ttype == PREPROCESSOR_IFDEF || ttype == PREPROCESSOR_IFNDEF) {
      if(skipping){
        nestedIfdefs++;
      }
      else{
        Macro macro = macros.get(getMacro(token));
        if (ttype == PREPROCESSOR_IFDEF && macro == null
            ||
            ttype == PREPROCESSOR_IFNDEF && macro != null) {
          LOG.debug("[{}:{}]: '{}' evaluated to false, skipping tokens that follow",
                    new Object[]{filePath, token.getLine(), token.getValue()});
          skipping = true;
        }
      }
      
      return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());

    } else if (ttype == PREPROCESSOR_IF) {
      if(skipping){
        nestedIfdefs++;
      }
      else{
        // TODO: hardcoded to 'false' right now; implement the parsing and evaluating
        // of the expression
        LOG.debug("[{}:{}]: '{}' evaluated to false, skipping tokens that follow",
                  new Object[]{filePath, token.getLine(), token.getValue()});
        skipping = true;
      }
      
      return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
      
    } else if (ttype == PREPROCESSOR_ELSE) {
      if(nestedIfdefs == 0){
        if(skipping){
          LOG.debug("[{}:{}]: #else, returning to non-skipping mode", filePath, token.getLine());
        }
        else{
          LOG.debug("[{}:{}]: skipping tokens inside the #else", filePath, token.getLine());
        }
        
        skipping = !skipping;
      }
      return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
    } else if (ttype == PREPROCESSOR_ENDIF) {
      if(nestedIfdefs > 0){
        nestedIfdefs--;
      }
      else{
        if(skipping){
          LOG.debug("[{}:{}]: #endif, returning to non-skipping mode", filePath,token.getLine());
        }
        skipping = false;
      }
      return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
    }
    
    if(inSkippingMode()){
      return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
    }
    
    if (ttype == PREPROCESSOR) {
      
      // Ignore all other preprocessor directives (which are not handled explicitly) 
      // and strip them from the stream

      return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
    } else if (ttype == PREPROCESSOR_INCLUDE) {

      //
      // Included files have to be scanned with the (only) goal of gathering macros.
      // This is done as follows:
      // a) parse the preprocessor line using the preprocessor line parser
      // b) if not done yet, try to find the according source code
      // c) if found, feed it into a special lexer, which calls back only if it finds relevant
      //    preprocessor directives (currently: include's and define's)
      
      File includedFile = findIncludedFile(token.getValue());
      if(includedFile == null){
        LOG.warn("[{}:{}]: cannot find the sources for '{}'", new Object[]{filePath, token.getLine(), token.getValue()});
      }
      else if(!analysedFiles.contains(includedFile)) {
        analysedFiles.add(includedFile.getAbsoluteFile());
        LOG.debug("[{}:{}]: processing {}, resolved to file '{}'",
            new Object[]{filePath, token.getLine(), token.getValue(), includedFile.getAbsolutePath()});
        headersUnderAnalysis.push(includedFile);
        try{
          IncludeLexer.create(this).lex(codeProvider.getSourceCode(includedFile));
        } finally {
          headersUnderAnalysis.pop();
         }
      }
      else{
        LOG.debug("[{}:{}]: skipping already included file '{}'", new Object[]{filePath, token.getLine(), includedFile});
      }      

      return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
    } else if (ttype == PREPROCESSOR_DEFINE) {

      // Here we have a define directive. Parse it and store the result in a dictionary.

      Macro macro = parseMacroDefinition(token.getValue());
      if (macro != null) {
        LOG.debug("[{}:{}]: storing macro: '{}'",
                  new Object[]{filePath, token.getLine(), macro});
        macros.put(macro.name, macro);
        
        List<String> macronames = file2macros.get(filePath);
        if(macronames == null){
          macronames = new LinkedList<String>();
          file2macros.put(filePath, macronames);
        }
        macronames.add(macro.name);
      }

      return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
    } else if(ttype != STRING && ttype != NUMBER){

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

        if (macro.params == null) {
          tokensConsumed = 1;
          replTokens = expandMacro(macro.name, serialize(macro.body));
        }
        else {
          int tokensConsumedMatchingArgs = matchArguments(tokens.subList(1, tokens.size()), arguments);
          if (tokensConsumedMatchingArgs > 0 && macro.params.size() == arguments.size()) {
            tokensConsumed = tokensConsumedMatchingArgs + 1;
            String replacement = replaceParams(macro.body, macro.params, arguments);

            LOG.debug("[{}:{}]: lexing macro body: '{}'",
                      new Object[]{filePath, token.getLine(), replacement});

            replTokens = expandMacro(macro.name, replacement);
          }
        }

        if (tokensConsumed > 0) {
          replTokens = reallocate(replTokens, token);

          LOG.debug("[{}:{}]: replacing '" + token.getValue()
                    + (arguments.size() == 0
                       ? ""
                       : "(" + serialize(arguments, ", ") + ")") + "' -> '" + serialize(replTokens) + "'",
                    filePath, token.getLine());

          return new PreprocessorAction(
              tokensConsumed,
              Lists.newArrayList(Trivia.createSkippedText(token)), // TODO: should we put all replaced tokens herein?
              replTokens);
        }
      }
    }

    return PreprocessorAction.NO_OPERATION;
  }
  
  public void beginPreprocessing(File file){
    // The following solves a problem with preprocessor guards
    // (like #IFNDEF FILE_HH
    //       #DEFINE FILE_HH
    //       ...
    //       #ENDIF)
    // We dont wanna have them work on files which we want to analyse.
    // So, just remove all macros known to be from that file
    // (if already have collected any)
    
    if(file != null){
      List<String> macrosToDelete = file2macros.get(file.getAbsolutePath());
      if(macrosToDelete != null){
        for(String macroname: macrosToDelete){
          macros.remove(macroname);
        }
        file2macros.remove(file.getAbsolutePath());
      }
    }
  }
  
  private List<Token> expandMacro(String macroName, String macroExpression) {
    // C++ standard 16.3.4/2 Macro Replacement - Rescanning and further replacement
    List<Token> tokens = null;
    Macro macro = macros.remove(macroName);
    try{
      tokens = stripEOF(CxxLexer.create(this).lex(macroExpression));
    }
    finally{
      macros.put(macroName, macro);
    }
    return tokens;
  }

  private List<Token> stripEOF(List<Token> tokens) {
    return tokens.subList(0, tokens.size() - 1);
  }
  
  private String serialize(List<Token> tokens) {
    return serialize(tokens, " ");
  }
  
  private String serialize(List<Token> tokens, String spacer) {
    List<String> values = new LinkedList<String>();
    for (Token t : tokens) {
      values.add(t.getValue());
    }
    return StringUtils.join(values, spacer);
  }

  private int matchArguments(List<Token> tokens, List<Token> arguments) {
    List<Token> rest = tokens;
    try{
      rest = match(rest, "(");
    } catch(MismatchException me){
      return 0;
    }

    try{
      do{
        rest = matchArgument(rest, arguments);
        try{
          rest = match(rest, ",");
        } catch (MismatchException me) {
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
                       .setType(STRING)
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
    AstNode ast = pplineParser.parse(macroDef);
    AstNode nameNode = ast.findFirstChild(pplineParser.getGrammar().pp_token);
    String macroName = nameNode.getTokenValue();
    AstNode afterName = nameNode.nextSibling();

    List<Token> macroParams = null;
    if(afterName.getTokenValue().equals("(")){
      macroParams = getParams(ast.findFirstChild(pplineParser.getGrammar().identifier_list));
    }
    
    AstNode replList = ast.findFirstChild(pplineParser.getGrammar().replacement_list);
    List<Token> macroBody = replList.getTokens().subList(0, replList.getTokens().size() - 1);
    
    return new Macro(macroName, macroParams, macroBody);
  }

  List<Token> getParams(AstNode identListAst) {
    List<Token> params = new ArrayList<Token>();
    if (identListAst != null) {
      for (AstNode node : identListAst.findDirectChildren(IDENTIFIER)) {
        params.add(node.getToken());
      }
    }
    
    return params;
  }

  File findIncludedFile(String includeLine){
    String fileName = null;
    File includedFile = null;
    boolean quoted = false;
    
    AstNode ast = pplineParser.parse(includeLine);
    AstNode includedString = ast.findFirstChild(STRING);
    if(includedString != null){
      fileName = stripQuotes(includedString.getTokenValue());
      quoted = true;
    }
    else {
      AstNode node = ast.findFirstChild(LT).nextSibling();
      StringBuilder sb = new StringBuilder();
      while(true){
        String value = node.getTokenValue();
        if(value.equals(">"))
          break;
        sb.append(value);
        node = node.nextSibling();
      }

      fileName = sb.toString();
    }

    if(fileName != null){
      File file = getFileUnderAnalysis();
      String dir = file == null ? "" : file.getParent();
      includedFile = codeProvider.getSourceCodeFile(fileName, dir,quoted);
    }
    
    return includedFile;
  }

  String getMacro(Token ifdefLine){
    AstNode ast = pplineParser.parse(ifdefLine.getValue());
    return ast.findFirstChild(IDENTIFIER).getTokenValue();
  }

  String stripQuotes(String str){
    return str.substring(1, str.length()-1);
  }

  private File getFileUnderAnalysis(){
    if(headersUnderAnalysis.isEmpty()){
      return context.getFile();
    } else {
      return headersUnderAnalysis.peek();
    }
  }
  
  private boolean inSkippingMode(){
    return skipping;
  }
}
  
