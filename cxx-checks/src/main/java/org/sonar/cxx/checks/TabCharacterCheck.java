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

import com.google.common.io.Files;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.squid.checks.SquidCheck;
import org.sonar.api.utils.SonarException;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.cxx.visitors.CxxCharsetAwareVisitor;
import com.sonar.sslr.api.Grammar;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

@Rule(
  key = "NoTabCharacter",
  description = "Instead of using tab character, multiple whitespaces shall be used",
  priority = Priority.MINOR)

//similar Vera++ rule L002 "Don't use tab characters"
public class TabCharacterCheck extends SquidCheck<Grammar> implements CxxCharsetAwareVisitor {

  private Charset charset;

  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  @Override
  public void visitFile(AstNode astNode) {
    List<String> lines;
    try {
      lines = Files.readLines(getContext().getFile(), charset);
    } catch (IOException e) {
      throw new SonarException(e);
    }
    for (String line : lines) {
      if (line.contains("\t")) {
        getContext().createFileViolation(this, "Replace all tab characters in this file by sequences of white-spaces.");
        break;
      }
    }
  }

}
