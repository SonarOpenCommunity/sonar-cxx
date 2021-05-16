/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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

import com.sonar.sslr.api.AstNode;
import static com.sonar.sslr.api.GenericTokenType.EOF;
import static com.sonar.sslr.api.GenericTokenType.IDENTIFIER;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Preprocessor;
import com.sonar.sslr.api.PreprocessorAction;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import com.sonar.sslr.impl.Parser;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.parser.CxxLexer;
import static org.sonar.cxx.parser.CxxTokenType.NUMBER;
import static org.sonar.cxx.parser.CxxTokenType.PREPROCESSOR;
import static org.sonar.cxx.parser.CxxTokenType.STRING;
import static org.sonar.cxx.parser.CxxTokenType.WS;
import static org.sonar.cxx.preprocessor.CppGrammarImpl.defineLine;
import static org.sonar.cxx.preprocessor.CppGrammarImpl.elifLine;
import static org.sonar.cxx.preprocessor.CppGrammarImpl.elseLine;
import static org.sonar.cxx.preprocessor.CppGrammarImpl.endifLine;
import static org.sonar.cxx.preprocessor.CppGrammarImpl.ifLine;
import static org.sonar.cxx.preprocessor.CppGrammarImpl.ifdefLine;
import static org.sonar.cxx.preprocessor.CppGrammarImpl.includeLine;
import static org.sonar.cxx.preprocessor.CppGrammarImpl.undefLine;
import static org.sonar.cxx.preprocessor.CppKeyword.IFDEF;
import static org.sonar.cxx.preprocessor.CppKeyword.IFNDEF;
import static org.sonar.cxx.preprocessor.CppPunctuator.BR_RIGHT;
import static org.sonar.cxx.preprocessor.CppPunctuator.COMMA;
import static org.sonar.cxx.preprocessor.CppPunctuator.HASH;
import static org.sonar.cxx.preprocessor.CppPunctuator.HASHHASH;
import static org.sonar.cxx.preprocessor.CppPunctuator.LT;
import org.sonar.cxx.squidbridge.SquidAstVisitorContext;

/**
 * Implements a C++ preprocessor according to '**A.12 Preprocessing directives [gram.cpp]**'.
 * The grammar for single lines is implemented in 'CppGrammar'.
 *
 * **A.12 Preprocessing directives [gram.cpp]**
 *
 * preprocessing-file:
 *    groupopt
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
 */
public class CxxPreprocessor extends Preprocessor {

  private static final Logger LOG = Loggers.get(CxxPreprocessor.class);

  private final SquidAstVisitorContext<Grammar> context;
  private final CxxSquidConfiguration squidConfig;
  private final SourceCodeProvider mockCodeProvider;

  private MapChain<String, Macro> unitMacros = null;
  private MapChain<String, Macro> globalMacros = null;
  private List<String> globalIncludeDirectories = null;

  private SourceCodeProvider unitCodeProvider;
  private File currentContextFile;

  private final Set<File> analysedFiles = new HashSet<>();
  private final Parser<Grammar> pplineParser;

  private static final String MISSING_INCLUDE_MSG =
     "Preprocessor: {} include directive error(s). "
     + "This is only relevant if parser creates syntax errors."
     + " The preprocessor searches for include files in the with "
     + "'sonar.cxx.includeDirectories' defined directories and order.";
  private static int missingIncludeFilesCounter = 0;

  public CxxPreprocessor(SquidAstVisitorContext<Grammar> context) {
    this(context, new CxxSquidConfiguration());
  }

  public CxxPreprocessor(SquidAstVisitorContext<Grammar> context, CxxSquidConfiguration squidConfig) {
    this(context, squidConfig, null);
  }

  /**
   * Ctor for unit tests only
   *
   * @param mockCodeProvider additional parameter to mock the code provider
   */
  public CxxPreprocessor(SquidAstVisitorContext<Grammar> context,
                         CxxSquidConfiguration squidConfig,
                         @Nullable SourceCodeProvider mockCodeProvider) {
    this.context = context;
    this.squidConfig = squidConfig;
    this.mockCodeProvider = mockCodeProvider;
    pplineParser = CppParser.create(squidConfig.getCharset());

    if (this.mockCodeProvider != null) {
      this.mockCodeProvider.setIncludeRoots(
        this.squidConfig.getValues(CxxSquidConfiguration.GLOBAL, CxxSquidConfiguration.INCLUDE_DIRECTORIES),
        this.squidConfig.getBaseDir());
    }

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

      unitCodeProvider = new SourceCodeProvider(currentContextFile);
      unitMacros = new MapChain<>();
      String path = currentContextFile.getAbsolutePath();

      if (globalMacros != null) {
        // reuse already parsed project macros
        unitMacros.putAll(globalMacros);
      } else {
        // on project level do this only once for all units
        addGlobalIncludeDirectories();
        addGlobalMacros();
        addGlobalForcedIncludes();
        globalMacros = new MapChain<>();
        globalMacros.putAll(unitMacros);

        if(LOG.isDebugEnabled()) {
          LOG.debug("global include directories: {}", unitCodeProvider.getIncludeRoots());
          LOG.debug("global macros: {}", globalMacros);
        }
      }

      LOG.debug("process unit '{}'", currentContextFile);

      // add unit specific stuff
      boolean changes = addUnitIncludeDirectories(path);
      if (changes && LOG.isDebugEnabled() ) {
        LOG.debug("unit include directories: {}", unitCodeProvider.getIncludeRoots());
      }
      changes = addUnitMacros(path);
      changes |= addUnitForcedIncludes(path);
      if (changes && LOG.isDebugEnabled()) {
        LOG.debug("unit macros: {}", unitMacros);
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
  public PreprocessorAction process(List<Token> tokens) { //TODO: deprecated PreprocessorAction
    Token token = tokens.get(0);
    var type = token.getType();

    if (type.equals(PREPROCESSOR)) {
      String rootFilePath = unitCodeProvider.getFileUnderAnalysisPath();
      return handlePreprocessorDirective(token, rootFilePath);
    }

    if (!type.equals(EOF)) {
      if (unitCodeProvider.doSkipBlock()) {
        return oneConsumedToken(token);
      }

      if (!type.equals(STRING) && !type.equals(NUMBER)) {
        String rootFilePath = unitCodeProvider.getFileUnderAnalysisPath();
        return handleIdentifiersAndKeywords(tokens, token, rootFilePath);
      }
    }

    return PreprocessorAction.NO_OPERATION;
  }

  private PreprocessorAction handlePreprocessorDirective(Token token, String rootFilePath) {
    AstNode lineAst;
    try {
      lineAst = pplineParser.parse(token.getValue()).getFirstChild();
    } catch (com.sonar.sslr.api.RecognitionException e) {
      LOG.warn("Cannot parse '{}', ignoring...", token.getValue());
      LOG.debug("Parser exception: '{}'", e.getMessage());
      return oneConsumedToken(token);
    }

    CppGrammarImpl type = (CppGrammarImpl) lineAst.getType();
    switch (type) {
      case ifLine:
        return handleIfLine(lineAst, token, rootFilePath);
      case elifLine:
        return handleElIfLine(lineAst, token, rootFilePath);
      case ifdefLine:
        return handleIfdefLine(lineAst, token, rootFilePath);
      case endifLine:
        return handleEndifLine(token, rootFilePath);
      case elseLine:
        return handleElseLine(token, rootFilePath);
      default:
        if (unitCodeProvider.doSkipBlock()) {
          return oneConsumedToken(token);
        }
        break;
    }

    switch (type) {
      case includeLine:
        return handleIncludeLine(lineAst, token, rootFilePath, squidConfig.getCharset());
      case defineLine:
        return handleDefineLine(lineAst, token, rootFilePath);
      case ppImport:
        return handleImportLine(lineAst, token, rootFilePath, squidConfig.getCharset());
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
    if (missingIncludeFilesCounter != 0) {
      LOG.warn(MISSING_INCLUDE_MSG, missingIncludeFilesCounter);
    }
  }

  public static void resetReport() {
    missingIncludeFilesCounter = 0;
  }

  private static List<Token> stripEOF(List<Token> tokens) {
    if (tokens.get(tokens.size() - 1).getType().equals(EOF)) {
      return tokens.subList(0, tokens.size() - 1);
    } else {
      return tokens;
    }
  }

  private static String serialize(List<Token> tokens) {
    return serialize(tokens, " ");
  }

  private static String serialize(List<Token> tokens, String spacer) {
    return tokens.stream().map(Token::getValue).collect(Collectors.joining(spacer));
  }

  private static List<Token> serialize(AstNode ast) {
    return ast.getChildren().stream().map(AstNode::getToken).collect(Collectors.toList());
  }

  private static int matchArguments(List<Token> tokens, List<Token> arguments) {
    List<Token> rest = new ArrayList<>(tokens);
    try {
      rest = match(rest, "(");
    } catch (MismatchException e) {
      return 0;
    }

    try {
      do {
        rest = matchArgument(rest, arguments);
        try {
          rest = match(rest, ",");
        } catch (MismatchException e) {
          break;
        }
      } while (true);
    } catch (MismatchException e) {
      // ...
    }
    try {
      rest = match(rest, ")");
    } catch (MismatchException e) {
      LOG.error("MismatchException : '{}' rest: '{}'", e.getMessage(), rest);
      return 0;
    }
    return tokens.size() - rest.size();
  }

  private static List<Token> match(List<Token> tokens, String str) throws MismatchException {
    if (!tokens.get(0).getValue().equals(str)) {
      throw new MismatchException("Mismatch: expected '" + str + "' got: '"
                                    + tokens.get(0).getValue() + "'" + " [" + tokens.get(0).getURI() + "("
                                    + tokens.get(0).getLine() + "," + tokens.get(0).getColumn() + ")]");
    }
    return tokens.subList(1, tokens.size());
  }

  private static List<Token> matchArgument(List<Token> tokens, List<Token> arguments) throws MismatchException {
    var nestingLevel = 0;
    var tokensConsumed = 0;
    var noTokens = tokens.size();
    Token firstToken = tokens.get(0);
    Token currToken = firstToken;
    String curr = currToken.getValue();
    var matchedTokens = new LinkedList<Token>();

    while (true) {
      if (nestingLevel == 0 && (",".equals(curr) || ")".equals(curr))) {
        if (tokensConsumed > 0) {
          arguments.add(Token.builder()
            .setLine(firstToken.getLine())
            .setColumn(firstToken.getColumn())
            .setURI(firstToken.getURI())
            .setValueAndOriginalValue(serialize(matchedTokens).trim())
            .setType(STRING)
            .build());
        }
        return tokens.subList(tokensConsumed, noTokens);
      }

      if ("(".equals(curr)) {
        nestingLevel++;
      } else if (")".equals(curr)) {
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

  private static List<Token> evaluateHashhashOperators(List<Token> tokens) {
    var newTokens = new ArrayList<Token>();

    Iterator<Token> it = tokens.iterator();
    while (it.hasNext()) {
      Token curr = it.next();
      if ("##".equals(curr.getValue())) {
        Token pred = predConcatToken(newTokens);
        Token succ = succConcatToken(it);
        if (pred != null && succ != null) {
          newTokens.add(Token.builder()
            .setLine(pred.getLine())
            .setColumn(pred.getColumn())
            .setURI(pred.getURI())
            .setValueAndOriginalValue(pred.getValue() + succ.getValue())
            .setType(pred.getType())
            .setGeneratedCode(true)
            .build());
        } else {
          LOG.error("Missing data : succ ='{}' or pred = '{}'", succ, pred);
        }
      } else {
        newTokens.add(curr);
      }
    }

    return newTokens;
  }

  @CheckForNull
  private static Token predConcatToken(List<Token> tokens) {
    while (!tokens.isEmpty()) {
      Token last = tokens.remove(tokens.size() - 1);
      if (!last.getType().equals(WS)) {
        if (!tokens.isEmpty()) {
          Token pred = tokens.get(tokens.size() - 1);
          if (!pred.getType().equals(WS) && !pred.hasTrivia()) {
            // Needed to paste tokens 0 and x back together after #define N(hex) 0x ## hex
            tokens.remove(tokens.size() - 1);
            String replacement = pred.getValue() + last.getValue();
            last = Token.builder()
              .setLine(pred.getLine())
              .setColumn(pred.getColumn())
              .setURI(pred.getURI())
              .setValueAndOriginalValue(replacement)
              .setType(pred.getType())
              .setGeneratedCode(true)
              .build();
          }
        }
        return last;
      }
    }
    return null;
  }

  @CheckForNull
  private static Token succConcatToken(Iterator<Token> it) {
    Token succ = null;
    while (it.hasNext()) {
      succ = it.next();
      if (!"##".equals(succ.getValue()) && !succ.getType().equals(WS)) {
        break;
      }
    }
    return succ;
  }

  private static String quote(String str) {
    var result = new StringBuilder(2 * str.length());
    var addBlank = false;
    var ignoreNextBlank = false;
    for (var i = 0; i < str.length(); i++) {
      var c = str.charAt(i);
      if (Character.isLowerCase(c) || Character.isUpperCase(c) || Character.isDigit(c) || c == '_') { // token
        if (addBlank) {
          result.append(' ');
          addBlank = false;
        }
        result.append(c);
      } else { // special characters
        switch (c) {
          case ' ':
            if (ignoreNextBlank) {
              ignoreNextBlank = false;
            } else {
              addBlank = true;
            }
            break;
          case '\"':
            if (addBlank) {
              result.append(' ');
              addBlank = false;
            }
            result.append("\\\"");
            break;
          case '\\':
            result.append("\\\\");
            addBlank = false;
            ignoreNextBlank = true;
            break;
          default: // operator
            result.append(c);
            addBlank = false;
            ignoreNextBlank = true;
            break;
        }
      }
    }
    return result.toString();
  }

  private static String encloseWithQuotes(String str) {
    return "\"" + str + "\"";
  }

  private static List<Token> reallocate(List<Token> tokens, Token token) {
    var reallocated = new LinkedList<Token>();
    int currColumn = token.getColumn();
    for (var t : tokens) {
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

  private static Macro parseMacroDefinition(AstNode defineLineAst) {
    var ast = defineLineAst.getFirstChild();
    var nameNode = ast.getFirstDescendant(CppGrammarImpl.ppToken);
    String macroName = nameNode.getTokenValue();

    var paramList = ast.getFirstDescendant(CppGrammarImpl.parameterList);
    List<Token> macroParams = paramList == null
                                ? "objectlikeMacroDefinition".equals(ast.getName()) ? null : new LinkedList<>()
                                : getParams(paramList);

    var vaargs = ast.getFirstDescendant(CppGrammarImpl.variadicparameter);
    if ((vaargs != null) && (macroParams != null)) {
      var identifier = vaargs.getFirstChild(IDENTIFIER);
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

    var replList = ast.getFirstDescendant(CppGrammarImpl.replacementList);
    List<Token> macroBody = replList == null
                              ? new LinkedList<>()
                              : replList.getTokens().subList(0, replList.getTokens().size() - 1);

    return new Macro(macroName, macroParams, macroBody, vaargs != null);
  }

  private static List<Token> getParams(AstNode identListAst) {
    return identListAst.getChildren(IDENTIFIER).stream().map(AstNode::getToken).collect(Collectors.toList());
  }

  private static String getMacroName(AstNode ast) {
    return ast.getFirstDescendant(IDENTIFIER).getTokenValue();
  }

  private static String stripQuotes(String str) {
    return str.substring(1, str.length() - 1);
  }

  public void finishedPreprocessing(File file) {
    // From 16.3.5 "Scope of macro definitions":
    // A macro definition lasts (independent of block structure) until a corresponding #undef directive is encountered
    // or (if none is encountered) until the end of the translation unit.

    analysedFiles.clear();
    unitMacros = null;
    unitCodeProvider = null;
    currentContextFile = null;
  }

  public SourceCodeProvider getCodeProvider() {
    return mockCodeProvider != null ? mockCodeProvider : unitCodeProvider;
  }

  public Macro getMacro(String macroname) {
    return unitMacros.get(macroname);
  }

  public String valueOf(String macroname) {
    String result = null;
    Macro macro = getMacro(macroname);
    if (macro != null) {
      result = serialize(macro.body);
    }
    return result;
  }

  public String expandFunctionLikeMacro(String macroName, List<Token> restTokens) {
    var expansion = new LinkedList<Token>();
    expandFunctionLikeMacro(macroName, restTokens, expansion);
    return serialize(expansion);
  }

  public Boolean expandHasIncludeExpression(AstNode exprAst) {
    return findIncludedFile(exprAst, exprAst.getToken(), unitCodeProvider.getFileUnderAnalysisPath()) != null;
  }

  /**
   * This is a collection of standard macros according to
   * http://gcc.gnu.org/onlinedocs/cpp/Standard-Predefined-Macros.html
   */
  private void addPredefinedMacros() {
    String[] predefinedMacros = {
      "__FILE__ \"file\"",
      "__LINE__ 1",
      // indicates 'date unknown'. should suffice
      "__DATE__ \"??? ?? ????\"",
      // indicates 'time unknown'. should suffice
      "__TIME__ \"??:??:??\"",
      "__STDC__ 1",
      "__STDC_HOSTED__ 1",
      "__cplusplus 201103L",
      // __has_include support (C++17)
      "__has_include 1"
    };

    for (var macro : predefinedMacros) {
      squidConfig.add(CxxSquidConfiguration.PREDEFINED_MACROS, CxxSquidConfiguration.DEFINES, macro);
    }
  }

  private void addGlobalMacros() {
    var defines = squidConfig.getValues(CxxSquidConfiguration.GLOBAL, CxxSquidConfiguration.DEFINES);
    if (!defines.isEmpty()) {
      Collections.reverse(defines);
      var macros = parseMacroDefinitions(defines);
      if (!macros.isEmpty()) {
        unitMacros.putAll(macros);
      }
    }
  }

  private boolean addUnitMacros(String level) {
    var defines = squidConfig.getLevelValues(level, CxxSquidConfiguration.DEFINES);
    if (!defines.isEmpty()) {
      Collections.reverse(defines);
      var macros = parseMacroDefinitions(defines);
      if (!macros.isEmpty()) {
        unitMacros.putAll(macros);
        return true;
      }
    }
    return false;
  }

  private void addGlobalIncludeDirectories() {
    globalIncludeDirectories = squidConfig.getValues(CxxSquidConfiguration.GLOBAL, CxxSquidConfiguration.INCLUDE_DIRECTORIES);
    unitCodeProvider.setIncludeRoots(globalIncludeDirectories,squidConfig.getBaseDir());
  }

  private boolean addUnitIncludeDirectories(String level) {
    List<String> unitIncludeDirectories = squidConfig.getLevelValues(level, CxxSquidConfiguration.INCLUDE_DIRECTORIES);
    boolean hasUnitIncludes = !unitIncludeDirectories.isEmpty();
    unitIncludeDirectories.addAll(globalIncludeDirectories);
    unitCodeProvider.setIncludeRoots(unitIncludeDirectories,squidConfig.getBaseDir());
    return hasUnitIncludes;
  }

  private void addGlobalForcedIncludes() {
    for (var include : squidConfig.getValues(CxxSquidConfiguration.GLOBAL, CxxSquidConfiguration.FORCE_INCLUDES)) {
      if (!include.isEmpty()) {
        LOG.debug("parsing force include: '{}'", include);
        parseIncludeLine("#include \"" + include + "\"", "sonar.cxx.forceIncludes",
                         squidConfig.getCharset());
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
        parseIncludeLine("#include \"" + include + "\"", "sonar.cxx.forceIncludes",
                         squidConfig.getCharset());
      }
    }
    return oldHash != unitMacros.hashCode();
  }

  private PreprocessorAction handleIfdefLine(AstNode ast, Token token, String filename) {
    if (unitCodeProvider.doNotSkipBlock()) {
      Macro macro = getMacro(getMacroName(ast));
      var tokType = ast.getToken().getType();
      if ((tokType.equals(IFDEF) && macro == null) || (tokType.equals(IFNDEF) && macro != null)) {
        // evaluated to false
        unitCodeProvider.skipBlock(true);
      }
      if (unitCodeProvider.doNotSkipBlock()) {
        unitCodeProvider.expressionWas(true);
      }
    } else {
      unitCodeProvider.nestedBlock(+1);
    }

    return oneConsumedToken(token);
  }

  private void parseIncludeLine(String includeLine, String filename, Charset charset) {
    AstNode includeAst = pplineParser.parse(includeLine);
    handleIncludeLine(includeAst, includeAst.getFirstDescendant(CppGrammarImpl.includeBodyQuoted)
                      .getToken(), filename, charset);
  }

  private int expandFunctionLikeMacro(String macroName, List<Token> restTokens, List<Token> expansion) {
    List<Token> arguments = new ArrayList<>();
    int tokensConsumedMatchingArgs = matchArguments(restTokens, arguments);

    Macro macro = getMacro(macroName);
    if (macro != null && macro.checkArgumentsCount(arguments.size())) {
      if (arguments.size() > macro.params.size()) {
        // group all arguments into the last one (__VA_ARGS__)
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
      List<Token> replTokens = replaceParams(macro.body, macro.params, arguments);
      replTokens = evaluateHashhashOperators(replTokens);
      expansion.addAll(expandMacro(macro.name, serialize(replTokens)));
    }

    return tokensConsumedMatchingArgs;
  }

  private List<Token> expandMacro(String macroName, String macroExpression) {
    // C++ standard 16.3.4/2 Macro Replacement - Rescanning and further replacement
    List<Token> tokens = null;
    unitMacros.disable(macroName);
    try {
      tokens = stripEOF(CxxLexer.create(this).lex(macroExpression));
    } finally {
      unitMacros.enable(macroName);
    }

    // make sure that all expanded Tokens are marked as generated it will prevent them from being involved into
    // NCLOC / complexity / highlighting
    for (var token : tokens) {
      if (!token.isGeneratedCode()) {
        token = Token.builder(token).setGeneratedCode(true).build();
      }
    }

    return tokens;
  }

  private static void expandVaOpt(List<Token> tokens, boolean keep) {
    // va-opt-replacement:
    //    __VA_OPT__ ( pp-tokensopt )
    //
    var firstIndex = -1;
    var lastIndex = -1;
    var brackets = 0;

    for (var i = 0; i < tokens.size(); i++) {
      switch (tokens.get(i).getValue()) {
        case "(":
          brackets++;
          break;
        case ")":
          brackets--;
          break;
      }
      if (brackets > 0) {
        if (firstIndex == -1) {
          firstIndex = i;
        }
      } else {
        if (firstIndex != -1 && lastIndex == -1) {
          lastIndex = i;
          break;
        }
      }
    }
    var replTokens = new ArrayList<Token>();
    if (firstIndex > 0 && lastIndex < tokens.size()) {
      if (keep) {
        // keep pp-tokensopt, remove ) and __VA_OPT__ (
        tokens.subList(lastIndex, lastIndex+1).clear();
        tokens.subList(0, firstIndex).clear();
      } else {
        // remove from body:  __VA_OPT__ ( pp-tokensopt )
        tokens.subList(firstIndex - 1, lastIndex + 1).clear();
      }
    }
  }

  private List<Token> replaceParams(List<Token> body, List<Token> parameters, List<Token> arguments) {
    // replace all parameters by according arguments "Stringify" the argument if the according parameter is
    // preceded by an #

    var newTokens = new ArrayList<Token>();
    if (!body.isEmpty()) {
      var tokenPastingLeftOp = false;
      var tokenPastingRightOp = false;

      // container to search parameter by name
      var paramterIndex = new HashMap<String, Integer>();
      for(var index=0; index<parameters.size(); index++) {
        paramterIndex.put(parameters.get(index).getValue(),index);
      }

      for (var i = 0; i < body.size(); ++i) {
        Token curr = body.get(i);
        int index = -1;
        if (curr.getType().equals(IDENTIFIER)) {
          index = paramterIndex.getOrDefault(curr.getValue(), -1);
        }
        if (index == -1) {
          if (curr.getValue().equals("__VA_OPT__")) {
            boolean keep = parameters.size() == arguments.size();
            expandVaOpt(body.subList(i, body.size()), keep);
          } else {
            if (tokenPastingRightOp && !curr.getType().equals(WS) && !curr.getType().equals(HASHHASH)) {
              tokenPastingRightOp = false;
            }
            newTokens.add(curr);
          }
        } else if (index == arguments.size()) {
          // EXTENSION: GCC's special meaning of token paste operator:
          // If variable argument is left out then the comma before the paste operator will be deleted.
          int j = i;
          while (j > 0 && body.get(j - 1).getType().equals(WS)) {
            j--;
          }
          if (j > 0 && "##".equals(body.get(j - 1).getValue())) {
            int k = --j;
            while (j > 0 && body.get(j - 1).getType().equals(WS)) {
              j--;
            }
            if (j > 0 && ",".equals(body.get(j - 1).getValue())) {
              newTokens.remove(newTokens.size() - 1 + j - i); // remove the comma
              newTokens.remove(newTokens.size() - 1 + k - i); // remove the paste operator
            }
          } else if (j > 0 && ",".equals(body.get(j - 1).getValue())) {
            // Got empty variadic args, remove comma
            newTokens.remove(newTokens.size() - 1 + j - i);
          }
        } else if (index < arguments.size()) {
          // token pasting operator?
          int j = i + 1;
          while (j < body.size() && body.get(j).getType().equals(WS)) {
            j++;
          }
          if (j < body.size() && "##".equals(body.get(j).getValue())) {
            tokenPastingLeftOp = true;
          }
          // in case of token pasting operator do not fully expand
          Token replacement = arguments.get(index);
          String newValue;
          if (tokenPastingLeftOp) {
            newValue = replacement.getValue();
            tokenPastingLeftOp = false;
            tokenPastingRightOp = true;
          } else if (tokenPastingRightOp) {
            newValue = replacement.getValue();
            tokenPastingLeftOp = false;
            tokenPastingRightOp = false;
          } else {
            if (i > 0 && "#".equals(body.get(i - 1).getValue())) {
              // if the token is a macro, the macro is not expanded - the macro name is converted into a string
              newTokens.remove(newTokens.size() - 1);
              newValue = encloseWithQuotes(quote(replacement.getValue()));
            } else {
              // otherwise the arguments have to be fully expanded before expanding the body of the macro
              newValue = serialize(expandMacro("", replacement.getValue()));
            }
          }

          if (newValue.isEmpty() && "__VA_ARGS__".equals(curr.getValue())) {
            // the Visual C++ implementation will suppress a trailing comma if no arguments are passed to the ellipsis
            for (var n = newTokens.size() - 1; n != 0; n = newTokens.size() - 1) {
              if (newTokens.get(n).getType().equals(WS)) {
                newTokens.remove(n);
              } else if (newTokens.get(n).getType().equals(COMMA)) {
                newTokens.remove(n);
                break;
              } else {
                break;
              }
            }
          } else {
            newTokens.add(Token.builder()
              .setLine(replacement.getLine())
              .setColumn(replacement.getColumn())
              .setURI(replacement.getURI())
              .setValueAndOriginalValue(newValue)
              .setType(replacement.getType())
              .setGeneratedCode(true).build());
          }
        }
      }
    }

    // replace # with "" if sequence HASH BR occurs for body HASH __VA_ARGS__
    if (newTokens.size() > 3 && newTokens.get(newTokens.size() - 2).getType().equals(HASH)
          && newTokens.get(newTokens.size() - 1).getType().equals(BR_RIGHT)) {
      for (var n = newTokens.size() - 2; n != 0; n--) {
        if (newTokens.get(n).getType().equals(WS)) {
          newTokens.remove(n);
        } else if (newTokens.get(n).getType().equals(HASH)) {
          newTokens.remove(n);
          newTokens.add(n, Token.builder()
                        .setLine(newTokens.get(n).getLine())
                        .setColumn(newTokens.get(n).getColumn())
                        .setURI(newTokens.get(n).getURI())
                        .setValueAndOriginalValue("\"\"")
                        .setType(STRING)
                        .setGeneratedCode(true).build());
          break;
        } else {
          break;
        }
      }
    }
    return newTokens;
  }

  private Macro parseMacroDefinition(String macroDef) {
    return parseMacroDefinition(pplineParser.parse(macroDef)
      .getFirstDescendant(CppGrammarImpl.defineLine));
  }

  /**
   * Parse defines, which are merged into one string
   */
  private Map<String, Macro> parseMacroDefinitions(List<String> defines) {
    var result = new HashMap<String, Macro>();

    for (var define : defines) {
      if (define.isBlank()) {
        continue;
      }

      var defineString = "#define " + define;
      Macro macro = parseMacroDefinition(defineString);

      if (macro != null) {
        result.put(macro.name, macro);
      }
    }
    return result;
  }

  @CheckForNull
  private File findIncludedFile(AstNode ast, Token token, String currFileName) {
    String includedFileName = null;
    var quoted = false;

    var node = ast.getFirstDescendant(CppGrammarImpl.includeBodyQuoted);
    if (node != null) {
      includedFileName = stripQuotes(node.getFirstChild().getTokenValue());
      quoted = true;
    } else if ((node = ast.getFirstDescendant(CppGrammarImpl.includeBodyBracketed)) != null) {
      node = node.getFirstDescendant(LT).getNextSibling();
      var sb = new StringBuilder(256);
      while (true) {
        String value = node.getTokenValue();
        if (">".equals(value)) {
          break;
        }
        sb.append(value);
        node = node.getNextSibling();
      }

      includedFileName = sb.toString();
    } else if ((node = ast.getFirstDescendant(CppGrammarImpl.includeBodyFreeform)) != null) {
      // expand and recurse
      String includeBody = serialize(stripEOF(node.getTokens()), "");
      String expandedIncludeBody = serialize(stripEOF(CxxLexer.create(this).lex(includeBody)), "");
      var parseError = false;
      AstNode includeBodyAst = null;
      try {
        includeBodyAst = pplineParser.parse("#include " + expandedIncludeBody);
      } catch (com.sonar.sslr.api.RecognitionException e) {
        parseError = true;
      }

      if (parseError || ((includeBodyAst != null)
                         && includeBodyAst.getFirstDescendant(CppGrammarImpl.includeBodyFreeform) != null)) {
        LOG.warn("[{}:{}]: cannot parse included filename: '{}'",
                 currFileName, token.getLine(), expandedIncludeBody);
        LOG.debug("Token : {}", token.toString());
        return null;
      }

      return findIncludedFile(includeBodyAst, token, currFileName);
    }

    if (includedFileName != null) {
      return getCodeProvider().getSourceCodeFile(includedFileName, quoted);
    }

    return null;
  }

  void handleConstantExpression(AstNode ast,Token token, String filename){
    try {
      unitCodeProvider.skipBlock(false);
      boolean result = ExpressionEvaluator.eval(this, ast.getFirstDescendant(CppGrammarImpl.constantExpression));
      unitCodeProvider.expressionWas(result);
      unitCodeProvider.skipBlock(!result);
      } catch (EvaluationException e) {
        LOG.error("[{}:{}]: error evaluating the expression {} assume 'true' ...",
                  filename, token.getLine(), token.getValue());
        unitCodeProvider.expressionWas(true);
        unitCodeProvider.skipBlock(false);
      }
  }

  PreprocessorAction handleIfLine(AstNode ast, Token token, String filename) {
    if (unitCodeProvider.doNotSkipBlock()) {
      unitCodeProvider.expressionWas(false);
      handleConstantExpression(ast, token, filename);
    } else {
      unitCodeProvider.nestedBlock(+1);
    }

    return oneConsumedToken(token);
  }

  PreprocessorAction handleElIfLine(AstNode ast, Token token, String filename) {
    // handling of an #elif line is similar to handling of an #if line but doesn't increase the nesting level
    if (unitCodeProvider.isNotNestedBlock()) {
      if (unitCodeProvider.doSkipBlock() && unitCodeProvider.expressionWasFalse()) {
        // the preceding expression had been evaluated to false
        handleConstantExpression(ast, token, filename);
      } else {
        // other block was already true: skipping tokens inside this #elif
        unitCodeProvider.skipBlock(true);
      }
    }

    return oneConsumedToken(token);
  }

  PreprocessorAction handleElseLine(Token token, String filename) {
    if (unitCodeProvider.isNotNestedBlock()) {
      if (unitCodeProvider.doSkipBlock() && unitCodeProvider.expressionWasFalse()) {
        // other block(s) were false: #else returning to non-skipping mode
        unitCodeProvider.expressionWas(true);
        unitCodeProvider.skipBlock(false);
      } else {
        // other block was true: skipping tokens inside the #else
        unitCodeProvider.skipBlock(true);
      }
    }

    return oneConsumedToken(token);
  }

  PreprocessorAction handleEndifLine(Token token, String filename) {
    if (unitCodeProvider.isNestedBlock()) {
      // nested #endif
      unitCodeProvider.nestedBlock(-1);
    } else {
      // after last #endif, switching to non-skipping mode
      unitCodeProvider.skipBlock(false);
      unitCodeProvider.expressionWas(false);
    }

    return oneConsumedToken(token);
  }

  PreprocessorAction handleDefineLine(AstNode ast, Token token, String filename) {
    // Here we have a define directive. Parse it and store the macro in a dictionary.
    Macro macro = parseMacroDefinition(ast);
    unitMacros.put(macro.name, macro);

    return oneConsumedToken(token);
  }

  PreprocessorAction handleIncludeLine(AstNode ast, Token token, String filename, Charset charset) {
    //
    // Included files have to be scanned with the (only) goal of gathering macros. This is done as follows:
    //
    // a) pipe the body of the include directive through a lexer to properly expand
    //    all macros which may be in there.
    // b) extract the filename out of the include body and try to find it
    // c) if not done yet, process it using a special lexer, which calls back only
    //    if it finds relevant preprocessor directives (currently: include's and define's)
    File includedFile = findIncludedFile(ast, token, filename);
    if (includedFile == null) {
      missingIncludeFilesCounter++;
      LOG.debug("[" + filename + ":" + token.getLine() + "]: preprocessor cannot find include file '" + token.getValue() + "'");
    } else if (analysedFiles.add(includedFile.getAbsoluteFile())) {
      unitCodeProvider.pushFileState(includedFile);
      try {
        LOG.debug("process include file '{}'", includedFile.getAbsoluteFile());
        IncludeLexer.create(this).lex(getCodeProvider().getSourceCode(includedFile, charset));
      } catch (IOException e) {
        LOG.error("[{}: preprocessor cannot read include file]: {}", includedFile.getAbsoluteFile(), e.getMessage());
      } finally {
        unitCodeProvider.popFileState();
      }
    }

    return oneConsumedToken(token);
  }

  PreprocessorAction handleImportLine(AstNode ast, Token token, String filename, Charset charset) {
    if (ast.getFirstDescendant(CppGrammarImpl.expandedIncludeBody) != null)  {
      // import <file>
      return handleIncludeLine(ast, token, filename, charset);
    }

    // forward to parser: ...  import ...
    return mapModuleTokens(ast, token);
  }

  PreprocessorAction handleModuleLine(AstNode ast, Token token) {
    // forward to parser: ...  module ...
    return mapModuleTokens(ast, token);
  }

  PreprocessorAction mapModuleTokens(AstNode ast, Token token) {
    List<Token> replTokens = new ArrayList<>();
    for (Token ppToken : stripEOF(serialize(ast))) {
      String value = ppToken.getValue();
      var type = ppToken.getType();
      Token newToken = ppToken;
      var convert = true;

      // identifier with special meaning?
//      if (type.equals(IDENTIFIER)) {
//        if (value.equals(CppSpecialIdentifier.MODULE.getValue())) {
//          type = CppSpecialIdentifier.MODULE;
//          convert = false;
//        } else if (value.equals(CppSpecialIdentifier.IMPORT.getValue())) {
//          type = CppSpecialIdentifier.IMPORT;
//          convert = false;
//        } else if (value.equals(CppSpecialIdentifier.EXPORT.getValue())) {
//          type = CppSpecialIdentifier.EXPORT;
//          convert = false;
//        }
//      }

      // convert pp token to cxx token
      if (convert) {
        List<Token> cxxTokens = CxxLexer.create().lex(value);
        newToken = cxxTokens.get(0);
        type = newToken.getType();
      }

      if (!type.equals(EOF)) {
        newToken = Token.builder()
          .setLine(token.getLine())
          .setColumn(ppToken.getColumn())
          .setURI(ppToken.getURI())
          .setValueAndOriginalValue(ppToken.getValue())
          .setType(type)
          .build();

        replTokens.add(newToken);
      }
    }

    return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),replTokens);
  }

  PreprocessorAction handleUndefLine(AstNode ast, Token token) {
    String macroName = ast.getFirstDescendant(IDENTIFIER).getTokenValue();
    unitMacros.remove(macroName);
    return oneConsumedToken(token);
  }

  PreprocessorAction handleIdentifiersAndKeywords(List<Token> tokens, Token curr, String filename) {
    //
    // Every identifier and every keyword can be a macro instance. Pipe the resulting string through a lexer to
    // create proper Tokens and to expand recursively all macros which may be in there.
    //
    PreprocessorAction ppaction = PreprocessorAction.NO_OPERATION;
    Macro macro = getMacro(curr.getValue());
    if (macro != null) {
      List<Token> replTokens = new LinkedList<>();
      var tokensConsumed = 0;

      if (macro.params == null) {
        tokensConsumed = 1;
        replTokens = new LinkedList<>(expandMacro(macro.name, serialize(evaluateHashhashOperators(macro.body))));
      } else {
        int tokensConsumedMatchingArgs = expandFunctionLikeMacro(macro.name,
                                                                 tokens.subList(1, tokens.size()),
                                                                 replTokens);
        if (tokensConsumedMatchingArgs > 0) {
          tokensConsumed = 1 + tokensConsumedMatchingArgs;
        }
      }

      if (tokensConsumed > 0) {

        // Rescanning to expand function like macros, in case it requires consuming more tokens
        List<Token> outTokens = new LinkedList<>();
        unitMacros.disable(macro.name);
        while (!replTokens.isEmpty()) {
          Token c = replTokens.get(0);
          PreprocessorAction action = PreprocessorAction.NO_OPERATION;
          if (c.getType().equals(IDENTIFIER)) {
            List<Token> rest = new ArrayList<>(replTokens);
            rest.addAll(tokens.subList(tokensConsumed, tokens.size()));
            action = handleIdentifiersAndKeywords(rest, c, filename);
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
        unitMacros.enable(macro.name);
        replTokens = reallocate(replTokens, curr);

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

  static class MismatchException extends Exception {

    private static final long serialVersionUID = 1960113363232807009L;

    MismatchException(String message) {
      super(message);
    }

    MismatchException(Throwable cause) {
      super(cause);
    }

    MismatchException(String message, Throwable cause) {
      super(message, cause);
    }

    MismatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
    }
  }

}
