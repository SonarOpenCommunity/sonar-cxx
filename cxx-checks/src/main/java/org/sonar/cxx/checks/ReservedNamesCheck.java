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
package org.sonar.cxx.checks;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.squid.checks.SquidCheck;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.cxx.api.CxxKeyword;
import com.sonar.sslr.api.Grammar;
import java.util.List;
import com.google.common.io.Files;
import java.io.IOException;
import java.nio.charset.Charset;
import org.sonar.api.utils.SonarException;
import org.sonar.cxx.visitors.CxxCharsetAwareVisitor;

@Rule(
  key = "ReservedNamesCheck",
  description = "Reserved names should not be used for preprocessor macros",
  priority = Priority.BLOCKER)

//similar Vera++ rule T002
public class ReservedNamesCheck extends SquidCheck<Grammar> implements CxxCharsetAwareVisitor {

  private static String[] keywords = null;
  private Charset charset;
  
  @Override
  public void init() {
    keywords = CxxKeyword.keywordValues();
  }

  @Override
  public void visitFile(AstNode astNode) {
    List<String> lines;
    try {
      lines = Files.readLines(getContext().getFile(), charset);
    } catch (IOException e) {
      throw new SonarException(e);
    }
    String[] name = null;
    int nr= 0;
    for (String line : lines) {
      nr++;
      String[] sub = line.split("^.*#define\\s+", 2);
      if (sub.length>1) {
        name = sub[1].toLowerCase().split("\\s",2);
        for (String keyword : keywords) {
          if (name[0].startsWith("_"+keyword) || (name[0].startsWith("__"+keyword))  ) {
            getContext().createLineViolation(this, "Reserved name used for macro (incorrect use of underscore)", nr);
          } else {
            if (name[0].contains(keyword)) {
              getContext().createLineViolation(this, "Reserved name used for macro (keyword or alternative token redefined)", nr);
            }
          }
        }
      }
    }
  }

  @Override
  public void setCharset(Charset charset) {
    this.charset = charset;
  }
}
