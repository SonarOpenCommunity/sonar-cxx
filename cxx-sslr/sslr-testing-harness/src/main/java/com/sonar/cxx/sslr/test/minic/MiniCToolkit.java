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
package com.sonar.cxx.sslr.test.minic;

import com.sonar.cxx.sslr.impl.Parser;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.List;
import org.sonar.colorizer.Tokenizer;
import org.sonar.cxx.sslr.toolkit.AbstractConfigurationModel;
import org.sonar.cxx.sslr.toolkit.ConfigurationProperty;
import org.sonar.cxx.sslr.toolkit.Toolkit;

public final class MiniCToolkit {

  private MiniCToolkit() {
  }

  public static void main(String[] args) {
    var toolkit = new Toolkit("SonarSource : MiniC : Toolkit", new MiniCConfigurationModel());
    toolkit.run();
  }

  static class MiniCConfigurationModel extends AbstractConfigurationModel {

    private final ConfigurationProperty charsetProperty = new ConfigurationProperty(
      "Charset",
      "Charset used when opening files.",
      "UTF-8", (String newValueCandidate)
      -> {
      try {
        Charset.forName(
          newValueCandidate);
        return "";
      } catch (IllegalCharsetNameException e) {
        return "Illegal charset name: "
          + newValueCandidate;
      } catch (UnsupportedCharsetException e) {
        return "Unsupported charset: "
          + newValueCandidate;
      }
    });

    @Override
    public List<ConfigurationProperty> getProperties() {
      return Collections.singletonList(charsetProperty);
    }

    @Override
    public Charset getCharset() {
      return Charset.forName(charsetProperty.getValue());
    }

    @Override
    public Parser doGetParser() {
      updateConfiguration();
      return MiniCParser.create();
    }

    @Override
    public List<Tokenizer> doGetTokenizers() {
      updateConfiguration();
      return MiniCColorizer.getTokenizers();
    }

    private static void updateConfiguration() {
      /* Construct a parser configuration object from the properties */
    }

  }

}
