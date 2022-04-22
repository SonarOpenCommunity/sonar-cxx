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
package org.sonar.cxx.sslr.toolkit; // cxx: in use

import com.sonar.cxx.sslr.impl.Parser;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.sonar.colorizer.Tokenizer;
import org.sonar.cxx.sslr.internal.toolkit.SourceCodeModel;
import org.sonar.cxx.sslr.internal.toolkit.ToolkitPresenter;
import org.sonar.cxx.sslr.internal.toolkit.ToolkitViewImpl;

public class Toolkit {

  private final String title;
  private final ConfigurationModel configurationModel;

  /**
   * Create a Toolkit with a title, a static parser and static tokenizers.
   *
   * @param parser
   * @param tokenizers
   * @param title
   *
   * @deprecated in 1.17, use {@link #Toolkit(String, ConfigurationModel)} instead.
   */
  @Deprecated(since = "1.17")
  public Toolkit(final Parser parser, final List<Tokenizer> tokenizers, String title) {
    this(title, new AbstractConfigurationModel() {

         @Override
         public List<ConfigurationProperty> getProperties() {
           return Collections.emptyList();
         }

         @Override
         public List<Tokenizer> doGetTokenizers() {
           return tokenizers;
         }

         @Override
         public Parser doGetParser() {
           return parser;
         }

       });
  }

  /**
   * Creates a Toolkit with a title, and the given {@link ConfigurationModel}.
   *
   * @param title
   * @param configurationModel
   *
   * @since 1.17
   */
  public Toolkit(String title, ConfigurationModel configurationModel) {
    Objects.requireNonNull(title);

    this.title = title;
    this.configurationModel = configurationModel;
  }

  public void run() {
    SwingUtilities.invokeLater(() -> {
      try {
        for (var info : UIManager.getInstalledLookAndFeels()) {
          if ("Nimbus".equals(info.getName())) {
            UIManager.setLookAndFeel(info.getClassName());
            break;
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      var model = new SourceCodeModel(configurationModel);
      var presenter = new ToolkitPresenter(configurationModel, model);
      presenter.setView(new ToolkitViewImpl(presenter));
      presenter.run(title);
    });
  }

}
