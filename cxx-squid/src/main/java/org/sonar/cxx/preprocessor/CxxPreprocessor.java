/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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
import com.sonar.sslr.api.AstNodeType;
import static com.sonar.sslr.api.GenericTokenType.EOF;
import static com.sonar.sslr.api.GenericTokenType.IDENTIFIER;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Preprocessor;
import com.sonar.sslr.api.PreprocessorAction;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.TokenType;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import static org.sonar.cxx.api.CppKeyword.IFDEF;
import static org.sonar.cxx.api.CppKeyword.IFNDEF;
import static org.sonar.cxx.api.CppPunctuator.BR_RIGHT;
import static org.sonar.cxx.api.CppPunctuator.COMMA;
import static org.sonar.cxx.api.CppPunctuator.HASH;
import static org.sonar.cxx.api.CppPunctuator.HASHHASH;
import static org.sonar.cxx.api.CppPunctuator.LT;
import static org.sonar.cxx.api.CxxTokenType.NUMBER;
import static org.sonar.cxx.api.CxxTokenType.PREPROCESSOR;
import static org.sonar.cxx.api.CxxTokenType.STRING;
import static org.sonar.cxx.api.CxxTokenType.WS;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.lexer.CxxLexer;
import static org.sonar.cxx.preprocessor.CppGrammar.defineLine;
import static org.sonar.cxx.preprocessor.CppGrammar.elifLine;
import static org.sonar.cxx.preprocessor.CppGrammar.elseLine;
import static org.sonar.cxx.preprocessor.CppGrammar.endifLine;
import static org.sonar.cxx.preprocessor.CppGrammar.ifLine;
import static org.sonar.cxx.preprocessor.CppGrammar.ifdefLine;
import static org.sonar.cxx.preprocessor.CppGrammar.includeLine;
import static org.sonar.cxx.preprocessor.CppGrammar.undefLine;
import org.sonar.squidbridge.SquidAstVisitorContext;

public class CxxPreprocessor extends Preprocessor {

  private static final Logger LOG = Loggers.get(CxxPreprocessor.class);

  private final SquidAstVisitorContext<Grammar> context;
  private final CxxSquidConfiguration squidConfig;
  private final SourceCodeProvider mockCodeProvider;

  private MapChain<String, Macro> unitMacros = null;

  private SourceCodeProvider unitCodeProvider;
  private File currentContextFile;

  private final Set<File> analysedFiles = new HashSet<>();
  private final Parser<Grammar> pplineParser;

  private static final String MISSING_INCLUDE_MSG =
     "Preprocessor: {} include directive error(s). "
     + "This is only relevant if parser creates syntax errors."
     + " The preprocessor searches for include files in the with "
     + "'sonar.cxx.includeDirectories' defined directories and order.";
  private static final String VARIADICPARAMETER = "__VA_ARGS__";
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
    pplineParser = CppParser.create(squidConfig);

    if (this.mockCodeProvider != null) {
      this.mockCodeProvider.setIncludeRoots(
        this.squidConfig.getValues(CxxSquidConfiguration.GLOBAL, CxxSquidConfiguration.INCLUDE_DIRECTORIES),
        this.squidConfig.getBaseDir());
    }

    addPredefinedMacros();
  }

  /**
   * Method called before the lexing starts which can be overridden to initialize a state for instance.
   *
   * Method can be called:
   * a) for a new "physical" file (including SquidAstVisitorContext mock)
   * b) while processing of #include directive.
   */
  @Override
  public void init() {
    // make sure, that the following code is executed for a new file only
    if (currentContextFile != context.getFile()) {

      // In case "physical" file is preprocessed, SquidAstVisitorContext::getFile() cannot return null.
      // Did you forget to setup the mock properly?
      currentContextFile = context.getFile();
      Objects.requireNonNull(currentContextFile, "SquidAstVisitorContext::getFile() must be non-null");

      unitCodeProvider = new SourceCodeProvider();

      // unit specific include directories
      unitCodeProvider.setIncludeRoots(
        squidConfig.getValues(currentContextFile.getAbsolutePath(), CxxSquidConfiguration.INCLUDE_DIRECTORIES),
        squidConfig.getBaseDir());

      // unit specific macros
      unitMacros = new MapChain<>();
      addUnitMacros();
      parseForcedIncludes();
    }
  }

  @Override
  public PreprocessorAction process(List<Token> tokens) { //TODO: deprecated PreprocessorAction
    Token token = tokens.get(0);
    TokenType ttype = token.getType();
    String rootFilePath = getFileUnderAnalysis().getAbsolutePath();

    if (ttype.equals(PREPROCESSOR)) {

      AstNode lineAst;
      try {
        lineAst = pplineParser.parse(token.getValue()).getFirstChild();
      } catch (com.sonar.sslr.api.RecognitionException e) {
        LOG.warn("Cannot parse '{}', ignoring...", token.getValue());
        LOG.debug("Parser exception: '{}'", e);
        return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
                                      new ArrayList<>());
      }

      AstNodeType lineKind = lineAst.getType();

      if (lineKind.equals(ifdefLine)) {
        return handleIfdefLine(lineAst, token, rootFilePath);
      } else if (lineKind.equals(ifLine)) {
        return handleIfLine(lineAst, token, rootFilePath);
      } else if (lineKind.equals(endifLine)) {
        return handleEndifLine(token, rootFilePath);
      } else if (lineKind.equals(elseLine)) {
        return handleElseLine(token, rootFilePath);
      } else if (lineKind.equals(elifLine)) {
        return handleElIfLine(lineAst, token, rootFilePath);
      }

      if (unitCodeProvider.doSkipBlock()) {
        return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
                                      new ArrayList<>());
      }

      if (lineKind.equals(defineLine)) {
        return handleDefineLine(lineAst, token, rootFilePath);
      } else if (lineKind.equals(includeLine)) {
        return handleIncludeLine(lineAst, token, rootFilePath, squidConfig.getCharset());
      } else if (lineKind.equals(undefLine)) {
        return handleUndefLine(lineAst, token);
      }

      // Ignore all other preprocessor directives (which are not handled explicitly) and strip them from the stream
      return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
                                    new ArrayList<>());
    }

    if (!ttype.equals(EOF)) {
      if (unitCodeProvider.doSkipBlock()) {
        return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
                                      new ArrayList<>());
      }

      if (!ttype.equals(STRING) && !ttype.equals(NUMBER)) {
        return handleIdentifiersAndKeywords(tokens, token, rootFilePath);
      }
    }

    return PreprocessorAction.NO_OPERATION;
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
    int nestingLevel = 0;
    int tokensConsumed = 0;
    int noTokens = tokens.size();
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
    boolean addBlank = false;
    boolean ignoreNextBlank = false;
    for (var i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
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
    AstNode ast = defineLineAst.getFirstChild();
    AstNode nameNode = ast.getFirstDescendant(CppGrammar.ppToken);
    String macroName = nameNode.getTokenValue();

    AstNode paramList = ast.getFirstDescendant(CppGrammar.parameterList);
    List<Token> macroParams = paramList == null
                                ? "objectlikeMacroDefinition".equals(ast.getName()) ? null : new LinkedList<>()
                                : getParams(paramList);

    AstNode vaargs = ast.getFirstDescendant(CppGrammar.variadicparameter);
    if ((vaargs != null) && (macroParams != null)) {
      AstNode identifier = vaargs.getFirstChild(IDENTIFIER);
      macroParams.add(identifier == null
                        ? Token.builder()
          .setLine(vaargs.getToken().getLine())
          .setColumn(vaargs.getToken().getColumn())
          .setURI(vaargs.getToken().getURI())
          .setValueAndOriginalValue(VARIADICPARAMETER)
          .setType(IDENTIFIER)
          .setGeneratedCode(true)
          .build()
                        : identifier.getToken());
    }

    AstNode replList = ast.getFirstDescendant(CppGrammar.replacementList);
    List<Token> macroBody = replList == null
                              ? new LinkedList<>() : replList.getTokens().subList(0, replList.getTokens().size() - 1);

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

    LOG.debug("finished preprocessing '{}'", file);

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
    File file = getFileUnderAnalysis();
    return findIncludedFile(exprAst, exprAst.getToken(), file.getAbsolutePath()) != null;
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

  private void addUnitMacros() {
    var defines = squidConfig.getValues(currentContextFile.getAbsolutePath(), CxxSquidConfiguration.DEFINES);
    Collections.reverse(defines);
    unitMacros.putAll(parseMacroDefinitions(defines));
  }

  private PreprocessorAction handleIfdefLine(AstNode ast, Token token, String filename) {
    if (unitCodeProvider.doNotSkipBlock()) {
      Macro macro = getMacro(getMacroName(ast));
      TokenType tokType = ast.getToken().getType();
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

    return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
                                  new ArrayList<>());
  }

  private void parseIncludeLine(String includeLine, String filename, Charset charset) {
    AstNode includeAst = pplineParser.parse(includeLine);
    handleIncludeLine(includeAst, includeAst.getFirstDescendant(CppGrammar.includeBodyQuoted)
                      .getToken(), filename, charset);
  }

  private int expandFunctionLikeMacro(String macroName, List<Token> restTokens, List<Token> expansion) {
    List<Token> arguments = new ArrayList<>();
    int tokensConsumedMatchingArgs = matchArguments(restTokens, arguments);

    Macro macro = getMacro(macroName);
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
      List<Token> replTokens = replaceParams(macro.body, macro.params, arguments);
      replTokens = evaluateHashhashOperators(replTokens);
      expansion.addAll(expandMacro(macro.name, serialize(replTokens)));
    }

    return tokensConsumedMatchingArgs;
  }

  /**
   * Parse the configured forced includes and store into the macro library.
   */
  private void parseForcedIncludes() {
    for (var include :  squidConfig.getValues(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES,
                                                  CxxSquidConfiguration.FORCE_INCLUDES)) {
      if (!include.isEmpty()) {
        LOG.debug("parsing force include: '{}'", include);
        parseIncludeLine("#include \"" + include + "\"", "sonar.cxx.forceIncludes",
                         squidConfig.getCharset());
      }
    }
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

  private List<Token> replaceParams(List<Token> body, List<Token> parameters, List<Token> arguments) {
    // replace all parameters by according arguments "Stringify" the argument if the according parameter is
    // preceded by an #

    var newTokens = new ArrayList<Token>();
    if (!body.isEmpty()) {
      var defParamValues = new ArrayList<String>();
      for (var t : parameters) {
        defParamValues.add(t.getValue());
      }

      boolean tokenPastingLeftOp = false;
      boolean tokenPastingRightOp = false;

      for (var i = 0; i < body.size(); ++i) {
        Token curr = body.get(i);
        int index = defParamValues.indexOf(curr.getValue());
        if (index == -1) {
          if (tokenPastingRightOp && !curr.getType().equals(WS) && !curr.getType().equals(HASHHASH)) {
            tokenPastingRightOp = false;
          }
          newTokens.add(curr);
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

          if (newValue.isEmpty() && VARIADICPARAMETER.equals(curr.getValue())) {
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
            newTokens.add(Token.builder().setLine(replacement.getLine()).setColumn(replacement.getColumn())
              .setURI(replacement.getURI()).setValueAndOriginalValue(newValue).setType(replacement.getType())
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
          newTokens.add(n, Token.builder().setLine(newTokens.get(n).getLine()).setColumn(newTokens.get(n).getColumn())
                        .setURI(newTokens.get(n).getURI()).setValueAndOriginalValue("\"\"").setType(STRING)
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
      .getFirstDescendant(CppGrammar.defineLine));
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

      String defineString = "#define " + define;

      LOG.debug("parsing external macro: '{}'", defineString);
      Macro macro = parseMacroDefinition(defineString);

      if (macro != null) {
        LOG.debug("storing external macro: '{}'", macro);
        result.put(macro.name, macro);
      }
    }
    return result;
  }

  @CheckForNull
  private File findIncludedFile(AstNode ast, Token token, String currFileName) {
    String includedFileName = null;
    boolean quoted = false;

    AstNode node = ast.getFirstDescendant(CppGrammar.includeBodyQuoted);
    if (node != null) {
      includedFileName = stripQuotes(node.getFirstChild().getTokenValue());
      quoted = true;
    } else if ((node = ast.getFirstDescendant(CppGrammar.includeBodyBracketed)) != null) {
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
    } else if ((node = ast.getFirstDescendant(CppGrammar.includeBodyFreeform)) != null) {
      // expand and recurse
      String includeBody = serialize(stripEOF(node.getTokens()), "");
      String expandedIncludeBody = serialize(stripEOF(CxxLexer.create(this).lex(includeBody)), "");
      boolean parseError = false;
      AstNode includeBodyAst = null;
      try {
        includeBodyAst = pplineParser.parse("#include " + expandedIncludeBody);
      } catch (com.sonar.sslr.api.RecognitionException e) {
        parseError = true;
      }

      if (parseError || ((includeBodyAst != null)
                         && includeBodyAst.getFirstDescendant(CppGrammar.includeBodyFreeform) != null)) {
        LOG.warn("[{}:{}]: cannot parse included filename: '{}'",
                 currFileName, token.getLine(), expandedIncludeBody);
        LOG.debug("Token : {}", token.toString());
        return null;
      }

      return findIncludedFile(includeBodyAst, token, currFileName);
    }

    if (includedFileName != null) {
      File file = getFileUnderAnalysis();
      String dir = file.getParent();
      return getCodeProvider().getSourceCodeFile(includedFileName, dir, quoted);
    }

    return null;
  }

  private File getFileUnderAnalysis() {
    if (unitCodeProvider.getIncludeUnderAnalysis() != null) {
      // a) CxxPreprocessor is called recursively in order to parse the #include directive.
      //    Return path to the included file.
      return unitCodeProvider.getIncludeUnderAnalysis();
    } else {
      // b) CxxPreprocessor is called in the ordinary mode: it is preprocessing the file, tracked in
      //    org.sonar.squidbridge.SquidAstVisitorContext. This file cannot be null. If it is null - you forgot to
      //    setup the test mock.
      Objects.requireNonNull(context.getFile(), "SquidAstVisitorContext::getFile() must be non-null");
      return context.getFile();
    }
  }

  void handleConstantExpression(AstNode ast,Token token, String filename){
    try {
      unitCodeProvider.skipBlock(false);
      boolean result = ExpressionEvaluator.eval(squidConfig, this, ast.getFirstDescendant(CppGrammar.constantExpression));
      unitCodeProvider.expressionWas(result);
      unitCodeProvider.skipBlock(!result);
      } catch (EvaluationException e) {
        LOG.error("[{}:{}]: error evaluating the expression {} assume 'true' ...",
                  filename, token.getLine(), token.getValue());
        LOG.error("{}", e);
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

    return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
                                  new ArrayList<>());
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

    return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
                                  new ArrayList<>());
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

    return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
                                  new ArrayList<>());
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

    return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
                                  new ArrayList<>());
  }

  PreprocessorAction handleDefineLine(AstNode ast, Token token, String filename) {
    // Here we have a define directive. Parse it and store the macro in a dictionary.
    Macro macro = parseMacroDefinition(ast);
    unitMacros.put(macro.name, macro);

    return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
                                  new ArrayList<>());
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
      LOG.debug("[" + filename + ":" + token.getLine() + "]: cannot find include file '" + token.getValue() + "'");
    } else if (analysedFiles.add(includedFile.getAbsoluteFile())) {
      unitCodeProvider.pushFileState(includedFile);
      try {
        IncludeLexer.create(this).lex(getCodeProvider().getSourceCode(includedFile, charset));
      } catch (IOException e) {
        LOG.error("[{}: Cannot read include file]: {}", includedFile.getAbsoluteFile(), e);
      } finally {
        unitCodeProvider.popFileState();
      }
    }

    return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
                                  new ArrayList<>());
  }

  PreprocessorAction handleUndefLine(AstNode ast, Token token) {
    String macroName = ast.getFirstDescendant(IDENTIFIER).getTokenValue();
    unitMacros.remove(macroName);
    return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
                                  new ArrayList<>());
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
      int tokensConsumed = 0;

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
