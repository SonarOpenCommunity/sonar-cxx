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
package org.sonar.cxx.sslr.toolkit;

import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.impl.Parser;
import java.util.List;
import javax.annotation.CheckForNull;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.sonar.colorizer.Tokenizer;

class AbstractConfigurationModelTest {

  @Test
  void getParserShouldReturnParserInstance() {
    var model = new MyConfigurationModel();
    Parser<? extends Grammar> p = mock(Parser.class);
    model.setParser(p);
    assertThat(model.getParser()).isEqualTo(p);
  }

  @Test
  void getParserShouldReturnSameParserInstanceWhenFlagNotSet() {
    var model = new MyConfigurationModel();
    Parser<? extends Grammar> p = mock(Parser.class);
    model.setParser(p);
    assertThat(model.getParser()).isEqualTo(p);
    Parser<? extends Grammar> p2 = mock(Parser.class);
    model.setParser(p2);
    assertThat(model.getParser()).isEqualTo(p);
  }

  @Test
  void getParserShouldReturnDifferentParserInstanceWhenFlagSet() {
    var model = new MyConfigurationModel();
    Parser<? extends Grammar> p = mock(Parser.class);
    model.setParser(p);
    assertThat(model.getParser()).isEqualTo(p);
    Parser<? extends Grammar> p2 = mock(Parser.class);
    model.setParser(p2);

    model.setUpdatedFlag();

    assertThat(model.getParser()).isEqualTo(p2);
  }

  @Test
  void getTokenizersShouldReturnParserInstance() {
    var model = new MyConfigurationModel();
    List<Tokenizer> t = mock(List.class);
    model.setTokenizers(t);
    assertThat(model.getTokenizers()).isEqualTo(t);
  }

  @Test
  void getTokenizersShouldReturnSameParserInstanceWhenFlagNotSet() {
    var model = new MyConfigurationModel();
    List<Tokenizer> t = mock(List.class);
    model.setTokenizers(t);
    assertThat(model.getTokenizers()).isEqualTo(t);
    List<Tokenizer> t2 = mock(List.class);
    model.setTokenizers(t2);
    assertThat(model.getTokenizers()).isEqualTo(t);
  }

  @Test
  void getTokenizersShouldReturnDifferentParserInstanceWhenFlagSet() {
    var model = new MyConfigurationModel();
    List<Tokenizer> t = mock(List.class);
    model.setTokenizers(t);
    assertThat(model.getTokenizers()).isEqualTo(t);
    List<Tokenizer> t2 = mock(List.class);
    model.setTokenizers(t2);

    model.setUpdatedFlag();

    assertThat(model.getTokenizers()).isEqualTo(t2);
  }

  private static class MyConfigurationModel extends AbstractConfigurationModel {

    private Parser<? extends Grammar> parser;
    private List<Tokenizer> tokenizers;

    @Override
    @CheckForNull
    public List<ConfigurationProperty> getProperties() {
      return null;
    }

    public void setParser(Parser<? extends Grammar> parser) {
      this.parser = parser;
    }

    @Override
    public Parser<? extends Grammar> doGetParser() {
      return parser;
    }

    public void setTokenizers(List<Tokenizer> tokenizers) {
      this.tokenizers = tokenizers;
    }

    @Override
    public List<Tokenizer> doGetTokenizers() {
      return tokenizers;
    }

  }

}
