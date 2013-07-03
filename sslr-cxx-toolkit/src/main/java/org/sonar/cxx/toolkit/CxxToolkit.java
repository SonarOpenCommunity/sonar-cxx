/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
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
package org.sonar.cxx.toolkit;

import com.google.common.collect.ImmutableList;
import org.sonar.colorizer.KeywordsTokenizer;
import org.sonar.colorizer.Tokenizer;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.parser.CxxParser;
import org.sonar.sslr.toolkit.Toolkit;

import java.util.List;

public final class CxxToolkit {

  private CxxToolkit() {
  }

  public static void main(String[] args) {
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SSDK");
    new Toolkit(CxxParser.create(), getCxxTokenizers(), "SSLR Cxx Toolkit").run();
  }

  public static List<Tokenizer> getCxxTokenizers() {
    return ImmutableList.of(
        (Tokenizer) new KeywordsTokenizer("<span class=\"k\">", "</span>", CxxKeyword.keywordValues()));
  }

}
