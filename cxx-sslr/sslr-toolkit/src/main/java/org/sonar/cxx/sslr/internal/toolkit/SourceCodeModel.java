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
import com.sonar.cxx.sslr.impl.ast.AstXmlPrinter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.sonar.colorizer.HtmlOptions;
import org.sonar.colorizer.HtmlRenderer;
import org.sonar.cxx.sslr.toolkit.ConfigurationModel;

public class SourceCodeModel {

  private final ConfigurationModel configurationModel;
  private final HtmlRenderer htmlRenderer = new HtmlRenderer(new HtmlOptions(false, null, false));

  private String sourceCode;
  private AstNode astNode;

  public SourceCodeModel(@Nonnull ConfigurationModel configurationModel) {
    Objects.requireNonNull(configurationModel);

    this.configurationModel = configurationModel;
  }

  public void setSourceCode(File source, Charset charset) {
    try {
      this.sourceCode = new String(Files.readAllBytes(Paths.get(source.getPath())), charset);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.astNode = configurationModel.getParser().parse(source);
  }

  public void setSourceCode(String sourceCode) {
    this.sourceCode = sourceCode;
    this.astNode = configurationModel.getParser().parse(sourceCode);
  }

  public String getHighlightedSourceCode() {
    return htmlRenderer.render(new StringReader(sourceCode), configurationModel.getTokenizers());
  }

  public String getXml() {
    return AstXmlPrinter.print(astNode);
  }

  public AstNode getAstNode() {
    return astNode;
  }

}
