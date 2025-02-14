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

import javax.annotation.Nullable;

import java.awt.Point;
import java.io.File;
import java.util.List;

/**
 * Contract interface for the view.
 *
 * Note that *none* of the methods here-under should generate *any* event back to the presenter.
 * Only end-user interactions are supposed to generate events.
 */
public interface ToolkitView {

  /**
   * Launch the application.
   */
  void run();

  /**
   * Set the title of the application.
   *
   * @param title
   */
  void setTitle(String title);

  /**
   * Prompt the user for a file to parse and return it.

   * @return The file to parse, or null if no file was picked
   */
  @Nullable
  File pickFileToParse();

  /**
   * Display the given HTML highlighted source code in the source code editor.
   * Scrollbars state is undefined after a call to this method.
   *
   * @param htmlHighlightedSourceCode The HTML highlighted source code
   */
  void displayHighlightedSourceCode(String htmlHighlightedSourceCode);

  /**
   * Display the abstract syntax tree view starting from a given node.
   *
   * @param astNode The root AST node or null if no abstract syntax tree must be shown
   */
  void displayAst(@Nullable AstNode astNode);

  /**
   * Display the given string in the XML view.
   *
   * @param xml The string to display
   */
  void displayXml(String xml);

  /**
   * Get the current source code editor scrollbars' position point.
   *
   * @return The point
   */
  Point getSourceCodeScrollbarPosition();

  /**
   * Scroll the source code editor in order to make the given point visible.
   *
   * @param point to make visible
   */
  void scrollSourceCodeTo(Point point);

  /**
   * Get the source code currently entered in the source code editor.
   *
   * @return The source code
   */
  String getSourceCode();

  /**
   * Get the text currently entered in the XPath field.
   *
   * @return The XPath field text
   */
  String getXPath();

  /**
   * Select the given AST node in the abstract syntax tree view.
   *
   * @param astNode The AST node to select, null will lead to a no operation
   */
  void selectAstNode(@Nullable AstNode astNode);

  /**
   * Clear all the selections in the abstract syntax tree view.
   */
  void clearAstSelections();

  /**
   * Scroll the abstract syntax tree view in order to make the given AST node visible.
   *
   * @param astNode The AST node to make visible, null will lead to a no operation
   */
  void scrollAstTo(@Nullable AstNode astNode);

  /**
   * Highlight the given AST node in the source code editor.
   *
   * @param astNode The AST node to highlight
   */
  void highlightSourceCode(AstNode astNode);

  /**
   * Clear all the highlights in the source code editor.
   */
  void clearSourceCodeHighlights();

  /**
   * Scroll the source code editor in order to make the given AST node visible.
   *
   * @param astNode The AST node to make visible, null will lead to a no operation
   */
  void scrollSourceCodeTo(@Nullable AstNode astNode);

  /**
   * Disable the XPath evaluate button.
   */
  void disableXPathEvaluateButton();

  /**
   * Enable the XPath evaluate button.
   */
  void enableXPathEvaluateButton();

  /**
   * Get the AST node which follows the current source code editor text cursor position.
   *
   * @return The following AST node, or null if there is no such node
   */
  @Nullable
  AstNode getAstNodeFollowingCurrentSourceCodeTextCursorPosition();

  /**
   * Get the list of nodes currently selected in the abstract syntax tree view.
   *
   * @return The list of selected AST nodes
   */
  List<AstNode> getSelectedAstNodes();

  /**
   * Append the given message to the console view.
   *
   * @param message The message to append
   */
  void appendToConsole(String message);

  /**
   * Set the focus on the console view.
   */
  void setFocusOnConsoleView();

  /**
   * Set the focus on the abstract syntax tree view.
   */
  void setFocusOnAbstractSyntaxTreeView();

  /**
   * Clear the console.
   */
  void clearConsole();

  /**
   * Add a new configuration property to the configuration tab.
   *
   * @param name
   * @param description
   */
  void addConfigurationProperty(String name, String description);

  /**
   * Get the value currently entered in the configuration property field identified by the given name.
   *
   * @param name The name of the configuration property
   * @return The current value of the field
   */
  String getConfigurationPropertyValue(String name);

  /**
   * Set the current value of the configuration property field identified by the given name.
   *
   * @param name The name of the configuration property
   * @param value The value to be set
   */
  void setConfigurationPropertyValue(String name, String value);

  /**
   * Set the error message of the configuration property identified by the given name.
   *
   * @param name The name of the configuration property
   * @param errorMessage The error message
   */
  void setConfigurationPropertyErrorMessage(String name, String errorMessage);

  /**
   * Set the focus on the configuration field identified by the given name.
   *
   * @param name
   */
  void setFocusOnConfigurationPropertyField(String name);

  /**
   * Set the focus on the configuration view.
   */
  void setFocusOnConfigurationView();

}
