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
package org.sonar.cxx.preprocessor;

import com.google.common.collect.Lists;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Preprocessor;
import com.sonar.sslr.api.PreprocessorAction;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.TokenType;
import com.sonar.sslr.api.Trivia;
import com.sonar.sslr.impl.Parser;
import com.sonar.sslr.squid.SquidAstVisitorContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.cxx.CxxConfiguration;
//import org.sonar.cxx.api.CxxGrammar;
import com.sonar.sslr.api.Grammar;

import org.sonar.cxx.lexer.CxxLexer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Map;

import static com.sonar.sslr.api.GenericTokenType.EOF;
import static com.sonar.sslr.api.GenericTokenType.IDENTIFIER;
import static org.sonar.cxx.api.CppKeyword.IFDEF;
import static org.sonar.cxx.api.CppKeyword.IFNDEF;
import static org.sonar.cxx.api.CppPunctuator.LT;
import static org.sonar.cxx.api.CxxTokenType.NUMBER;
import static org.sonar.cxx.api.CxxTokenType.PREPROCESSOR;
import static org.sonar.cxx.api.CxxTokenType.STRING;
import static org.sonar.cxx.api.CxxTokenType.WS;

public class CxxPreprocessor extends Preprocessor {
  private class State {
    public boolean skipping;
    public int nestedIfdefs;
    public File includeUnderAnalysis;

    public State(File includeUnderAnalysis) {
      reset();
      this.includeUnderAnalysis = includeUnderAnalysis;
    }

    public final void reset() {
      skipping = false;
      nestedIfdefs = 0;
      includeUnderAnalysis = null;
    }
  }

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
    public Macro(String name, List<Token> params, List<Token> body, boolean variadic) {
      this.name = name;
      this.params = params;
      this.body = body;
      this.isVariadic = variadic;
    }

    public String toString() {
      return name
        + (params == null ? "" : "(" + serialize(params, ", ") + ")")
        + " -> '" + serialize(body) + "'";
    }

    public boolean checkArgumentsCount(int count) {
      return isVariadic == true
        ? count >= params.size() - 1
        : count == params.size();
    }

    public String name;
    public List<Token> params;
    public List<Token> body;
    public boolean isVariadic;
  }

  private static final Logger LOG = LoggerFactory.getLogger("CxxPreprocessor");
  private Parser<Grammar> pplineParser = null;
  private MapChain<String, Macro> macros = new MapChain<String, Macro>();
  private Set<File> analysedFiles = new HashSet<File>();
  private SourceCodeProvider codeProvider = new SourceCodeProvider();
  private SquidAstVisitorContext<Grammar> context;
  private ExpressionEvaluator ifExprEvaluator;

  // state which is not shared between files
  private State state = new State(null);
  private Stack<State> stateStack = new Stack<State>();

  public CxxPreprocessor(SquidAstVisitorContext<Grammar> context) {
    this(context, new CxxConfiguration());
  }

  public CxxPreprocessor(SquidAstVisitorContext<Grammar> context, CxxConfiguration conf) {
    this(context, conf, new SourceCodeProvider());
  }

  public CxxPreprocessor(SquidAstVisitorContext<Grammar> context,
      CxxConfiguration conf,
      SourceCodeProvider sourceCodeProvider) {
    this.context = context;
    this.ifExprEvaluator = new ExpressionEvaluator(conf, this);

    codeProvider = sourceCodeProvider;
    codeProvider.setIncludeRoots(conf.getIncludeDirectories(), conf.getBaseDir());

    pplineParser = CppParser.create(conf);

    // parse the configured defines and store into the macro library
    for (String define : conf.getDefines()) {
      LOG.debug("parsing external macro: '{}'", define);
      if (!define.equals("")) {
        Macro macro = parseMacroDefinition("#define " + define);
        if (macro != null) {
          LOG.info("storing external macro: '{}'", macro);
          macros.putHighPrio(macro.name, macro);
        }
      }
    }

    // set standard macros
    for (Map.Entry<String, String> entry: StandardDefinitions.macros().entrySet()) {
      Token bodyToken;
      try{
        bodyToken = Token.builder()
          .setLine(1)
          .setColumn(0)
          .setURI(new java.net.URI(""))
          .setValueAndOriginalValue(entry.getValue())
          .setType(STRING)
          .build();
      } catch (java.net.URISyntaxException e) {
        throw new RuntimeException(e);
      }
      
      macros.putHighPrio(entry.getKey(), new Macro(entry.getKey(), null, Lists.newArrayList(bodyToken), false));
    }
  }

  @Override
  public PreprocessorAction process(List<Token> tokens) {
    Token token = tokens.get(0);
    TokenType ttype = token.getType();
    File file = getFileUnderAnalysis();
    String filePath = file == null ? token.getURI().toString() : file.getAbsolutePath();

    if (ttype == PREPROCESSOR) {

      AstNode lineAst = null;
      try {
        lineAst = pplineParser.parse(token.getValue()).getFirstChild();
      } catch (com.sonar.sslr.api.RecognitionException re) {
        LOG.warn("Cannot parse '{}', ignoring...", token.getValue());
        return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
      }

      String lineKind = lineAst.getName();

      if ("ifdefLine".equals(lineKind)) {
        return handleIfdefLine(lineAst, token, filePath);
      } else if ("elseLine".equals(lineKind)) {
        return handleElseLine(lineAst, token, filePath);
      } else if ("endifLine".equals(lineKind)) {
        return handleEndifLine(lineAst, token, filePath);
      } else if ("ifLine".equals(lineKind)) {
        return handleIfLine(lineAst, token, filePath);
      } else if ("elifLine".equals(lineKind)) {
        return handleElIfLine(lineAst, token, filePath);
      }

      if (inSkippingMode()) {
        return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
      }

      if ("defineLine".equals(lineKind)) {
        return handleDefineLine(lineAst, token, filePath);
      } else if ("includeLine".equals(lineKind)) {
        return handleIncludeLine(lineAst, token, filePath);
      } else if ("undefLine".equals(lineKind)) {
        return handleUndefLine(lineAst, token, filePath);
      }

      // Ignore all other preprocessor directives (which are not handled explicitly)
      // and strip them from the stream

      return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
    }

    if (ttype != EOF) {
      if (inSkippingMode()) {
        return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
      }

      if (ttype != STRING && ttype != NUMBER) {
        return handleIdentifiersAndKeywords(tokens, token, filePath);
      }
    }

    return PreprocessorAction.NO_OPERATION;
  }

  public void finishedPreprocessing(File file) {
    // From 16.3.5 "Scope of macro definitions":
    // A macro definition lasts (independent of block structure) until
    // a corresponding #undef directive is encountered or (if none
    // is encountered) until the end of the translation unit.

    LOG.debug("finished preprocessing '{}'", file);

    analysedFiles.clear();
    macros.clearLowPrio();
    state.reset();
  }

  public String valueOf(String macroname) {
    String result = null;
    Macro macro = macros.get(macroname);
    if (macro != null) {
      result = serialize(macro.body);
    }
    return result;
  }

  private PreprocessorAction handleIfdefLine(AstNode ast, Token token, String filename) {
    if (state.skipping) {
      state.nestedIfdefs++;
    }
    else {
      Macro macro = macros.get(getMacroName(ast));
      TokenType tokType = ast.getToken().getType();
      if ((tokType == IFDEF && macro == null) || (tokType == IFNDEF && macro != null)) {
        LOG.trace("[{}:{}]: '{}' evaluated to false, skipping tokens that follow",
            new Object[] {filename, token.getLine(), token.getValue()});
        state.skipping = true;
      }
    }

    return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
  }

  PreprocessorAction handleElseLine(AstNode ast, Token token, String filename) {
    if (state.nestedIfdefs == 0) {
      if (state.skipping) {
        LOG.trace("[{}:{}]: #else, returning to non-skipping mode", filename, token.getLine());
      }
      else {
        LOG.trace("[{}:{}]: skipping tokens inside the #else", filename, token.getLine());
      }

      state.skipping = !state.skipping;
    }

    return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
  }

  PreprocessorAction handleEndifLine(AstNode ast, Token token, String filename) {
    if (state.nestedIfdefs > 0) {
      state.nestedIfdefs--;
    }
    else {
      if (state.skipping) {
        LOG.trace("[{}:{}]: #endif, returning to non-skipping mode", filename, token.getLine());
      }
      state.skipping = false;
    }

    return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
  }

  PreprocessorAction handleIfLine(AstNode ast, Token token, String filename) {
    if (state.skipping) {
      state.nestedIfdefs++;
    }
    else {
      LOG.trace("[{}:{}]: handling #if line '{}'",
          new Object[] {filename, token.getLine(), token.getValue()});
      try {
        state.skipping = !ifExprEvaluator.eval(ast.getFirstDescendant(CppGrammar.constantExpression));
      } catch (EvaluationException e) {
        LOG.error("[{}:{}]: error evaluating the expression {} assume 'true' ...",
            new Object[] {filename, token.getLine(), token.getValue()});
        LOG.error(e.toString());
        state.skipping = false;
      }

      if (state.skipping) {
        LOG.trace("[{}:{}]: '{}' evaluated to false, skipping tokens that follow",
            new Object[] {filename, token.getLine(), token.getValue()});
      }
    }

    return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
  }

  PreprocessorAction handleElIfLine(AstNode ast, Token token, String filename) {
    // Handling of an elif line is similar to handling of an if line but
    // doesn't increase the nesting level
    if (state.nestedIfdefs == 0) {
      if (state.skipping) { //the preceeding clauses had been evaluated to false
        try {
          LOG.trace("[{}:{}]: handling #elif line '{}'",
                    new Object[] {filename, token.getLine(), token.getValue()});
          
          // *this* preprocessor instance is used for evaluation, too.
          // It *must not* be in skipping mode while evaluating expressions.
          state.skipping = false;

          state.skipping = !ifExprEvaluator.eval(ast.getFirstDescendant(CppGrammar.constantExpression));
        } catch (EvaluationException e) {
          LOG.error("[{}:{}]: error evaluating the expression {} assume 'true' ...",
                    new Object[] {filename, token.getLine(), token.getValue()});
          LOG.error(e.toString());
          state.skipping = false;
        }
        
        if (state.skipping) {
          LOG.trace("[{}:{}]: '{}' evaluated to false, skipping tokens that follow",
                    new Object[] {filename, token.getLine(), token.getValue()});
        }
      }
      else {
        state.skipping = !state.skipping;
        LOG.trace("[{}:{}]: skipping tokens inside the #elif", filename, token.getLine());
      }
    }

    return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
  }

  PreprocessorAction handleDefineLine(AstNode ast, Token token, String filename) {
    // Here we have a define directive. Parse it and store the result in a dictionary.

    Macro macro = parseMacroDefinition(ast);
    if (macro != null) {
      LOG.trace("[{}:{}]: storing macro: '{}'", new Object[] {filename, token.getLine(), macro});
      macros.putLowPrio(macro.name, macro);
    }

    return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
  }

  PreprocessorAction handleIncludeLine(AstNode ast, Token token, String filename) {
    //
    // Included files have to be scanned with the (only) goal of gathering macros.
    // This is done as follows:
    //    
    // a) pipe the body of the include directive through a lexer to properly expand
    //    all macros which may be in there.
    // b) extract the filename out of the include body and try to find it
    // c) if not done yet, process it using a special lexer, which calls back only
    //    if it finds relevant preprocessor directives (currently: include's and define's)
    
    File includedFile = findIncludedFile(ast, token, filename);
    if (includedFile == null) {
      LOG.warn("[{}:{}]: cannot find the sources for '{}'", new Object[] {filename, token.getLine(), token.getValue()});
    }
    else if (!analysedFiles.contains(includedFile)) {
      analysedFiles.add(includedFile.getAbsoluteFile());
      LOG.debug("[{}:{}]: processing {}, resolved to file '{}'",
                new Object[] {filename, token.getLine(), token.getValue(), includedFile.getAbsolutePath()});
      
      stateStack.push(state);
      state = new State(includedFile);
      
      try {
        IncludeLexer.create(this).lex(codeProvider.getSourceCode(includedFile));
      } finally {
        state = stateStack.pop();
      }
    }
    else {
      LOG.debug("[{}:{}]: skipping already included file '{}'", new Object[] {filename, token.getLine(), includedFile});
    }
    
    return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
  }

  PreprocessorAction handleUndefLine(AstNode ast, Token token, String filename) {
    String macroName = ast.getFirstDescendant(IDENTIFIER).getTokenValue();
    macros.removeLowPrio(macroName);
    return new PreprocessorAction(1, Lists.newArrayList(Trivia.createSkippedText(token)), new ArrayList<Token>());
  }

  PreprocessorAction handleIdentifiersAndKeywords(List<Token> tokens, Token curr, String filename) {
    //
    // Every identifier and every keyword can be a macro instance.
    // Pipe the resulting string through a lexer to create proper Tokens
    // and to expand recursively all macros which may be in there.
    //

    PreprocessorAction ppaction = PreprocessorAction.NO_OPERATION;
    Macro macro = macros.get(curr.getValue());
    if (macro != null) {
      List<Token> replTokens = new LinkedList<Token>();
      int tokensConsumed = 0;
      List<Token> arguments = new ArrayList<Token>();

      if (macro.params == null) {
        tokensConsumed = 1;
        replTokens = expandMacro(macro.name, serialize(evaluateHashhashOperators(macro.body)));
      }
      else {
        int tokensConsumedMatchingArgs = expandFunctionLikeMacro(macro.name,
            tokens.subList(1, tokens.size()),
            replTokens);
        if (tokensConsumedMatchingArgs > 0) {
          tokensConsumed = 1 + tokensConsumedMatchingArgs;
        }
      }

      if (tokensConsumed > 0) {
        replTokens = reallocate(replTokens, curr);

        LOG.trace("[{}:{}]: replacing '" + curr.getValue()
          + (arguments.isEmpty()
              ? ""
              : "(" + serialize(arguments, ", ") + ")") + "' -> '" + serialize(replTokens) + "'",
            filename, curr.getLine());

        ppaction = new PreprocessorAction(
            tokensConsumed,
            Lists.newArrayList(Trivia.createSkippedText(tokens.subList(0, tokensConsumed))),
            replTokens);
      }
    }

    return ppaction;
  }

  public String expandFunctionLikeMacro(String macroName, List<Token> restTokens) {
    List<Token> expansion = new LinkedList<Token>();
    expandFunctionLikeMacro(macroName, restTokens, expansion);
    return serialize(expansion);
  }

  private int expandFunctionLikeMacro(String macroName, List<Token> restTokens, List<Token> expansion) {
    List<Token> replTokens = null;
    List<Token> arguments = new ArrayList<Token>();
    int tokensConsumedMatchingArgs = matchArguments(restTokens, arguments);

    Macro macro = macros.get(macroName);
    if (macro != null && macro.checkArgumentsCount(arguments.size())) {
      if (arguments.size() > macro.params.size()) {
        //Group all arguments into the last one
        List<Token> vaargs = arguments.subList(macro.params.size() - 1, arguments.size());
        Token firstToken = vaargs.get(0);
        arguments = arguments.subList(0, macro.params.size() - 1);
        arguments.add(Token.builder()
            .setLine(firstToken.getLine())
            .setColumn(firstToken.getColumn())
            .setURI(firstToken.getURI())
            .setValueAndOriginalValue(serialize(vaargs, ","))
            .setType(STRING)
            .build());
      }
      replTokens = replaceParams(macro.body, macro.params, arguments);
      replTokens = evaluateHashhashOperators(replTokens);
      expansion.addAll(expandMacro(macro.name, serialize(replTokens)));
    }

    return tokensConsumedMatchingArgs;
  }

  private List<Token> expandMacro(String macroName, String macroExpression) {
    // C++ standard 16.3.4/2 Macro Replacement - Rescanning and further replacement
    List<Token> tokens = null;
    macros.disable(macroName);
    try {
      tokens = stripEOF(CxxLexer.create(this).lex(macroExpression));
    } finally {
      macros.enable(macroName);
    }
    return tokens;
  }

  private List<Token> stripEOF(List<Token> tokens) {
    if (tokens.get(tokens.size() - 1).getType() == EOF){
      return tokens.subList(0, tokens.size() - 1);
    }
    else{
      return tokens;
    }
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
    try {
      rest = match(rest, "(");
    } catch (MismatchException me) {
      return 0;
    }

    try {
      do {
        rest = matchArgument(rest, arguments);
        try {
          rest = match(rest, ",");
        } catch (MismatchException me) {
          break;
        }
      } while (true);

      rest = match(rest, ")");
    } catch (MismatchException me) {
      LOG.error(me.toString());
      return 0;
    }

    return tokens.size() - rest.size();
  }

  private List<Token> match(List<Token> tokens, String str) throws MismatchException {
    if (!tokens.get(0).getValue().equals(str)) {
      throw new MismatchException("Mismatch: expected '" + str + "' got: '"
        + tokens.get(0).getValue() + "'");
    }
    return tokens.subList(1, tokens.size());
  }

  private List<Token> matchArgument(List<Token> tokens, List<Token> arguments) throws MismatchException {
    int nestingLevel = 0;
    int tokensConsumed = 0;
    int noTokens = tokens.size();
    Token firstToken = tokens.get(0);
    Token currToken = firstToken;
    String curr = currToken.getValue();
    List<Token> matchedTokens = new LinkedList<Token>();

    while (true) {
      if (nestingLevel == 0 && (",".equals(curr) || ")".equals(curr))) {
        if (tokensConsumed > 0) {
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
      } else if (curr.equals(")")) {
        nestingLevel--;
      }

      tokensConsumed++;
      if (tokensConsumed == noTokens) {
        throw new MismatchException("reached the end of the stream while matching a macro argument");
      }

      matchedTokens.add(currToken);
      currToken = tokens.get(tokensConsumed);
      curr = currToken.getValue();
    }
  }

  private List<Token> replaceParams(List<Token> body, List<Token> parameters, List<Token> arguments) {
    // Replace all parameters by according arguments
    // "Stringify" the argument if the according parameter is preceded by an #

    List<Token> newTokens = new ArrayList<Token>();
    if (!body.isEmpty()) {
      List<String> defParamValues = new ArrayList<String>();
      for (Token t : parameters) {
        defParamValues.add(t.getValue());
      }

      for (int i = 0; i < body.size(); ++i) {
        Token curr = body.get(i);
        int index = defParamValues.indexOf(curr.getValue());
        if (index == -1) {
          newTokens.add(curr);
        }
        else if (index < arguments.size()) {
          Token replacement = arguments.get(index);

          // TODO: maybe we should pipe the argument through the whole expansion
          // engine before doing the replacement
          // String newValue = serialize(expandMacro("", replacement.getValue()));

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
      }
    }

    return newTokens;
  }

  private List<Token> evaluateHashhashOperators(List<Token> tokens) {
    List<Token> newTokens = new ArrayList<Token>();

    Iterator<Token> it = tokens.iterator();
    while (it.hasNext()) {
      Token curr = it.next();
      if (curr.getValue().equals("##")) {
        Token pred = predConcatToken(newTokens);
        Token succ = succConcatToken(it);
        newTokens.add(Token.builder()
            .setLine(pred.getLine())
            .setColumn(pred.getColumn())
            .setURI(pred.getURI())
            .setValueAndOriginalValue(pred.getValue() + succ.getValue())
            .setType(pred.getType())
            .setGeneratedCode(true)
            .build());
      } else {
        newTokens.add(curr);
      }
    }

    return newTokens;
  }

  private Token predConcatToken(List<Token> tokens) {
    while (!tokens.isEmpty()) {
      Token last = tokens.remove(tokens.size() - 1);
      if (last.getType() != WS) {
        return last;
      }
    }
    return null;
  }

  private Token succConcatToken(Iterator<Token> it) {
    Token succ = null;
    while (it.hasNext()) {
      succ = it.next();
      if (!succ.getValue().equals("##") && succ.getType() != WS) {
        break;
      }
    }
    return succ;
  }

  private String quote(String str) {
    return StringUtils.replaceEach(str, new String[] {"\\", "\""}, new String[] {"\\\\", "\\\""});
  }

  private String encloseWithQuotes(String str) {
    return "\"" + str + "\"";
  }

  private List<Token> reallocate(List<Token> tokens, Token token) {
    List<Token> reallocated = new LinkedList<Token>();
    int currColumn = token.getColumn();
    for (Token t : tokens) {
      reallocated.add(Token.builder()
          .setLine(token.getLine())
          .setColumn(currColumn)
          .setURI(token.getURI())
          .setValueAndOriginalValue(t.getValue())
          .setType(t.getType())
          .setGeneratedCode(true)
          .build());
      currColumn += t.getValue().length() + 1;
    }

    return reallocated;
  }

  private Macro parseMacroDefinition(String macroDef) {
    return parseMacroDefinition(pplineParser.parse(macroDef)
        .getFirstDescendant(CppGrammar.defineLine));
  }

  private Macro parseMacroDefinition(AstNode defineLineAst) {
    AstNode ast = defineLineAst.getFirstChild();
    AstNode nameNode = ast.getFirstDescendant(CppGrammar.ppToken);
    String macroName = nameNode.getTokenValue();

    AstNode paramList = ast.getFirstDescendant(CppGrammar.parameterList);
    List<Token> macroParams = paramList == null
        ? ast.getName().equals("objectlikeMacroDefinition") ? null : new LinkedList<Token>()
        : getParams(paramList);

    AstNode vaargs = ast.getFirstDescendant(CppGrammar.variadicparameter);
    if (vaargs != null) {
        AstNode identifier = vaargs.getFirstChild(IDENTIFIER);
        macroParams.add(identifier == null
            ? Token.builder()
                .setLine(vaargs.getToken().getLine())
                .setColumn(vaargs.getToken().getColumn())
                .setURI(vaargs.getToken().getURI())
                .setValueAndOriginalValue("__VA_ARGS__")
                .setType(IDENTIFIER)
                .setGeneratedCode(true)
                .build()
            : identifier.getToken());
    }

    AstNode replList = ast.getFirstDescendant(CppGrammar.replacementList);
    List<Token> macroBody = replList == null
        ? new LinkedList<Token>()
        : replList.getTokens().subList(0, replList.getTokens().size() - 1);

    return new Macro(macroName, macroParams, macroBody, vaargs != null);
  }

  private List<Token> getParams(AstNode identListAst) {
    List<Token> params = new ArrayList<Token>();
    if (identListAst != null) {
      for (AstNode node : identListAst.getChildren(IDENTIFIER)) {
        params.add(node.getToken());
      }
    }

    return params;
  }

  private File findIncludedFile(AstNode ast, Token token, String currFileName) {
    String includedFileName = null;
    File includedFile = null;
    boolean quoted = false;

    AstNode node = ast.getFirstDescendant(CppGrammar.includeBodyQuoted);
    if(node != null){
      includedFileName = stripQuotes(node.getFirstChild().getTokenValue());
      quoted = true;
    } else if((node = ast.getFirstDescendant(CppGrammar.includeBodyBracketed)) != null) {
      node = node.getFirstDescendant(LT).getNextSibling();
      StringBuilder sb = new StringBuilder();
      while (true) {
        String value = node.getTokenValue();
        if (value.equals(">")) {
          break;
        }
        sb.append(value);
        node = node.getNextSibling();
      }
      
      includedFileName = sb.toString();
    } else if((node = ast.getFirstDescendant(CppGrammar.includeBodyFreeform)) != null) {
      // expand and recurse
      String includeBody = serialize(stripEOF(node.getTokens()), "");
      String expandedIncludeBody = serialize(stripEOF(CxxLexer.create(this).lex(includeBody)), "");

      boolean parseError = false;
      AstNode includeBodyAst = null;
      try{
        includeBodyAst = pplineParser.parse("#include " + expandedIncludeBody);
      }
      catch(com.sonar.sslr.api.RecognitionException re){
        parseError = true;
      }

      if(parseError || includeBodyAst.getFirstDescendant(CppGrammar.includeBodyFreeform) != null){
        LOG.warn("[{}:{}]: cannot parse included filename: {}'",
                 new Object[] {currFileName, token.getLine(), expandedIncludeBody});
        return null;
      }
      
      return findIncludedFile(includeBodyAst, token, currFileName);
    }
    
    if (includedFileName != null) {
      File file = getFileUnderAnalysis();
      String dir = file == null ? "" : file.getParent();
      includedFile = codeProvider.getSourceCodeFile(includedFileName, dir, quoted);
    }

    return includedFile;
  }

  private String getMacroName(AstNode ast) {
    return ast.getFirstDescendant(IDENTIFIER).getTokenValue();
  }

  private String stripQuotes(String str) {
    return str.substring(1, str.length() - 1);
  }

  private File getFileUnderAnalysis() {
    if (state.includeUnderAnalysis == null) {
      return context.getFile();
    }
    return state.includeUnderAnalysis;
  }

  private boolean inSkippingMode() {
    return state.skipping;
  }
}
