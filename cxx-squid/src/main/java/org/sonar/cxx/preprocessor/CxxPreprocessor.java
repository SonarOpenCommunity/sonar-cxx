/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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
package org.sonar.cxx.preprocessor;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.Preprocessor;
import com.sonar.cxx.sslr.api.PreprocessorAction;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.Trivia;
import com.sonar.cxx.sslr.impl.Parser;
import com.sonar.cxx.sslr.impl.token.TokenUtils;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.annotation.CheckForNull;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.parser.CxxKeyword;
import org.sonar.cxx.parser.CxxLexerPool;
import org.sonar.cxx.parser.CxxTokenType;
import org.sonar.cxx.squidbridge.SquidAstVisitorContext;

/**
 * Implements a C++ preprocessor according to '**A.12 Preprocessing directives [gram.cpp]**'.
 * The grammar for single lines is implemented in 'CppGrammar'.
 *
 * **A.12 Preprocessing directives [gram.cpp]**
 *
 * <code>
 * preprocessing-file:
 *   groupopt
 *   module-file
 *
 * module-file:
 *   pp-global-module-fragmentopt pp-module groupopt pp-private-module-fragmentopt
 *
 * group:
 *   group-part
 *   group group-part
 *
 * group-part:
 *   control-line
 *   if-section
 *   text-line
 *   # conditionally-supported-directive
 *
 * if-section:
 *   if-group elif-groupsopt else-groupopt endif-line
 *
 * elif-groups:
 *   elif-group
 *   elif-groups elif-group
 * </code>
 */
public class CxxPreprocessor extends Preprocessor {

  private static final Logger LOG = Loggers.get(CxxPreprocessor.class);

  private final SquidAstVisitorContext<Grammar> context;
  private final CxxSquidConfiguration squidConfig;

  private MacroContainer<String, PPMacro> unitMacros = null;
  private MacroContainer<String, PPMacro> globalMacros = null;
  private List<String> globalIncludeDirectories = null;

  private File currentContextFile;

  private final Parser<Grammar> lineParser;
  private final PPExpression constantExpression;
  private CxxLexerPool lineLexerwithPP = null;
  private PPReplace replace = null;
  private PPInclude include = null;

  private static final String MISSING_INCLUDE_MSG = "Preprocessor: {} include directive error(s). "
                                                      + "This is only relevant if parser creates syntax errors."
                                                      + " The preprocessor searches for include files in the with "
                                                      + "'sonar.cxx.includeDirectories' defined directories and order.";

  public CxxPreprocessor(SquidAstVisitorContext<Grammar> context) {
    this(context, new CxxSquidConfiguration());
  }

  public CxxPreprocessor(SquidAstVisitorContext<Grammar> context, CxxSquidConfiguration squidConfig) {
    this.context = context;
    this.squidConfig = squidConfig;
    lineParser = PPParser.create(squidConfig.getCharset());
    constantExpression = new PPExpression(this);

    addPredefinedMacros();
  }

  public Charset getCharset() {
    return squidConfig.getCharset();
  }

  /**
   * Method called before the lexing starts which can be overridden to initialize a state for instance.
   *
   * Method can be called:
   * a) for a new "physical" file (including SquidAstVisitorContext mock)
   * b) while processing of #include directive.
   *
   * Attention: This function is called for each file and is therefore extremely performance critical!
   */
  @Override
  public void init() {
    // make sure, that the following code is executed for a new file only
    if (currentContextFile != context.getFile()) {
      currentContextFile = context.getFile();

      include = new PPInclude(this, currentContextFile);
      unitMacros = new MacroContainer<>();
      String path = currentContextFile.getAbsolutePath();

      if (globalMacros != null) {
        // reuse already parsed project macros
        unitMacros.putAll(globalMacros);
      } else {
        // on project level do this only once for all units
        lineLexerwithPP = CxxLexerPool.create(this);
        replace = new PPReplace(this); // TODO: try to remove dependecies inside PPReplace, lexer, unitMacros
        addGlobalIncludeDirectories();
        addGlobalMacros();
        addGlobalForcedIncludes();
        globalMacros = new MacroContainer<>();
        globalMacros.putAll(unitMacros);

        if (LOG.isDebugEnabled()) {
          LOG.debug("global include directories: {}", Include().getStandardIncludeDirs());
          LOG.debug("global macros: {}", globalMacros);
        }
      }

      LOG.debug("process unit '{}'", currentContextFile);

      // are items on unit level available: if not jump over below steps
      if (!squidConfig.isUnitsEmpty()) {

        // add unit specific stuff
        boolean changes = addUnitIncludeDirectories(path);
        if (changes && LOG.isDebugEnabled()) {
          LOG.debug("unit include directories: {}", Include().getStandardIncludeDirs());
        }
        changes = addUnitMacros(path);
        changes |= addUnitForcedIncludes(path);
        if (changes && LOG.isDebugEnabled()) {
          LOG.debug("unit macros: {}", unitMacros);
        }
      } else {
        // use global include directories only
        Include().setStandardIncludeDirs(globalIncludeDirectories, squidConfig.getBaseDir());
      }
    }
  }

  /**
   * Handle preprocessed tokens.
   *
   * Tokens with type PREPROCESSOR are preprocessor directives.
   *
   */
  @Override
  public PreprocessorAction process(List<Token> tokens) {
    var token = tokens.get(0);
    var type = token.getType();

    if (CxxTokenType.PREPROCESSOR.equals(type)) {
      return handlePreprocessorDirective(token);
    } else if (State().skipTokens() && !GenericTokenType.EOF.equals(type)) {
      return oneConsumedToken(token);
    } else if (GenericTokenType.IDENTIFIER.equals(type)) {
      return handleMacroReplacement(tokens, token);
    } else if (type instanceof CxxKeyword) {
      return handleMacroReplacement(tokens, token);
    }

    return PreprocessorAction.NO_OPERATION;
  }

  public PPInclude Include() {
    return include;
  }

  public PPState State() {
    return Include().State();
  }

  @CheckForNull
  AstNode lineParser(String line) {
    AstNode lineAst;
    try {
      lineAst = lineParser.parse(line);
    } catch (com.sonar.cxx.sslr.api.RecognitionException e) {
      LOG.warn("Cannot parse '{}', ignoring...", line);
      LOG.debug("Parser exception: '{}'", e.getMessage());
      lineAst = null;
    }
    return lineAst;
  }

  List<Token> tokenizeMacro(PPMacro macro, String macroExpression) {
    // C++ standard 16.3.4/2 Macro Replacement - Rescanning and further replacement
    List<Token> tokens = null;
    unitMacros.pushDisable(macro.identifier);
    try {
      tokens = tokenize(macroExpression);
    } finally {
      unitMacros.popDisable();
    }
    return tokens;
  }

  List<Token> tokenize(String expression) {
    // C++ standard 16.3.4/2 Macro Replacement - Rescanning and further replacement
    List<Token> tokens = null;
    var lexer = lineLexerwithPP.borrowLexer();
    try {
      // macros can be nested, so the expansion of macros can be called recursively.
      // Each level needs its own lexer (use a lexer pool).
      tokens = TokenUtils.removeLastTokenIfEof(lexer.lex(expression));
    } finally {
      lineLexerwithPP.returnLexer(lexer);
    }
    return tokens;
  }

  @CheckForNull
  PPMacro getMacro(String macroName) {
    return unitMacros.get(macroName);
  }

  private PreprocessorAction handlePreprocessorDirective(Token token) {
    AstNode lineAst = lineParser(token.getValue());
    if (lineAst == null) {
      return oneConsumedToken(token);
    }
    lineAst = lineAst.getFirstChild();

    switch ((PPGrammarImpl) lineAst.getType()) {
      case ifLine:
        return handleIfLine(lineAst, token);
      case elifLine:
        return handleElIfLine(lineAst, token);
      case ifdefLine:
        return handleIfdefLine(lineAst, token);
      case endifLine:
        return handleEndifLine(token);
      case elseLine:
        return handleElseLine(token);
      case includeLine:
        return handleIncludeLine(lineAst, token);
      case defineLine:
        return handleDefineLine(lineAst, token);
      case ppImport:
        return handleImportLine(lineAst, token);
      case ppModule:
        return handleModuleLine(lineAst, token);
      case undefLine:
        return handleUndefLine(lineAst, token);
      default:
        // ignore all other preprocessor directives (which are not handled explicitly) and strip them from the stream
        return oneConsumedToken(token);
    }
  }

  public static void finalReport() {
    int missing = PPInclude.getMissingFilesCounter();
    if (missing != 0) {
      LOG.warn(MISSING_INCLUDE_MSG, missing);
    }
  }

  public static void resetReport() {
    PPInclude.resetMissingFilesCounter();
  }

  private static String getIdentifierName(AstNode node) {
    return Optional.ofNullable(node.getFirstDescendant(GenericTokenType.IDENTIFIER))
      .map(AstNode::getTokenValue)
      .orElse("");
  }

  public void finishedPreprocessing() {
    // From 16.3.5 "Scope of macro definitions":
    // A macro definition lasts (independent of block structure) until a corresponding #undef directive is encountered
    // or (if none is encountered) until the end of the translation unit.

    unitMacros = null;
    include = null;
    currentContextFile = null;
  }

  public String valueOf(String macroname) {
    String result = null;
    PPMacro macro = getMacro(macroname);
    if (macro != null) {
      result = TokenUtils.merge(macro.replacementList);
    }
    return result;
  }

  public String expandFunctionLikeMacro(String macroName, List<Token> restTokens) {
    var expansion = new LinkedList<Token>();
    PPMacro macro = getMacro(macroName);
    if (macro != null) {
      replace.replaceFunctionLikeMacro(macro, restTokens, expansion);
      return TokenUtils.merge(expansion);
    }
    return "";
  }

  public Boolean expandHasIncludeExpression(AstNode exprAst) {
    return Include().searchFile(exprAst) != null;
  }

  private void addPredefinedMacros() {
    for (var macro : PPPredefinedMacros.predefinedMacroValues()) {
      squidConfig.add(CxxSquidConfiguration.PREDEFINED_MACROS, CxxSquidConfiguration.DEFINES, macro);
    }
  }

  private void addGlobalMacros() {
    var defines = squidConfig.getValues(CxxSquidConfiguration.GLOBAL, CxxSquidConfiguration.DEFINES);
    if (!defines.isEmpty()) {
      Collections.reverse(defines);
      parseMacroDefinitions(defines, unitMacros);
    }
  }

  private boolean addUnitMacros(String level) {
    var defines = squidConfig.getLevelValues(level, CxxSquidConfiguration.DEFINES);
    if (!defines.isEmpty()) {
      Collections.reverse(defines);
      parseMacroDefinitions(defines, unitMacros);
    }
    return false;
  }

  private void addGlobalIncludeDirectories() {
    globalIncludeDirectories = squidConfig.getValues(CxxSquidConfiguration.GLOBAL,
                                                     CxxSquidConfiguration.INCLUDE_DIRECTORIES);
    Include().setStandardIncludeDirs(globalIncludeDirectories, squidConfig.getBaseDir());
  }

  private boolean addUnitIncludeDirectories(String level) {
    List<String> unitIncludeDirectories = squidConfig.getLevelValues(level, CxxSquidConfiguration.INCLUDE_DIRECTORIES);
    boolean hasUnitIncludes = !unitIncludeDirectories.isEmpty();
    unitIncludeDirectories.addAll(globalIncludeDirectories);
    Include().setStandardIncludeDirs(unitIncludeDirectories, squidConfig.getBaseDir());
    return hasUnitIncludes;
  }

  private void addGlobalForcedIncludes() {
    for (var include : squidConfig.getValues(CxxSquidConfiguration.GLOBAL, CxxSquidConfiguration.FORCE_INCLUDES)) {
      if (!include.isEmpty()) {
        LOG.debug("parsing force include: '{}'", include);
        parseIncludeLine("#include \"" + include + "\"");
      }
    }
  }

  /**
   * Parse the configured forced includes and store it into the macro library.
   */
  private boolean addUnitForcedIncludes(String level) {
    int oldHash = unitMacros.hashCode();
    for (var include : squidConfig.getLevelValues(level, CxxSquidConfiguration.FORCE_INCLUDES)) {
      if (!include.isEmpty()) {
        LOG.debug("parsing force include: '{}'", include);
        parseIncludeLine("#include \"" + include + "\"");
      }
    }
    return oldHash != unitMacros.hashCode();
  }

  private void parseIncludeLine(String includeLine) {
    AstNode astNode = lineParser(includeLine);
    if (astNode != null) {
      handleIncludeLine(astNode, astNode.getFirstDescendant(PPGrammarImpl.includeBodyQuoted).getToken());
    }
  }

  @CheckForNull
  PPMacro parseMacroDefinition(String macroDef) {
    AstNode astNode = lineParser(macroDef);
    if (astNode != null) {
      return PPMacro.create(astNode.getFirstDescendant(PPGrammarImpl.defineLine));
    }
    return null;
  }

  /**
   * Parse defines, which are merged into one string
   */
  private void parseMacroDefinitions(List<String> defines, MacroContainer<String, PPMacro> result) {
    for (var define : defines) {
      if (!define.isBlank()) {
        PPMacro macro = parseMacroDefinition("#define " + define);
        if (macro != null) {
          result.put(macro.identifier, macro);
        }
      }
    }
  }

  //
  // Preprocessor directives
  //
  private void handleConstantExpression(AstNode ast, Token token) {
    try {
      State().setSkipTokens(false);
      boolean result = constantExpression.evaluate(ast.getFirstDescendant(PPGrammarImpl.constantExpression));
      State().setConditionValue(result);
      State().setSkipTokens(!result);
    } catch (EvaluationException e) {
      String rootFilePath = State().getFileUnderAnalysisPath();
      LOG.error("[{}:{}]: error evaluating the expression {} assume 'true' ...",
                rootFilePath, token.getLine(), token.getValue());
      State().setConditionValue(true);
      State().setSkipTokens(false);
    }
  }

  private PreprocessorAction handleIfdefLine(AstNode ast, Token token) {
    if (State().skipTokens()) {
      State().changeNestingDepth(+1);
    } else {
      PPMacro macro = getMacro(getIdentifierName(ast));
      var tokType = ast.getToken().getType();
      boolean result = (tokType.equals(PPKeyword.IFDEF) && macro != null)
                         || (tokType.equals(PPKeyword.IFNDEF) && macro == null);
      State().setConditionValue(result);
      State().setSkipTokens(!result);
    }

    return oneConsumedToken(token);
  }

  private PreprocessorAction handleIfLine(AstNode ast, Token token) {
    if (State().skipTokens()) {
      State().changeNestingDepth(+1);
    } else {
      handleConstantExpression(ast, token);
    }

    return oneConsumedToken(token);
  }

  private PreprocessorAction handleElIfLine(AstNode ast, Token token) {
    // handling of an #elif line is similar to handling of an #if line but doesn't increase the nesting level
    if (!State().isInsideNestedBlock()) {
      if (State().skipTokens() && State().ifLastConditionWasFalse()) {
        // the preceding expression had been evaluated to false
        handleConstantExpression(ast, token);
      } else {
        // other block was already true: skipping tokens inside this #elif
        State().setSkipTokens(true);
      }
    }

    return oneConsumedToken(token);
  }

  private PreprocessorAction handleElseLine(Token token) {
    if (!State().isInsideNestedBlock()) {
      if (State().skipTokens() && State().ifLastConditionWasFalse()) {
        // other block(s) were false: #else returning to non-skipping mode
        State().setConditionValue(true);
        State().setSkipTokens(false);
      } else {
        // other block was true: skipping tokens inside the #else
        State().setSkipTokens(true);
      }
    }

    return oneConsumedToken(token);
  }

  private PreprocessorAction handleEndifLine(Token token) {
    if (State().isInsideNestedBlock()) {
      // nested #endif
      State().changeNestingDepth(-1);
    } else {
      // after last #endif, switching to non-skipping mode
      State().setSkipTokens(false);
      State().setConditionValue(false);
    }

    return oneConsumedToken(token);
  }

  private PreprocessorAction handleDefineLine(AstNode ast, Token token) {
    if (!State().skipTokens()) {
      // Here we have a define directive. Parse it and store the macro in a dictionary.
      PPMacro macro = PPMacro.create(ast);
      unitMacros.put(macro.identifier, macro);
    }

    return oneConsumedToken(token);
  }

  private PreprocessorAction handleUndefLine(AstNode ast, Token token) {
    if (!State().skipTokens()) {
      String macroName = ast.getFirstDescendant(GenericTokenType.IDENTIFIER).getTokenValue();
      unitMacros.remove(macroName);
    }
    return oneConsumedToken(token);
  }

  private PreprocessorAction handleIncludeLine(AstNode ast, Token token) {
    if (!State().skipTokens()) {
      Include().handleFile(ast, token);
    }

    return oneConsumedToken(token);
  }

  private PreprocessorAction handleImportLine(AstNode ast, Token token) {
    if (!State().skipTokens()) {
      if (ast.getFirstDescendant(PPGrammarImpl.expandedIncludeBody) != null) {
        // import <file>
        return handleIncludeLine(ast, token);
      }

      // forward to parser: ...  import ...
      var result = TokenList.transformToCxx(ast.getTokens(), token);
      return new PreprocessorAction(1, Collections.singletonList(Trivia.createPreprocessingToken(token)), result);
    }
    return oneConsumedToken(token);
  }

  private PreprocessorAction handleModuleLine(AstNode ast, Token token) {
    if (!State().skipTokens()) {
      // forward to parser: ...  module ...
      var result = TokenList.transformToCxx(ast.getTokens(), token);
      return new PreprocessorAction(1, Collections.singletonList(Trivia.createPreprocessingToken(token)), result);
    }
    return oneConsumedToken(token);
  }

  /**
   * Replace text macros while possibly concatenating or quoting identifiers
   * (controlled by directives #define and #undef, and operators # and ##).
   *
   * Every identifier and every keyword can be a macro instance. Pipe the resulting string through a lexer to create
   * proper tokens and to expand recursively all macros which may be in there.
   */
  private PreprocessorAction handleMacroReplacement(List<Token> tokens, Token curr) {
    PreprocessorAction ppaction = PreprocessorAction.NO_OPERATION;
    PPMacro macro = getMacro(curr.getValue());
    if (macro != null) {
      List<Token> replTokens = new LinkedList<>();
      var tokensConsumed = 0;

      if (macro.parameterList == null) {
        tokensConsumed = 1;
        replTokens = new LinkedList<>(replace.replaceObjectLikeMacro(macro, TokenUtils.merge(PPConcatenation
                                                                     .concatenate(macro.replacementList)))
        );
      } else {
        int tokensConsumedMatchingArgs = replace.replaceFunctionLikeMacro(macro, tokens.subList(1, tokens.size()),
                                                                          replTokens
        );
        if (tokensConsumedMatchingArgs > 0) {
          tokensConsumed = 1 + tokensConsumedMatchingArgs;
        }
      }

      if (tokensConsumed > 0) {

        // Rescanning to expand function like macros, in case it requires consuming more tokens
        List<Token> outTokens = new LinkedList<>();
        unitMacros.pushDisable(macro.identifier);
        while (!replTokens.isEmpty()) {
          var c = replTokens.get(0);
          PreprocessorAction action = PreprocessorAction.NO_OPERATION;
          if (c.getType().equals(GenericTokenType.IDENTIFIER)) {
            List<Token> rest = new ArrayList<>(replTokens);
            rest.addAll(tokens.subList(tokensConsumed, tokens.size()));
            action = handleMacroReplacement(rest, c);
          }
          if (action.equals(PreprocessorAction.NO_OPERATION)) {
            replTokens.remove(0);
            outTokens.add(c);
          } else {
            outTokens.addAll(action.getTokensToInject());
            int tokensConsumedRescanning = action.getNumberOfConsumedTokens();
            if (tokensConsumedRescanning >= replTokens.size()) {
              tokensConsumed += tokensConsumedRescanning - replTokens.size();
              replTokens.clear();
            } else {
              replTokens.subList(0, tokensConsumedRescanning).clear();
            }
          }
        }
        replTokens = outTokens;
        unitMacros.popDisable();
        replTokens = TokenList.adjustPosition(replTokens, curr);

        ppaction = new PreprocessorAction(
          tokensConsumed,
          Collections.singletonList(Trivia.createSkippedText(tokens.subList(0, tokensConsumed))),
          replTokens);
      }
    }

    return ppaction;
  }

  private PreprocessorAction oneConsumedToken(Token token) {
    return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)), Collections.emptyList());
  }

}
