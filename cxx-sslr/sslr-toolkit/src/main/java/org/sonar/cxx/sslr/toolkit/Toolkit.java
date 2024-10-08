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
package org.sonar.cxx.sslr.toolkit; // cxx: in use

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.sonar.cxx.sslr.internal.toolkit.SourceCodeModel;
import org.sonar.cxx.sslr.internal.toolkit.ToolkitPresenter;
import org.sonar.cxx.sslr.internal.toolkit.ToolkitViewImpl;

public class Toolkit {

  private final String title;
  private final ConfigurationModel configurationModel;

  /**
   * Creates a Toolkit with a title, and the given {@link ConfigurationModel}.
   *
   * @param title
   * @param configurationModel
   *
   * @since 1.17
   */
  public Toolkit(@Nonnull String title, ConfigurationModel configurationModel) {
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
