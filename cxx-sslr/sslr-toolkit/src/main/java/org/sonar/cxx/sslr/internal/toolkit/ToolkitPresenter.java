/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2025 SonarOpenCommunity
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
/**
 * fork of SonarSource Language Recognizer: https://github.com/SonarSource/sslr
 * Copyright (C) 2010-2021 SonarSource SA / mailto:info AT sonarsource DOT com / license: LGPL v3
 */
package org.sonar.cxx.sslr.internal.toolkit;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.RecognitionException;
import com.sonar.cxx.sslr.xpath.api.AstNodeXPathQuery;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.sonar.cxx.sslr.toolkit.ConfigurationModel;
import org.sonar.cxx.sslr.toolkit.ConfigurationProperty;

public class ToolkitPresenter {

  private final ConfigurationModel configurationModel;
  private final SourceCodeModel model;
  private ToolkitView view = null;

  public ToolkitPresenter(ConfigurationModel configurationModel, SourceCodeModel model) {
    this.configurationModel = configurationModel;
    this.model = model;
  }

  public void setView(@Nonnull ToolkitView view) {
    Objects.requireNonNull(view);
    this.view = view;
  }

  // @VisibleForTesting
  void checkInitialized() {
    if (view == null) {
      throw new IllegalStateException("the view must be set before the presenter can be ran");
    }
  }

  // @VisibleForTesting
  void initUncaughtExceptionsHandler() {
    Thread.currentThread().setUncaughtExceptionHandler((Thread t, Throwable e) -> {
      Writer result = new StringWriter();
      var printWriter = new PrintWriter(result);
      e.printStackTrace(printWriter);

      view.appendToConsole(result.toString());
      view.setFocusOnConsoleView();
    });
  }

  // @VisibleForTesting
  void initConfigurationTab() {
    for (var configurationProperty : configurationModel.getProperties()) {
      view.addConfigurationProperty(configurationProperty.getName(), configurationProperty.getDescription());
      view.setConfigurationPropertyValue(configurationProperty.getName(), configurationProperty.getValue());
    }
  }

  public void run(String title) {
    checkInitialized();

    initUncaughtExceptionsHandler();

    view.setTitle(title);
    view.displayHighlightedSourceCode("");
    view.displayAst(null);
    view.displayXml("");
    view.disableXPathEvaluateButton();

    initConfigurationTab();

    view.run();
  }

  public void onSourceCodePasteButtonClick() {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable contents = clipboard.getContents(null);
    if ((contents != null) && (contents.isDataFlavorSupported(DataFlavor.stringFlavor))) {
      String txtToParse;
      try {
        txtToParse = (String) contents.getTransferData(DataFlavor.stringFlavor);
      } catch (UnsupportedFlavorException | IOException e) {
        throw new RuntimeException(e);
      }
      try {
        model.setSourceCode(txtToParse);
      } catch (RecognitionException e) {
        // ignore parsing errors
      }
      view.displayHighlightedSourceCode(model.getHighlightedSourceCode());
    }
  }

  public void onSourceCodeOpenButtonClick() {
    var fileToParse = view.pickFileToParse();
    if (fileToParse != null) {
      view.clearConsole();
      try {
        view.displayHighlightedSourceCode(new String(Files.readAllBytes(Path.of(fileToParse.getPath())),
          configurationModel.getCharset()));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      try {
        model.setSourceCode(fileToParse, configurationModel.getCharset());
      } catch (RecognitionException e) {
        // ignore parsing errors
      }
      view.displayHighlightedSourceCode(model.getHighlightedSourceCode());
    }
  }

  public void onSourceCodeParseButtonClick() {
    view.clearConsole();
    var sourceCode = view.getSourceCode();
    model.setSourceCode(sourceCode);
    var sourceCodeScrollbarPosition = view.getSourceCodeScrollbarPosition();
    view.displayHighlightedSourceCode(model.getHighlightedSourceCode());
    view.displayAst(model.getAstNode());
    view.displayXml(model.getXml());
    view.scrollSourceCodeTo(sourceCodeScrollbarPosition);
    view.setFocusOnAbstractSyntaxTreeView();
    view.enableXPathEvaluateButton();
  }

  public void onXPathEvaluateButtonClick() {
    var xpath = view.getXPath();
    var xpathQuery = AstNodeXPathQuery.create(xpath);

    view.clearConsole();
    view.clearAstSelections();
    view.clearSourceCodeHighlights();

    AstNode firstAstNode = null;
    for (var resultObject : xpathQuery.selectNodes(model.getAstNode())) {
      if (resultObject instanceof AstNode resultAstNode) {
        if (firstAstNode == null) {
          firstAstNode = resultAstNode;
        }

        view.selectAstNode(resultAstNode);
        view.highlightSourceCode(resultAstNode);
      }
    }

    view.scrollAstTo(firstAstNode);
    view.scrollSourceCodeTo(firstAstNode);

    view.setFocusOnAbstractSyntaxTreeView();
  }

  public void onSourceCodeKeyTyped() {
    view.displayAst(null);
    view.displayXml("");
    view.clearSourceCodeHighlights();
    view.disableXPathEvaluateButton();
  }

  public void onSourceCodeTextCursorMoved() {
    view.clearAstSelections();
    var astNode = view.getAstNodeFollowingCurrentSourceCodeTextCursorPosition();
    view.selectAstNode(astNode);
    view.scrollAstTo(astNode);
  }

  public void onAstSelectionChanged() {
    view.clearSourceCodeHighlights();

    AstNode firstAstNode = null;

    for (var astNode : view.getSelectedAstNodes()) {
      if (firstAstNode == null) {
        firstAstNode = astNode;
      }

      view.highlightSourceCode(astNode);
    }

    view.scrollSourceCodeTo(firstAstNode);
  }

  public void onConfigurationPropertyFocusLost(String name) {
    var configurationProperty = getConfigurationPropertyByName(name);
    if (configurationProperty == null) {
      throw new IllegalArgumentException("No such configuration property: " + name);
    }

    var newValueCandidate = view.getConfigurationPropertyValue(name);
    var errorMessage = configurationProperty.validate(newValueCandidate);

    view.setConfigurationPropertyErrorMessage(configurationProperty.getName(), errorMessage);

    if ("".equals(errorMessage)) {
      configurationProperty.setValue(newValueCandidate);
      configurationModel.setUpdatedFlag();
    } else {
      view.setFocusOnConfigurationPropertyField(name);
      view.setFocusOnConfigurationView();
    }
  }

  @CheckForNull
  private ConfigurationProperty getConfigurationPropertyByName(String name) {
    for (var configurationProperty : configurationModel.getProperties()) {
      if (name.equals(configurationProperty.getName())) {
        return configurationProperty;
      }
    }

    return null;
  }

}
