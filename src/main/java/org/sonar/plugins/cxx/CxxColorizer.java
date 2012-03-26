/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.web.CodeColorizerFormat;
import org.sonar.colorizer.CDocTokenizer;
import org.sonar.colorizer.CppDocTokenizer;
import org.sonar.colorizer.KeywordsTokenizer;
import org.sonar.colorizer.StringTokenizer;
import org.sonar.colorizer.Tokenizer;

public final class CxxColorizer extends CodeColorizerFormat {
  private static final String[] KEYWORDS = {
    "auto", "char", "class", "double", "float", "inline", "int", "long", "private", "protected",
    "public", "return", "short", "static", "struct", "template", "throw", "typedef", "typename",
    "union", "unsigned", "virtual", "void"
  };
  private List<Tokenizer> tokenizers;

  public CxxColorizer() {
    super(CxxLanguage.KEY);
  }

  @Override
  public List<Tokenizer> getTokenizers() {
    if (tokenizers == null) {
      String tagAfter = "</span>";
      tokenizers = new ArrayList<Tokenizer>();
      tokenizers.add(new StringTokenizer("<span class=\"s\">", tagAfter));
      tokenizers.add(new KeywordsTokenizer("<span class=\"k\">", tagAfter, KEYWORDS));
      tokenizers.add(new CDocTokenizer("<span class=\"c\">", tagAfter));
      tokenizers.add(new CppDocTokenizer("<span class=\"c\">", tagAfter));
    }
    return tokenizers;
  }
}
