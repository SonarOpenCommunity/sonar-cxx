/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2024 SonarOpenCommunity
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
import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Token;
import java.awt.Point;
import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.sonar.cxx.sslr.toolkit.ConfigurationModel;
import org.sonar.cxx.sslr.toolkit.ConfigurationProperty;

class ToolkitPresenterTest {

  @Test
  void checkInitializedBad() {
    var presenter = new ToolkitPresenter(mock(ConfigurationModel.class), mock(SourceCodeModel.class));

    var thrown = catchThrowableOfType(IllegalStateException.class, presenter::checkInitialized);
    assertThat(thrown)
      .isExactlyInstanceOf(IllegalStateException.class)
      .hasMessage("the view must be set before the presenter can be ran");
  }

  @Test
  void checkInitializedGood() {
    var presenter = new ToolkitPresenter(mock(ConfigurationModel.class), mock(SourceCodeModel.class));
    presenter.setView(mock(ToolkitView.class));
    presenter.checkInitialized();
  }

  @Test
  void initUncaughtExceptionsHandler() {
    var view = mock(ToolkitView.class);

    var presenter = new ToolkitPresenter(mock(ConfigurationModel.class), mock(SourceCodeModel.class));
    presenter.setView(view);

    presenter.initUncaughtExceptionsHandler();

    var uncaughtExceptionHandler = Thread.currentThread().getUncaughtExceptionHandler();
    assertThat(uncaughtExceptionHandler).isNotInstanceOf(ThreadGroup.class);

    var e = mock(Throwable.class);

    uncaughtExceptionHandler.uncaughtException(null, e);
    verify(e).printStackTrace(any(PrintWriter.class));
    verify(view).appendToConsole(anyString());
    verify(view).setFocusOnConsoleView();
  }

  @Test
  void initConfigurationTab() {
    var view = mock(ToolkitView.class);

    var presenter = new ToolkitPresenter(mock(ConfigurationModel.class), mock(SourceCodeModel.class));
    presenter.setView(view);
    presenter.initConfigurationTab();

    verify(view, never()).addConfigurationProperty(Mockito.anyString(), Mockito.anyString());
    verify(view, never()).setConfigurationPropertyValue(Mockito.anyString(), Mockito.anyString());

    var property1 = mock(ConfigurationProperty.class);
    when(property1.getName()).thenReturn("property1");
    when(property1.getDescription()).thenReturn("description1");
    when(property1.getValue()).thenReturn("default1");

    var property2 = mock(ConfigurationProperty.class);
    when(property2.getName()).thenReturn("property2");
    when(property2.getDescription()).thenReturn("description2");
    when(property2.getValue()).thenReturn("default2");

    var configurationModel = mock(ConfigurationModel.class);
    when(configurationModel.getProperties()).thenReturn(Arrays.asList(property1, property2));
    presenter = new ToolkitPresenter(configurationModel, mock(SourceCodeModel.class));
    presenter.setView(view);
    presenter.initConfigurationTab();

    verify(view).addConfigurationProperty("property1", "description1");
    verify(view).setConfigurationPropertyValue("property1", "default1");
    verify(view).addConfigurationProperty("property2", "description2");
    verify(view).setConfigurationPropertyValue("property2", "default2");
  }

  @Test
  void run() {
    var view = mock(ToolkitView.class);

    var presenter = new ToolkitPresenter(mock(ConfigurationModel.class), mock(SourceCodeModel.class));
    presenter.setView(view);

    presenter.run("my_mocked_title");

    assertThat(Thread.currentThread().getUncaughtExceptionHandler()).isNotInstanceOf(ThreadGroup.class);
    verify(view).setTitle("my_mocked_title");
    verify(view).displayHighlightedSourceCode("");
    verify(view).displayAst(null);
    verify(view).displayXml("");
    verify(view).disableXPathEvaluateButton();
    verify(view).run();
  }

  @Test
  void runShouldCallInitConfigurationTab() {
    var view = mock(ToolkitView.class);

    var presenter = new ToolkitPresenter(mock(ConfigurationModel.class), mock(SourceCodeModel.class));
    presenter.setView(view);
    presenter.run("my_mocked_title");
    verify(view, never()).addConfigurationProperty(Mockito.anyString(), Mockito.anyString());

    var configurationModel = mock(ConfigurationModel.class);
    when(configurationModel.getProperties()).thenReturn(Collections.singletonList(mock(ConfigurationProperty.class)));
    presenter = new ToolkitPresenter(configurationModel, mock(SourceCodeModel.class));
    presenter.setView(view);
    presenter.run("my_mocked_title");
    verify(view).addConfigurationProperty(any(), any());
  }

  @Test
  void runFailsWithoutView() {
    var thrown = catchThrowableOfType(IllegalStateException.class, () -> {
      new ToolkitPresenter(mock(ConfigurationModel.class), mock(SourceCodeModel.class)).run("foo");
    });
    assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  void onSourceCodeOpenButtonClick() {
    var view = mock(ToolkitView.class);
    var file = new File("src/test/resources/parse_error.txt");
    when(view.pickFileToParse()).thenReturn(file);
    var model = mock(SourceCodeModel.class);
    var astNode = mock(AstNode.class);
    when(model.getHighlightedSourceCode()).thenReturn("my_mocked_highlighted_source_code");
    when(model.getAstNode()).thenReturn(astNode);
    when(model.getXml()).thenReturn("my_mocked_xml");

    var presenter = new ToolkitPresenter((ConfigurationModel) when(mock(ConfigurationModel.class)
      .getCharset()).thenReturn(StandardCharsets.UTF_8).getMock(), model);
    presenter.setView(view);

    presenter.onSourceCodeOpenButtonClick();

    verify(view).pickFileToParse();

    verify(view).clearConsole();
    verify(view).displayHighlightedSourceCode("my_mocked_highlighted_source_code");
    verify(model).setSourceCode(file, StandardCharsets.UTF_8);
    verify(view).displayAst(astNode);
    verify(view).displayXml("my_mocked_xml");
    verify(view).scrollSourceCodeTo(new Point(0, 0));
    verify(view).setFocusOnAbstractSyntaxTreeView();
    verify(view).enableXPathEvaluateButton();
  }

  @Test
  void onSourceCodeOpenButtonClickWithParseErrorShouldClearConsoleAndDisplayCode() {
    var view = mock(ToolkitView.class);
    var file = new File("src/test/resources/parse_error.txt");
    when(view.pickFileToParse()).thenReturn(file);
    var model = mock(SourceCodeModel.class);
    Mockito.doThrow(new RuntimeException("Parse error")).when(model).setSourceCode(Mockito.any(File.class), Mockito.any(
      Charset.class));

    var presenter = new ToolkitPresenter((ConfigurationModel) when(mock(ConfigurationModel.class)
      .getCharset()).thenReturn(StandardCharsets.UTF_8).getMock(), model);
    presenter.setView(view);

    try {
      presenter.onSourceCodeOpenButtonClick();
      throw new AssertionError("Expected an exception");
    } catch (RuntimeException e) {
      verify(view).clearConsole();
      verify(view).displayHighlightedSourceCode("parse_error.txt");
    }
  }

  @Test
  void onSourceCodeOpenButtonClickShouldNoOperationWhenNoFile() {
    var view = mock(ToolkitView.class);
    when(view.pickFileToParse()).thenReturn(null);

    var model = mock(SourceCodeModel.class);

    var presenter = new ToolkitPresenter(mock(ConfigurationModel.class), model);
    presenter.setView(view);

    presenter.onSourceCodeOpenButtonClick();

    verify(view).pickFileToParse();

    verify(view, never()).clearConsole();
    verify(model, never()).setSourceCode(any(File.class), any(Charset.class));
    verify(view, never()).displayHighlightedSourceCode(anyString());
    verify(view, never()).displayAst(any(AstNode.class));
    verify(view, never()).displayXml(anyString());
    verify(view, never()).scrollSourceCodeTo(any(Point.class));
    verify(view, never()).enableXPathEvaluateButton();
  }

  @Test
  void onSourceCodeParseButtonClick() {
    var view = mock(ToolkitView.class);
    when(view.getSourceCode()).thenReturn("my_mocked_source");
    var point = mock(Point.class);
    when(view.getSourceCodeScrollbarPosition()).thenReturn(point);
    var model = mock(SourceCodeModel.class);
    when(model.getHighlightedSourceCode()).thenReturn("my_mocked_highlighted_source_code");
    var astNode = mock(AstNode.class);
    when(model.getAstNode()).thenReturn(astNode);
    when(model.getXml()).thenReturn("my_mocked_xml");

    var presenter = new ToolkitPresenter(mock(ConfigurationModel.class), model);
    presenter.setView(view);

    presenter.onSourceCodeParseButtonClick();

    verify(view).clearConsole();
    verify(view).getSourceCode();
    verify(model).setSourceCode("my_mocked_source");
    verify(view).displayHighlightedSourceCode("my_mocked_highlighted_source_code");
    view.displayAst(astNode);
    view.displayXml("my_mocked_xml");
    view.scrollSourceCodeTo(point);
    verify(view).setFocusOnAbstractSyntaxTreeView();
    verify(view).enableXPathEvaluateButton();
  }

  @Test
  void onXPathEvaluateButtonClickAstNodeResults() {
    var view = mock(ToolkitView.class);
    when(view.getXPath()).thenReturn("//foo");
    var model = mock(SourceCodeModel.class);
    var astNode = new AstNode(GenericTokenType.IDENTIFIER, "foo", null);
    when(model.getAstNode()).thenReturn(astNode);

    var presenter = new ToolkitPresenter(mock(ConfigurationModel.class), model);
    presenter.setView(view);

    presenter.onXPathEvaluateButtonClick();

    verify(view).clearAstSelections();
    verify(view).clearSourceCodeHighlights();

    verify(view).selectAstNode(astNode);
    verify(view).highlightSourceCode(astNode);

    verify(view).scrollAstTo(astNode);
  }

  @Test
  void onXPathEvaluateButtonClickScrollToFirstAstNode() {
    var view = mock(ToolkitView.class);
    when(view.getXPath()).thenReturn("//foo");
    var model = mock(SourceCodeModel.class);
    var astNode = new AstNode(GenericTokenType.IDENTIFIER, "foo", null);
    var childAstNode = new AstNode(GenericTokenType.IDENTIFIER, "foo", null);
    astNode.addChild(childAstNode);
    when(model.getAstNode()).thenReturn(astNode);

    var presenter = new ToolkitPresenter(mock(ConfigurationModel.class), model);
    presenter.setView(view);

    presenter.onXPathEvaluateButtonClick();

    verify(view).scrollAstTo(astNode);
    verify(view, never()).scrollAstTo(childAstNode);

    verify(view).scrollSourceCodeTo(astNode);
    verify(view, never()).scrollSourceCodeTo(childAstNode);
  }

  @Test
  void onXPathEvaluateButtonClickStringResult() throws Exception {
    var view = mock(ToolkitView.class);
    when(view.getXPath()).thenReturn("//foo/@tokenValue");
    var model = mock(SourceCodeModel.class);
    var token = Token.builder()
      .setType(GenericTokenType.IDENTIFIER)
      .setValueAndOriginalValue("bar")
      .setURI(new URI("tests://unittest"))
      .setLine(1)
      .setColumn(1)
      .build();
    var astNode = new AstNode(GenericTokenType.IDENTIFIER, "foo", token);
    when(model.getAstNode()).thenReturn(astNode);

    var presenter = new ToolkitPresenter(mock(ConfigurationModel.class), model);
    presenter.setView(view);

    presenter.onXPathEvaluateButtonClick();

    verify(view).clearConsole();
    verify(view).clearAstSelections();
    verify(view).clearSourceCodeHighlights();

    verify(view, never()).selectAstNode(any(AstNode.class));
    verify(view, never()).highlightSourceCode(any(AstNode.class));

    verify(view).scrollAstTo(null);
    verify(view).scrollSourceCodeTo((AstNode) null);

    verify(view).setFocusOnAbstractSyntaxTreeView();
  }

  @Test
  void onSourceCodeKeyTyped() {
    var view = mock(ToolkitView.class);

    var presenter = new ToolkitPresenter(mock(ConfigurationModel.class), mock(SourceCodeModel.class));
    presenter.setView(view);

    presenter.onSourceCodeKeyTyped();

    verify(view).displayAst(null);
    verify(view).displayXml("");
    verify(view).clearSourceCodeHighlights();
    verify(view).disableXPathEvaluateButton();
  }

  @Test
  void onSourceCodeTextCursorMoved() {
    var view = mock(ToolkitView.class);
    var astNode = mock(AstNode.class);
    when(view.getAstNodeFollowingCurrentSourceCodeTextCursorPosition()).thenReturn(astNode);

    var presenter = new ToolkitPresenter(mock(ConfigurationModel.class), mock(SourceCodeModel.class));
    presenter.setView(view);

    presenter.onSourceCodeTextCursorMoved();

    verify(view).clearAstSelections();
    verify(view).selectAstNode(astNode);
    verify(view).scrollAstTo(astNode);
  }

  @Test
  void onAstSelectionChanged() {
    var view = mock(ToolkitView.class);
    var firstAstNode = mock(AstNode.class);
    var secondAstNode = mock(AstNode.class);
    when(view.getSelectedAstNodes()).thenReturn(Arrays.asList(firstAstNode, secondAstNode));

    var presenter = new ToolkitPresenter(mock(ConfigurationModel.class), mock(SourceCodeModel.class));
    presenter.setView(view);

    presenter.onAstSelectionChanged();

    verify(view).clearSourceCodeHighlights();

    verify(view).highlightSourceCode(firstAstNode);
    verify(view).highlightSourceCode(secondAstNode);

    verify(view).scrollSourceCodeTo(firstAstNode);

    verify(view, never()).scrollSourceCodeTo(secondAstNode);
  }

  @Test
  void onConfigurationPropertyFocusLostWhenValidationSuccesses() {
    var view = mock(ToolkitView.class);

    var property = mock(ConfigurationProperty.class);
    when(property.getName()).thenReturn("name");
    when(property.getDescription()).thenReturn("description");
    when(view.getConfigurationPropertyValue("name")).thenReturn("foo");
    when(property.validate("foo")).thenReturn("");

    var configurationModel = mock(ConfigurationModel.class);
    when(configurationModel.getProperties()).thenReturn(Collections.singletonList(property));
    var presenter = new ToolkitPresenter(configurationModel, mock(SourceCodeModel.class));
    presenter.setView(view);

    presenter.onConfigurationPropertyFocusLost("name");

    verify(view).setConfigurationPropertyErrorMessage("name", "");
    verify(view, never()).setFocusOnConfigurationPropertyField(Mockito.anyString());
    verify(view, never()).setFocusOnConfigurationView();
    verify(property).setValue("foo");
    verify(configurationModel).setUpdatedFlag();
  }

  @Test
  void onConfigurationPropertyFocusLostWhenValidationFails() {
    var view = mock(ToolkitView.class);

    var property = mock(ConfigurationProperty.class);
    when(property.getName()).thenReturn("name");
    when(property.getDescription()).thenReturn("description");
    when(view.getConfigurationPropertyValue("name")).thenReturn("foo");
    when(property.validate("foo")).thenReturn("The value foo is forbidden!");

    var configurationModel = mock(ConfigurationModel.class);
    when(configurationModel.getProperties()).thenReturn(Collections.singletonList(property));
    var presenter = new ToolkitPresenter(configurationModel, mock(SourceCodeModel.class));
    presenter.setView(view);

    presenter.onConfigurationPropertyFocusLost("name");

    verify(view).setConfigurationPropertyErrorMessage("name", "The value foo is forbidden!");
    verify(view).setFocusOnConfigurationPropertyField("name");
    verify(view).setFocusOnConfigurationView();
    verify(property, never()).setValue("foo");
    verify(configurationModel, never()).setUpdatedFlag();
  }

  @Test
  void onConfigurationPropertyFocusLostWithInvalidName() {
    var view = mock(ToolkitView.class);
    var presenter = new ToolkitPresenter(mock(ConfigurationModel.class), mock(SourceCodeModel.class));

    var thrown = catchThrowableOfType(IllegalArgumentException.class, () -> {
      presenter.setView(view);
      presenter.onConfigurationPropertyFocusLost("name");
    });
    assertThat(thrown)
      .isExactlyInstanceOf(IllegalArgumentException.class)
      .hasMessage("No such configuration property: name");
  }

}
