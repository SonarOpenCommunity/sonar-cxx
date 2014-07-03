/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx;

import org.sonar.api.web.CodeColorizerFormat;
import org.sonar.colorizer.CDocTokenizer;
import org.sonar.colorizer.CppDocTokenizer;
import org.sonar.colorizer.KeywordsTokenizer;
import org.sonar.colorizer.Tokenizer;

import java.util.ArrayList;
import java.util.List;
import org.sonar.colorizer.LiteralTokenizer;
import org.sonar.colorizer.RegexpTokenizer;
import org.sonar.cxx.api.CxxKeyword;

/**
 * {@inheritDoc}
 */
public final class CxxColorizer extends CodeColorizerFormat {

  private static final String SPAN = "</span>";

  /**
   * {@inheritDoc}
   */
  public CxxColorizer() {
    super(CxxLanguage.KEY);
  }

  @Override
  public List<Tokenizer> getTokenizers() {
    List<Tokenizer> tokenizers = new ArrayList<Tokenizer>();
    tokenizers.add(new CDocTokenizer("<span class=\"p\">", SPAN)); // C style comments
    tokenizers.add(new CppDocTokenizer("<span class=\"p\">", SPAN)); // C++ style comments
    tokenizers.add(new KeywordsTokenizer("<span class=\"k\">", SPAN, CxxKeyword.keywordValues())); // keywords
    tokenizers.add(new LiteralTokenizer("<span class=\"s\">", SPAN)); // strings, characters
    tokenizers.add(new RegexpTokenizer("<span class=\"h\">", SPAN, "#[^\\n\\r]*+")); // preprocessor directives
    tokenizers.add(new RegexpTokenizer("<span class=\"c\">", SPAN, "[+-]?[0-9]+[xX]?+(\\.[0-9]*+)?")); // numbers
    return tokenizers;
  }
}
