/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022 SonarOpenCommunity
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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class ToolkitViewImpl extends JFrame implements ToolkitView {

  private static final long serialVersionUID = 1L;
  private static final TreeModel EMPTY_TREE_MODEL = new DefaultTreeModel(null);

  public final transient ToolkitPresenter presenter;

  private final JTabbedPane tabbedPane = new JTabbedPane();

  private final JTree astTree = new JTree();
  private final JScrollPane astTreeScrollPane = new JScrollPane(astTree);

  private final JTextArea xmlTextArea = new JTextArea();
  private final JScrollPane xmlScrollPane = new JScrollPane(xmlTextArea);

  private final JTextArea consoleTextArea = new JTextArea();
  private final JScrollPane consoleScrollPane = new JScrollPane(consoleTextArea);

  private final JPanel configurationInnerPanel = new JPanel(new GridBagLayout());
  private final JPanel configurationOuterPanel = new JPanel(new BorderLayout());
  private final JScrollPane configurationScrollPane = new JScrollPane(configurationOuterPanel);
  private final Map<String, ConfigurationPropertyPanel> configurationPropertiesPanels = new HashMap<>();

  private final JLabel sourceCodeLabel = new JLabel(" Source Code");
  private final JEditorPane sourceCodeEditorPane = new JEditorPane();
  private final JScrollPane sourceCodeEditorScrollPane = new JScrollPane(sourceCodeEditorPane);
  private final JButton sourceCodeOpenButton = new JButton();
  private final JButton sourceCodeParseButton = new JButton();
  private final JPanel sourceCodeButtonsPanel = new JPanel();
  private final JPanel sourceCodePanel = new JPanel(new BorderLayout(0, 2));

  private final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sourceCodePanel, tabbedPane);

  private final JPanel southPanel = new JPanel(new BorderLayout(0, 2));

  private final JLabel xpathLabel = new JLabel("  XPath query");
  private final JTextArea xpathTextArea = new JTextArea();
  private final JScrollPane xpathTextAreaScrollPane = new JScrollPane(xpathTextArea);
  private final JPanel xpathPanel = new JPanel(new BorderLayout(10, 2));

  private final JFileChooser fileChooser = new JFileChooser();
  private final JButton xpathButton = new JButton();
  private final JPanel xpathButtonPanel = new JPanel();

  private transient LineOffsets lineOffsets = null;
  private final transient DefaultHighlighter.DefaultHighlightPainter highlighter
                                                                       = new DefaultHighlighter.DefaultHighlightPainter(
      Color.LIGHT_GRAY);

  private boolean sourceCodeTextCursorMovedEventDisabled = false;
  private boolean astSelectionEventDisabled = false;

  public ToolkitViewImpl(@Nonnull ToolkitPresenter presenter) {
    Objects.requireNonNull(presenter);
    this.presenter = presenter;

    initComponents();
  }

  private void initComponents() {
    setSize(1000, 700);
    setDefaultCloseOperation(ToolkitViewImpl.EXIT_ON_CLOSE);

    setLayout(new BorderLayout(0, 5));

    astTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    astTree.addTreeSelectionListener((TreeSelectionEvent event) -> {
      if (!astSelectionEventDisabled) {
        presenter.onAstSelectionChanged();
      }
    });

    consoleTextArea.setEditable(false);
    consoleTextArea.setFont(Font.decode("Monospaced"));

    tabbedPane.setTabPlacement(JTabbedPane.TOP);
    tabbedPane.add("Abstract Syntax Tree", astTreeScrollPane);
    tabbedPane.add("XML", xmlScrollPane);
    tabbedPane.add("Console", consoleScrollPane);
    tabbedPane.add("Configuration", configurationScrollPane);

    configurationOuterPanel.add(configurationInnerPanel, BorderLayout.NORTH);
    configurationOuterPanel.add(Box.createGlue(), BorderLayout.CENTER);

    sourceCodeEditorPane.setContentType("text/html");
    sourceCodeEditorPane.setEditable(true);
    ((DefaultCaret) sourceCodeEditorPane.getCaret()).setUpdatePolicy(DefaultCaret.UPDATE_WHEN_ON_EDT);
    sourceCodeEditorPane.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void removeUpdate(DocumentEvent e) {
        presenter.onSourceCodeKeyTyped();
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        presenter.onSourceCodeKeyTyped();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        presenter.onSourceCodeKeyTyped();
      }
    });
    sourceCodeEditorPane.addCaretListener((CaretEvent e) -> {
      if (!sourceCodeTextCursorMovedEventDisabled) {
        presenter.onSourceCodeTextCursorMoved();
      }
    });

    sourceCodeOpenButton.setText("Open Source File");
    sourceCodeOpenButton.addActionListener((ActionEvent e) -> {
      presenter.onSourceCodeOpenButtonClick();
    });

    sourceCodeParseButton.setText("Parse Source Code");
    sourceCodeParseButton.addActionListener((ActionEvent e) -> {
      presenter.onSourceCodeParseButtonClick();
    });

    sourceCodeButtonsPanel.add(sourceCodeOpenButton);
    sourceCodeButtonsPanel.add(sourceCodeParseButton);

    sourceCodePanel.add(sourceCodeLabel, BorderLayout.NORTH);
    sourceCodePanel.add(sourceCodeEditorScrollPane, BorderLayout.CENTER);
    sourceCodePanel.add(sourceCodeButtonsPanel, BorderLayout.SOUTH);

    splitPane.setDividerLocation(getWidth() / 2);
    add(splitPane, BorderLayout.CENTER);

    xpathPanel.add(xpathLabel, BorderLayout.NORTH);
    xpathPanel.add(Box.createHorizontalGlue(), BorderLayout.WEST);
    xpathTextArea.setText("//IDENTIFIER");
    xpathTextArea.setRows(8);
    xpathPanel.add(xpathTextAreaScrollPane, BorderLayout.CENTER);
    xpathPanel.add(Box.createHorizontalGlue(), BorderLayout.EAST);

    southPanel.add(xpathPanel, BorderLayout.NORTH);

    xpathButton.setText("Evaluate XPath");
    xpathButton.addActionListener((ActionEvent e) -> {
      presenter.onXPathEvaluateButtonClick();
    });
    xpathButtonPanel.add(xpathButton);

    southPanel.add(xpathButtonPanel, BorderLayout.SOUTH);

    add(southPanel, BorderLayout.SOUTH);
  }

  @Override
  public void run() {
    setVisible(true);
  }

  @Override
  @CheckForNull
  public File pickFileToParse() {
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      return fileChooser.getSelectedFile();
    } else {
      return null;
    }
  }

  @Override
  public void displayHighlightedSourceCode(String htmlHighlightedSourceCode) {
    try {
      sourceCodeTextCursorMovedEventDisabled = true;

      Objects.requireNonNull(htmlHighlightedSourceCode);

      var sb = new StringBuilder();
      sb.append("<html><head><style type=\"text/css\">");
      sb.append(CssLoader.getCss());
      sb.append("</style></head><body><pre class=\"code\" id=\"code\">");
      sb.append(htmlHighlightedSourceCode);
      sb.append("</pre></body></html>");

      sourceCodeEditorPane.setText(sb.toString());
      lineOffsets = new LineOffsets(getSourceCode());
    } finally {
      sourceCodeTextCursorMovedEventDisabled = false;
    }
  }

  @Override
  public void displayAst(@Nullable AstNode astNode) {
    if (astNode == null) {
      astTree.setModel(EMPTY_TREE_MODEL);
    } else {
      TreeNode treeNode = getTreeNode(astNode);
      astTree.setModel(new DefaultTreeModel(treeNode));
    }
  }

  private static DefaultMutableTreeNode getTreeNode(AstNode astNode) {
    var treeNode = new DefaultMutableTreeNode(astNode);

    if (astNode.hasChildren()) {
      for (var childAstNode : astNode.getChildren()) {
        treeNode.add(getTreeNode(childAstNode));
      }
    } else if (astNode.hasToken() && astNode.getToken().hasTrivia()) {
      for (var trivia : astNode.getToken().getTrivia()) {
        var childTreeNode = new DefaultMutableTreeNode(trivia);

        if (trivia.hasPreprocessingDirective()) {
          childTreeNode.add(getTreeNode(trivia.getPreprocessingDirective().getAst()));
        }

        treeNode.add(childTreeNode);
      }
    }

    return treeNode;
  }

  @Override
  public void displayXml(@Nonnull String xml) {
    Objects.requireNonNull(xml);

    xmlTextArea.setText(xml);
  }

  @Override
  public Point getSourceCodeScrollbarPosition() {
    int x = sourceCodeEditorScrollPane.getHorizontalScrollBar().getValue();
    int y = sourceCodeEditorScrollPane.getVerticalScrollBar().getValue();

    return new Point(x, y);
  }

  @Override
  public void scrollSourceCodeTo(final Point point) {
    Objects.requireNonNull(point);

    // http://stackoverflow.com/questions/8789371/java-jtextpane-jscrollpane-de-activate-automatic-scrolling
    SwingUtilities.invokeLater(() -> {
      sourceCodeEditorScrollPane.getHorizontalScrollBar().setValue(point.x);
      sourceCodeEditorScrollPane.getVerticalScrollBar().setValue(point.y);
    });
  }

  @Override
  public String getSourceCode() {
    int startOffset = getCodeElementStartOffset();
    int endOffset = getCodeElementEndOffset();

    try {
      return sourceCodeEditorPane.getText(startOffset, endOffset - startOffset - 1);
    } catch (BadLocationException e) {
      throw new RuntimeException(e);
    }
  }

  private int getCodeElementStartOffset() {
    var htmlDocument = (HTMLDocument) sourceCodeEditorPane.getDocument();
    var codeElement = htmlDocument.getElement("code");
    return codeElement.getStartOffset();
  }

  private int getCodeElementEndOffset() {
    var htmlDocument = (HTMLDocument) sourceCodeEditorPane.getDocument();
    var codeElement = htmlDocument.getElement("code");
    return codeElement.getEndOffset();
  }

  @Override
  public String getXPath() {
    return xpathTextArea.getText();
  }

  @Override
  public void selectAstNode(AstNode astNode) {
    if (astNode != null) {
      try {
        astSelectionEventDisabled = true;
        var treeNode = getAstTreeNodeWithGivenUserObject((DefaultMutableTreeNode) astTree.getModel().getRoot(), astNode);
        if (treeNode != null) {
          astTree.getSelectionModel().addSelectionPath(new TreePath(treeNode.getPath()));
        }
      } finally {
        astSelectionEventDisabled = false;
      }
    }
  }

  @CheckForNull
  private DefaultMutableTreeNode getAstTreeNodeWithGivenUserObject(DefaultMutableTreeNode treeNode, Object userObject) {
    if (treeNode.getUserObject().equals(userObject)) {
      return treeNode;
    } else {
      for (int i = 0; i < treeNode.getChildCount(); i++) {
        var treeNodeWithUserObject = getAstTreeNodeWithGivenUserObject((DefaultMutableTreeNode) treeNode.getChildAt(i),
                                                                   userObject);
        if (treeNodeWithUserObject != null) {
          return treeNodeWithUserObject;
        }
      }

      return null;
    }
  }

  @Override
  public void highlightSourceCode(@Nonnull AstNode astNode) {
    Objects.requireNonNull(astNode);

    if (!astNode.hasToken()) {
      return;
    }

    var startToken = astNode.getToken();
    var endToken = astNode.getLastToken();

    int startOffset = getValidDocumentOffsetFromSourceCodeOffset(lineOffsets.getStartOffset(startToken));
    int endOffset = getValidDocumentOffsetFromSourceCodeOffset(lineOffsets.getEndOffset(endToken));

    try {
      sourceCodeEditorPane.getHighlighter().addHighlight(startOffset, endOffset, highlighter);
    } catch (BadLocationException e) {
      throw new RuntimeException(e);
    }
  }

  private int getValidDocumentOffsetFromSourceCodeOffset(int offset) {
    int result = Math.max(offset, 0);
    result += getCodeElementStartOffset();
    result = Math.min(result, getCodeElementEndOffset());

    return result;
  }

  @Override
  public void clearAstSelections() {
    try {
      astSelectionEventDisabled = true;
      astTree.getSelectionModel().clearSelection();
    } finally {
      astSelectionEventDisabled = false;
    }
  }

  @Override
  public void scrollAstTo(@Nullable AstNode astNode) {
    if (astNode != null) {
      var treeNode = getAstTreeNodeWithGivenUserObject((DefaultMutableTreeNode) astTree.getModel().getRoot(), astNode);
      if (treeNode != null) {
        astTree.scrollPathToVisible(new TreePath(treeNode.getPath()));
      }
    }
  }

  @Override
  public void clearSourceCodeHighlights() {
    sourceCodeEditorPane.getHighlighter().removeAllHighlights();
  }

  @Override
  public void scrollSourceCodeTo(@Nullable AstNode astNode) {
    if (astNode != null && astNode.hasToken()) {
      int visibleLines = sourceCodeEditorPane.getVisibleRect().height / sourceCodeEditorPane.getFontMetrics(
        sourceCodeEditorPane.getFont()).getHeight();
      int line = astNode.getToken().getLine() + visibleLines / 2;

      try {
        var rect = sourceCodeEditorPane.modelToView2D(0);
        sourceCodeEditorPane.scrollRectToVisible(new Rectangle((int) rect.getX(), (int) rect.getY(),
                                                               (int) rect.getWidth(), (int) rect.getHeight()));

        rect = sourceCodeEditorPane.modelToView2D(lineOffsets.getOffset(line, 0));
        sourceCodeEditorPane.scrollRectToVisible(new Rectangle((int) rect.getX(), (int) rect.getY(),
                                                               (int) rect.getWidth(), (int) rect.getHeight()));
      } catch (BadLocationException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void disableXPathEvaluateButton() {
    xpathButton.setEnabled(false);
  }

  @Override
  public void enableXPathEvaluateButton() {
    xpathButton.setEnabled(true);
  }

  @Override
  @Nullable
  public AstNode getAstNodeFollowingCurrentSourceCodeTextCursorPosition() {
    int currentOffset = sourceCodeEditorPane.getCaretPosition() - getCodeElementStartOffset();

    return getFollowingAstNode((DefaultMutableTreeNode) astTree.getModel().getRoot(), currentOffset);
  }

  private AstNode getFollowingAstNode(DefaultMutableTreeNode treeNode, int offset) {
    AstNode followingAstNode = null;

    if (treeNode != null) {
      Enumeration enumeration = ((DefaultMutableTreeNode) astTree.getModel().getRoot()).breadthFirstEnumeration();

      int nearestOffsetSoFar = Integer.MAX_VALUE;
      while (enumeration.hasMoreElements()) {
        var childTreeNode = (DefaultMutableTreeNode) enumeration.nextElement();
        if (childTreeNode.getUserObject() instanceof AstNode) {
          var astNode = (AstNode) childTreeNode.getUserObject();

          if (astNode.hasToken()) {
            var token = astNode.getToken();
            int tokenOffset = lineOffsets.getStartOffset(token);

            if (tokenOffset >= offset && tokenOffset < nearestOffsetSoFar) {
              nearestOffsetSoFar = tokenOffset;
              followingAstNode = astNode;
            }
          }
        }
      }
    }

    return followingAstNode;
  }

  @Override
  public List<AstNode> getSelectedAstNodes() {
    List<AstNode> acc = new ArrayList<>();

    var selectedPaths = astTree.getSelectionPaths();
    if (selectedPaths != null) {
      for (var selectedPath : selectedPaths) {
        var treeNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();

        var userObject = treeNode.getUserObject();
        if (userObject instanceof AstNode) {
          var astNode = (AstNode) userObject;
          acc.add(astNode);
        }
      }
    }

    return acc;
  }

  @Override
  public void appendToConsole(String message) {
    consoleTextArea.append(message);
  }

  @Override
  public void setFocusOnConsoleView() {
    tabbedPane.setSelectedComponent(consoleScrollPane);
  }

  @Override
  public void setFocusOnAbstractSyntaxTreeView() {
    tabbedPane.setSelectedComponent(astTreeScrollPane);
  }

  @Override
  public void clearConsole() {
    consoleTextArea.setText("");
  }

  @Override
  public void addConfigurationProperty(final String name, String description) {
    var configurationPropertyPanel = new ConfigurationPropertyPanel(name, description);

    configurationPropertyPanel.getValueTextField().addFocusListener(new FocusAdapter() {

      @Override
      public void focusLost(FocusEvent e) {
        presenter.onConfigurationPropertyFocusLost(name);
      }

    });

    configurationPropertiesPanels.put(name, configurationPropertyPanel);

    var constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weightx = 1;
    constraints.gridx = 0;
    constraints.anchor = GridBagConstraints.NORTH;

    configurationInnerPanel.add(configurationPropertyPanel.getPanel(), constraints);
  }

  @Override
  public String getConfigurationPropertyValue(String name) {
    return configurationPropertiesPanels.get(name).getValueTextField().getText();
  }

  @Override
  public void setConfigurationPropertyValue(String name, String value) {
    configurationPropertiesPanels.get(name).getValueTextField().setText(value);
  }

  @Override
  public void setConfigurationPropertyErrorMessage(String name, String errorMessage) {
    configurationPropertiesPanels.get(name).getErrorMessageLabel().setText(errorMessage);
  }

  @Override
  public void setFocusOnConfigurationPropertyField(String name) {
    configurationPropertiesPanels.get(name).getValueTextField().requestFocus();
  }

  @Override
  public void setFocusOnConfigurationView() {
    tabbedPane.setSelectedComponent(configurationScrollPane);
  }

}
