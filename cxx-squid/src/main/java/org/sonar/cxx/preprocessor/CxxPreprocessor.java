/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection; //@todo: deprecated, see http://javadocs.sonarsource.org/4.5.2/apidocs/deprecated-list.html
import java.util.Collections; //@todo: deprecated, see http://javadocs.sonarsource.org/4.5.2/apidocs/deprecated-list.html
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxCompilationUnitSettings;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.CxxLanguage;
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

  private static final String CPLUSPLUS = "__cplusplus";
  private static final String EVALUATED_TO_FALSE = "[{}:{}]: '{}' evaluated to false, skipping tokens that follow";
  private final CxxLanguage language;
  private File currentContextFile;
  private String rootFilePath;

//@todo: deprecated Preprocessor
  private static class State {

    private boolean skipPreprocessorDirectives;
    private boolean conditionWasTrue;
    private int conditionalInclusionCounter;
    private File includeUnderAnalysis;

    public State(@Nullable File includeUnderAnalysis) {
      this.skipPreprocessorDirectives = false;
      this.conditionWasTrue = false;
      this.conditionalInclusionCounter = 0;
      this.includeUnderAnalysis = includeUnderAnalysis;
    }

    /**
     * reset preprocessor state
     */
    public final void reset() {
      skipPreprocessorDirectives = false;
      conditionWasTrue = false;
      conditionalInclusionCounter = 0;
      includeUnderAnalysis = null;
    }
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

  static final class Macro {

    private final String name;
    private final List<Token> params;
    private final List<Token> body;
    private final boolean isVariadic;

    public Macro(String name, @Nullable List<Token> params, @Nullable List<Token> body, boolean variadic) {
      this.name = name;
      if (params == null) {
        this.params = null;
      } else {
        this.params = params.stream().collect(Collectors.toList());
      }
      if (body == null) {
        this.body = null;
      } else {
        this.body = body.stream().collect(Collectors.toList());
      }
      this.isVariadic = variadic;
    }

    @Override
    public String toString() {
      return name
        + (params == null ? "" : "(" + serialize(params, ", ") + (isVariadic ? "..." : "") + ")")
        + " -> '" + serialize(body) + "'";
    }

    public boolean checkArgumentsCount(int count) {
      return isVariadic
        ? count >= params.size() - 1
        : count == params.size();
    }
  }

  private static final Logger LOG = Loggers.get(CxxPreprocessor.class);
  private Parser<Grammar> pplineParser;
  private final MapChain<String, Macro> fixedMacros = new MapChain<>();
  private MapChain<String, Macro> unitMacros;
  private final Set<File> analysedFiles = new HashSet<>();
  private SourceCodeProvider codeProvider = new SourceCodeProvider();
  private SourceCodeProvider unitCodeProvider;
  private SquidAstVisitorContext<Grammar> context;
  private ExpressionEvaluator ifExprEvaluator;
  private List<String> cFilesPatterns;
  private CxxConfiguration conf;
  private CxxCompilationUnitSettings compilationUnitSettings;
  private static final String VARIADICPARAMETER = "__VA_ARGS__";

  public static class Include {

    private final int line;
    private final String path;

    Include(int line, String path) {
      this.line = line;
      this.path = path;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Include that = (Include) o;

      if (line != that.line) {
        return false;
      }
      if (path != null ? !path.equals(that.path) : that.path != null) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return 31 * line + (path != null ? path.hashCode() : 0);
    }

    public String getPath() {
      return path;
    }

    public int getLine() {
      return line;
    }
  }
  private final Multimap<String, Include> includedFiles = HashMultimap.create();
  private final Multimap<String, Include> missingIncludeFiles = HashMultimap.create();

  private State currentFileState = new State(null);
  private final Deque<State> globalStateStack = new LinkedList<>();

  public CxxPreprocessor(SquidAstVisitorContext<Grammar> context, CxxLanguage language) {
    this(context, new CxxConfiguration(), language);
  }

  public CxxPreprocessor(SquidAstVisitorContext<Grammar> context, CxxConfiguration conf, CxxLanguage language) {
    this(context, conf, new SourceCodeProvider(), language);
  }

  public CxxPreprocessor(SquidAstVisitorContext<Grammar> context,
    CxxConfiguration conf,
    SourceCodeProvider sourceCodeProvider,
    CxxLanguage language) {
    this.context = context;
    this.ifExprEvaluator = new ExpressionEvaluator(conf, this);
    this.cFilesPatterns = conf.getCFilesPatterns();
    this.conf = conf;
    this.language = language;

    codeProvider = sourceCodeProvider;
    codeProvider.setIncludeRoots(conf.getIncludeDirectories(), conf.getBaseDir());

    pplineParser = CppParser.create(conf);

    try {
      getMacros().setHighPrio(true);

      // parse the configured defines and store into the macro library
      for (String define : conf.getDefines()) {
        LOG.debug("parsing external macro: '{}'", define);
        if (!"".equals(define)) {
          Macro macro = parseMacroDefinition("#define " + define);
          LOG.debug("storing external macro: '{}'", macro);
          getMacros().put(macro.name, macro);
        }
      }

      // set standard macros
      registerMacros(StandardDefinitions.macros());

      // parse the configured force includes and store into the macro library
      for (String include : conf.getForceIncludeFiles()) {
        LOG.debug("parsing force include: '{}'", include);
        if (!"".equals(include)) {
          parseIncludeLine("#include \"" + include + "\"", "sonar." + this.language.getPropertiesKey()
            + ".forceIncludes", conf.getEncoding());
        }
      }
    } finally {
      getMacros().setHighPrio(false);
    }
  }

  private void registerMacros(Map<String, String> standardMacros) {
    for (Map.Entry<String, String> entry : standardMacros.entrySet()) {
      Token bodyToken;
      try {
        bodyToken = Token.builder()
          .setLine(1)
          .setColumn(0)
          .setURI(new java.net.URI(""))
          .setValueAndOriginalValue(entry.getValue())
          .setType(STRING)
          .build();
      } catch (java.net.URISyntaxException e) {
        throw new PreprocessorRuntimeException("URI cannot be handled", e);
      }

      getMacros().put(entry.getKey(), new Macro(entry.getKey(), null, Collections.singletonList(bodyToken), false));
    }
  }

  public Collection<Include> getIncludedFiles(File file) {
    return includedFiles.get(file.getPath());
  }

  public Collection<Include> getMissingIncludeFiles(File file) {
    return missingIncludeFiles.get(file.getPath());
  }

  private boolean isCFile(String filePath) {
    for (String pattern : cFilesPatterns) {
      String patt = pattern.replace("*", "");
      if (filePath.endsWith(patt)) {
        LOG.debug("Parse '{}' as C file, ends in '{}'", filePath, pattern);
        return true;
      }
    }
    LOG.debug("Parse '{}' as C++ file", filePath);
    return false;
  }

  @Override
  public PreprocessorAction process(List<Token> tokens) { //TODO: deprecated PreprocessorAction
    Token token = tokens.get(0);
    TokenType ttype = token.getType();

    File file = getFileUnderAnalysis();
    rootFilePath = file == null ? token.getURI().toString() : file.getAbsolutePath();

    if (context.getFile() != currentContextFile) {
      currentContextFile = context.getFile();
      compilationUnitSettings = conf.getCompilationUnitSettings(currentContextFile.getAbsolutePath());

      if (compilationUnitSettings != null) {
        LOG.debug("compilation unit settings for: '{}'", rootFilePath);
      } else {
        compilationUnitSettings = conf.getGlobalCompilationUnitSettings();

        if (compilationUnitSettings != null) {
          LOG.debug("global compilation unit settings for: '{}'", rootFilePath);
        }
      }

      if (compilationUnitSettings != null) {
        // Use compilation unit settings
        unitCodeProvider = new SourceCodeProvider();
        unitCodeProvider.setIncludeRoots(compilationUnitSettings.getIncludes(), conf.getBaseDir());

        unitMacros = new MapChain<>();

        try {
          // Treat all global defines as high prio
          getMacros().setHighPrio(true);

          // parse the configured defines and store into the macro library
          for (String define : conf.getDefines()) {
            LOG.debug("parsing external macro to unit: '{}'", define);
            if (!"".equals(define)) {
              Macro macro = parseMacroDefinition("#define " + define);
              if (macro != null) {
                LOG.debug("storing external macro to unit: '{}'", macro);
                getMacros().put(macro.name, macro);
              }
            }
          }

          // set standard macros
          // using smaller set of defines as rest is provides by compilation unit settings
          HashMap<String, String> defines = new HashMap<>();
          defines.put("__FILE__", "\"file\"");
          defines.put("__LINE__", "1");
          defines.put("__DATE__", "\"??? ?? ????\"");
          defines.put("__TIME__", "\"??:??:??\"");
          registerMacros(defines);

          // parse the configured force includes and store into the macro library
          for (String include : conf.getForceIncludeFiles()) {
            LOG.debug("parsing force include to unit: '{}'", include);
            if (!"".equals(include)) {
              // TODO -> this needs to come from language
              parseIncludeLine("#include \"" + include + "\"", "sonar.cxx.forceIncludes", conf.getEncoding());
            }
          }

          // rest of defines comes from compilation unit settings
          registerMacros(compilationUnitSettings.getDefines());
        } finally {
          getMacros().setHighPrio(false);
        }

        if (getMacro(CPLUSPLUS) == null) {
          //Create macros to replace C++ keywords when parsing C files
          registerMacros(StandardDefinitions.compatibilityMacros());
        }
      } else {
        // Use global settings
        LOG.debug("global settings for: '{}'", rootFilePath);
        if (isCFile(currentContextFile.getAbsolutePath())) {
          //Create macros to replace C++ keywords when parsing C files
          registerMacros(StandardDefinitions.compatibilityMacros());
          fixedMacros.disable(CPLUSPLUS);
        } else {
          fixedMacros.enable(CPLUSPLUS);
        }
      }
    }

    if (ttype.equals(PREPROCESSOR)) {

      AstNode lineAst;
      try {
        lineAst = pplineParser.parse(token.getValue()).getFirstChild();
      } catch (com.sonar.sslr.api.RecognitionException re) {
        LOG.warn("Cannot parse '{}', ignoring...", token.getValue());
        LOG.debug("Parser exception: '{}'", re);
        return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
          new ArrayList<Token>()); //TODO: deprecated PreprocessorAction
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

      if (currentFileState.skipPreprocessorDirectives) {
        return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
          new ArrayList<Token>()); //TODO: deprecated PreprocessorAction
      }

      if (lineKind.equals(defineLine)) {
        return handleDefineLine(lineAst, token, rootFilePath);
      } else if (lineKind.equals(includeLine)) {
        return handleIncludeLine(lineAst, token, rootFilePath, conf.getCharset());
      } else if (lineKind.equals(undefLine)) {
        return handleUndefLine(lineAst, token);
      }

      // Ignore all other preprocessor directives (which are not handled explicitly)
      // and strip them from the stream
      return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
        new ArrayList<Token>()); //TODO: deprecated PreprocessorAction
    }

    if (!ttype.equals(EOF)) {
      if (currentFileState.skipPreprocessorDirectives) {
        return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
          new ArrayList<Token>()); //TODO: deprecated PreprocessorAction
      }

      if (!ttype.equals(STRING) && !ttype.equals(NUMBER)) {
        return handleIdentifiersAndKeywords(tokens, token, rootFilePath);
      }
    }

    return PreprocessorAction.NO_OPERATION; //TODO: deprecated PreprocessorAction
  }

  public void finishedPreprocessing(File file) {
    // From 16.3.5 "Scope of macro definitions":
    // A macro definition lasts (independent of block structure) until
    // a corresponding #undef directive is encountered or (if none
    // is encountered) until the end of the translation unit.

    LOG.debug("finished preprocessing '{}'", file);

    analysedFiles.clear();
    fixedMacros.clearLowPrio();
    unitMacros = null;
    compilationUnitSettings = null;
    unitCodeProvider = null;
    currentFileState.reset();
    currentContextFile = null;
  }

  public SourceCodeProvider getCodeProvider() {
    return unitCodeProvider != null ? unitCodeProvider : codeProvider;
  }

  public final MapChain<String, Macro> getMacros() {
    return unitMacros != null ? unitMacros : fixedMacros;
  }

  public final Macro getMacro(String macroname) {
    return getMacros().get(macroname);
  }

  public String valueOf(String macroname) {
    String result = null;
    Macro macro = getMacro(macroname);
    if (macro != null) {
      result = serialize(macro.body);
    }
    return result;
  }

  PreprocessorAction handleIfLine(AstNode ast, Token token, String filename) { //TODO: deprecated PreprocessorAction
    if (!currentFileState.skipPreprocessorDirectives) {
      currentFileState.conditionWasTrue = false;
      if (LOG.isTraceEnabled()) {
        LOG.trace("[{}:{}]: handling #if line '{}'",
          filename, token.getLine(), token.getValue());
      }
      try {
        currentFileState.skipPreprocessorDirectives = false;
        currentFileState.skipPreprocessorDirectives = !ifExprEvaluator.eval(
          ast.getFirstDescendant(CppGrammar.constantExpression));
      } catch (EvaluationException e) {
        LOG.error("[{}:{}]: error evaluating the expression {} assume 'true' ...",
          filename, token.getLine(), token.getValue());
        LOG.error("{}", e);
        currentFileState.skipPreprocessorDirectives = false;
      }

      if (currentFileState.skipPreprocessorDirectives) {
        if (LOG.isTraceEnabled()) {
          LOG.trace(EVALUATED_TO_FALSE, filename, token.getLine(), token.getValue());
        }
      } else {
        currentFileState.conditionWasTrue = true;
      }
    } else {
      currentFileState.conditionalInclusionCounter++;
    }

    return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
      new ArrayList<Token>()); //TODO: deprecated PreprocessorAction
  }

  PreprocessorAction handleElIfLine(AstNode ast, Token token, String filename) { //TODO: deprecated PreprocessorAction
    // Handling of an elif line is similar to handling of an if line but doesn't increase the nesting level
    if (currentFileState.conditionalInclusionCounter == 0) {
      //the preceding clauses had been evaluated to false
      if (currentFileState.skipPreprocessorDirectives && !currentFileState.conditionWasTrue) {
        try {
          if (LOG.isTraceEnabled()) {
            LOG.trace("[{}:{}]: handling #elif line '{}'",
              filename, token.getLine(), token.getValue());
          }
          currentFileState.skipPreprocessorDirectives = false;
          currentFileState.skipPreprocessorDirectives = !ifExprEvaluator.eval(
            ast.getFirstDescendant(CppGrammar.constantExpression));
        } catch (EvaluationException e) {
          LOG.error("[{}:{}]: error evaluating the expression {} assume 'true' ...",
            filename, token.getLine(), token.getValue());
          LOG.error("{}", e);
          currentFileState.skipPreprocessorDirectives = false;
        }

        if (currentFileState.skipPreprocessorDirectives) {
          if (LOG.isTraceEnabled()) {
            LOG.trace(EVALUATED_TO_FALSE, filename, token.getLine(), token.getValue());
          }
        } else {
          currentFileState.conditionWasTrue = true;
        }
      } else {
        if (LOG.isTraceEnabled()) {
          LOG.trace("[{}:{}]: skipping tokens inside the #elif", filename, token.getLine());
        }
        currentFileState.skipPreprocessorDirectives = true;
      }
    }

    return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
      new ArrayList<Token>()); //TODO: deprecated PreprocessorAction
  }

  private PreprocessorAction handleIfdefLine(AstNode ast, Token token, String filename) { //TODO: deprecated
    if (!currentFileState.skipPreprocessorDirectives) {
      Macro macro = getMacro(getMacroName(ast));
      TokenType tokType = ast.getToken().getType();
      if ((tokType.equals(IFDEF) && macro == null) || (tokType.equals(IFNDEF) && macro != null)) {
        if (LOG.isTraceEnabled()) {
          LOG.trace(EVALUATED_TO_FALSE, filename, token.getLine(), token.getValue());
        }
        currentFileState.skipPreprocessorDirectives = true;
      }
      if (!currentFileState.skipPreprocessorDirectives) {
        currentFileState.conditionWasTrue = true;
      }
    } else {
      currentFileState.conditionalInclusionCounter++;
    }

    return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
      new ArrayList<Token>()); //TODO: deprecated PreprocessorAction
  }

  PreprocessorAction handleElseLine(Token token, String filename) { //TODO: deprecated PreprocessorAction
    if (currentFileState.conditionalInclusionCounter == 0) {
      if (currentFileState.skipPreprocessorDirectives && !currentFileState.conditionWasTrue) {
        if (LOG.isTraceEnabled()) {
          LOG.trace("[{}:{}]: #else, returning to non-skipping mode", filename, token.getLine());
        }
        currentFileState.skipPreprocessorDirectives = false;
        currentFileState.conditionWasTrue = true;
      } else {
        if (LOG.isTraceEnabled()) {
          LOG.trace("[{}:{}]: skipping tokens inside the #else", filename, token.getLine());
        }
        currentFileState.skipPreprocessorDirectives = true;
      }
    }

    return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
      new ArrayList<Token>()); //TODO: deprecated PreprocessorAction
  }

  PreprocessorAction handleEndifLine(Token token, String filename) { //TODO: deprecated PreprocessorAction
    if (currentFileState.conditionalInclusionCounter > 0) {
      currentFileState.conditionalInclusionCounter--;
    } else {
      if (currentFileState.skipPreprocessorDirectives) {
        if (LOG.isTraceEnabled()) {
          LOG.trace("[{}:{}]: #endif, returning to non-skipping mode", filename, token.getLine());
        }
        currentFileState.skipPreprocessorDirectives = false;
      }
      currentFileState.conditionWasTrue = false;
    }

    return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
      new ArrayList<Token>()); //TODO: deprecated PreprocessorAction
  }

  PreprocessorAction handleDefineLine(AstNode ast, Token token, String filename) { //TODO: deprecated PreprocessorAction
    // Here we have a define directive. Parse it and store the result in a dictionary.

    Macro macro = parseMacroDefinition(ast);
    if (LOG.isTraceEnabled()) {
      LOG.trace("[{}:{}]: storing macro: '{}'", filename, token.getLine(), macro);
    }
    getMacros().put(macro.name, macro);

    return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
      new ArrayList<Token>()); //@todo: deprecated PreprocessorAction
  }

  private void parseIncludeLine(String includeLine, String filename, Charset charset) {
    AstNode includeAst = pplineParser.parse(includeLine);
    handleIncludeLine(includeAst, includeAst.getFirstDescendant(CppGrammar.includeBodyQuoted)
      .getToken(), filename, charset);
  }

  PreprocessorAction handleIncludeLine(AstNode ast, Token token, String filename, Charset charset) { //TODO: deprecated 
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

    File currentFile = this.getFileUnderAnalysis();
    if (currentFile != null && includedFile != null) {
      includedFiles.put(currentFile.getPath(), new Include(token.getLine(), includedFile.getAbsolutePath()));
    }

    if (includedFile == null) {
      if (conf.getMissingIncludeWarningsEnabled()) {
        LOG.warn("[" + filename + ":" + token.getLine() + "]: cannot find the sources for '"
          + token.getValue() + "'");
      }
      if (currentFile != null) {
        missingIncludeFiles.put(currentFile.getPath(), new Include(token.getLine(), token.getValue()));
      }
    } else if (!analysedFiles.contains(includedFile)) {
      analysedFiles.add(includedFile.getAbsoluteFile());
      if (LOG.isTraceEnabled()) {
        LOG.trace("[{}:{}]: processing {}, resolved to file '{}'",
          filename, token.getLine(), token.getValue(), includedFile.getAbsolutePath());
      }

      globalStateStack.push(currentFileState);
      currentFileState = new State(includedFile);

      try {
        IncludeLexer.create(this).lex(getCodeProvider().getSourceCode(includedFile, charset));
      } catch (IOException ex) {
        LOG.error("[{}: Cannot read file]: {}", includedFile.getAbsoluteFile(), ex);
      } finally {
        currentFileState = globalStateStack.pop();
      }
    }

    return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
      new ArrayList<Token>()); //@todo: deprecated PreprocessorAction
  }

  PreprocessorAction handleUndefLine(AstNode ast, Token token) { //@todo: deprecated PreprocessorAction
    String macroName = ast.getFirstDescendant(IDENTIFIER).getTokenValue();
    getMacros().removeLowPrio(macroName);
    return new PreprocessorAction(1, Collections.singletonList(Trivia.createSkippedText(token)),
      new ArrayList<Token>()); //@todo: deprecated PreprocessorAction
  }

  PreprocessorAction handleIdentifiersAndKeywords(List<Token> tokens, Token curr, String filename) {//@todo:deprecated
    //
    // Every identifier and every keyword can be a macro instance.
    // Pipe the resulting string through a lexer to create proper Tokens
    // and to expand recursively all macros which may be in there.
    //

    PreprocessorAction ppaction = PreprocessorAction.NO_OPERATION; //@todo: deprecated PreprocessorAction
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
        getMacros().disable(macro.name);
        while (!replTokens.isEmpty()) {
          Token c = replTokens.get(0);
          PreprocessorAction action = PreprocessorAction.NO_OPERATION; //@todo: deprecated PreprocessorAction
          if (c.getType().equals(IDENTIFIER)) {
            List<Token> rest = new ArrayList(replTokens);
            rest.addAll(tokens.subList(tokensConsumed, tokens.size()));
            action = handleIdentifiersAndKeywords(rest, c, filename);
          }
          if (action.equals(PreprocessorAction.NO_OPERATION)) { //@todo: deprecated PreprocessorAction
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
        getMacros().enable(macro.name);

        replTokens = reallocate(replTokens, curr);

        if (LOG.isTraceEnabled()) {
          LOG.trace("[{}:{}]: replacing '" + curr.getValue()
            + (tokensConsumed == 1
              ? ""
              : serialize(tokens.subList(1, tokensConsumed))) + "' -> '" + serialize(replTokens) + '\'',
            filename, curr.getLine());
        }

        ppaction = new PreprocessorAction( //@todo: deprecated PreprocessorAction
          tokensConsumed,
          Collections.singletonList(Trivia.createSkippedText(tokens.subList(0, tokensConsumed))),
          replTokens);
      }
    }

    return ppaction;
  }

  public String expandFunctionLikeMacro(String macroName, List<Token> restTokens) {
    List<Token> expansion = new LinkedList<>();
    expandFunctionLikeMacro(macroName, restTokens, expansion);
    return serialize(expansion);
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

  public Boolean expandHasIncludeExpression(AstNode exprAst) {
    File file = getFileUnderAnalysis();
    String filePath = file == null ? rootFilePath : file.getAbsolutePath();
    return findIncludedFile(exprAst, exprAst.getToken(), filePath) != null;
  }

  private List<Token> expandMacro(String macroName, String macroExpression) {
    // C++ standard 16.3.4/2 Macro Replacement - Rescanning and further replacement
    List<Token> tokens = null;
    getMacros().disable(macroName);
    try {
      tokens = stripEOF(CxxLexer.create(this).lex(macroExpression));
    } finally {
      getMacros().enable(macroName);
    }
    return tokens;
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
    StringJoiner js = new StringJoiner(spacer);
    for (Token t : tokens) {
      js.add(t.getValue());
    }
    return js.toString();
  }

  private static int matchArguments(List<Token> tokens, List<Token> arguments) {
    List<Token> rest = new ArrayList<>(tokens);
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
    } catch (MismatchException me) {
    }
    try {
      rest = match(rest, ")");
    } catch (MismatchException me) {
      LOG.error("MismatchException : '{}' rest: '{}'", me.getMessage(), rest);
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
    List<Token> matchedTokens = new LinkedList<>();

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

  private List<Token> replaceParams(List<Token> body, List<Token> parameters, List<Token> arguments) {
    // Replace all parameters by according arguments
    // "Stringify" the argument if the according parameter is preceded by an #

    List<Token> newTokens = new ArrayList<>();
    if (!body.isEmpty()) {
      List<String> defParamValues = new ArrayList<>();
      for (Token t : parameters) {
        defParamValues.add(t.getValue());
      }

      boolean tokenPastingLeftOp = false;
      boolean tokenPastingRightOp = false;

      for (int i = 0; i < body.size(); ++i) {
        Token curr = body.get(i);
        int index = defParamValues.indexOf(curr.getValue());
        if (index == -1) {
          if (tokenPastingRightOp && !curr.getType().equals(WS) && !curr.getType().equals(HASHHASH)) {
            tokenPastingRightOp = false;
          }
          newTokens.add(curr);
        } else if (index == arguments.size()) {
          // EXTENSION: GCC's special meaning of token paste operator
          // If variable argument is left out then the comma before the paste
          // operator will be deleted
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
              // If the token is a macro, the macro is not expanded - the macro
              // name is converted into a string.
              newTokens.remove(newTokens.size() - 1);
              newValue = encloseWithQuotes(quote(replacement.getValue()));
            } else {
              // otherwise the arguments have to be fully expanded before
              // expanding the body of the macro
              newValue = serialize(expandMacro("", replacement.getValue()));
            }
          }

          if (newValue.isEmpty() && VARIADICPARAMETER.equals(curr.getValue())) {
            // the Visual C++ implementation will suppress a trailing comma
            // if no arguments are passed to the ellipsis
            for (int n = newTokens.size() - 1; n != 0; n = newTokens.size() - 1) {
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
      for (int n = newTokens.size() - 2; n != 0; n--) {
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

  private static List<Token> evaluateHashhashOperators(List<Token> tokens) {
    List<Token> newTokens = new ArrayList<>();

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

  @Nullable
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

  @Nullable
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
    StringBuilder result = new StringBuilder(2 * str.length());
    boolean addBlank = false;
    boolean ignoreNextBlank = false;
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_') { // token
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
    List<Token> reallocated = new LinkedList<>();
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

  private static Macro parseMacroDefinition(AstNode defineLineAst) {
    AstNode ast = defineLineAst.getFirstChild();
    AstNode nameNode = ast.getFirstDescendant(CppGrammar.ppToken);
    String macroName = nameNode.getTokenValue();

    AstNode paramList = ast.getFirstDescendant(CppGrammar.parameterList);
    List<Token> macroParams = paramList == null
      ? "objectlikeMacroDefinition".equals(ast.getName()) ? null : new LinkedList<>() : getParams(paramList);

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
    List<Token> params = new ArrayList<>();
    for (AstNode node : identListAst.getChildren(IDENTIFIER)) {
      params.add(node.getToken());
    }

    return params;
  }

  private File findIncludedFile(AstNode ast, Token token, String currFileName) {
    String includedFileName = null;
    File includedFile = null;
    boolean quoted = false;

    AstNode node = ast.getFirstDescendant(CppGrammar.includeBodyQuoted);
    if (node != null) {
      includedFileName = stripQuotes(node.getFirstChild().getTokenValue());
      quoted = true;
    } else if ((node = ast.getFirstDescendant(CppGrammar.includeBodyBracketed)) != null) {
      node = node.getFirstDescendant(LT).getNextSibling();
      StringBuilder sb = new StringBuilder();
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
      if (LOG.isTraceEnabled()) {
        LOG.trace("Include resolve macros: includeBody '{}' - expandedIncludeBody: '{}'",
          includeBody, expandedIncludeBody);
      }

      boolean parseError = false;
      AstNode includeBodyAst = null;
      try {
        includeBodyAst = pplineParser.parse("#include " + expandedIncludeBody);
      } catch (com.sonar.sslr.api.RecognitionException re) {
        parseError = true;
      }

      if (parseError || ((includeBodyAst != null)
        && includeBodyAst.getFirstDescendant(CppGrammar.includeBodyFreeform) != null)) {
        LOG.warn("[{}:{}]: cannot parse included filename: '{}'",
          currFileName, token.getLine(), expandedIncludeBody);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Token : {}", token.toString());
        }
        return null;
      }

      return findIncludedFile(includeBodyAst, token, currFileName);
    }

    if (includedFileName != null) {
      File file = getFileUnderAnalysis();
      String dir;
      if (file != null) {
        dir = file.getParent();
      } else {
        try {
          dir = Paths.get(new URI(currFileName)).getParent().toString();
        } catch (IllegalArgumentException | FileSystemNotFoundException | SecurityException | URISyntaxException e) {
          dir = "";
        }
      }
      includedFile = getCodeProvider().getSourceCodeFile(includedFileName, dir, quoted);
    }

    return includedFile;
  }

  private static String getMacroName(AstNode ast) {
    return ast.getFirstDescendant(IDENTIFIER).getTokenValue();
  }

  private static String stripQuotes(String str) {
    return str.substring(1, str.length() - 1);
  }

  private File getFileUnderAnalysis() {
    if (currentFileState.includeUnderAnalysis == null) {
      return context.getFile();
    }
    return currentFileState.includeUnderAnalysis;
  }

  static class PreprocessorRuntimeException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -6568372484065119533L;

    public PreprocessorRuntimeException(String message) {
      super(message);
    }

    public PreprocessorRuntimeException(String message, Throwable throwable) {
      super(message, throwable);
    }
  }
}
