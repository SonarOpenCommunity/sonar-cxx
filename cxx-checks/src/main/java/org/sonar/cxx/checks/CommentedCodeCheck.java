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

import com.google.common.collect.Sets;
import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import com.sonar.sslr.squid.checks.SquidCheck;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.cxx.api.CppKeyword;
import org.sonar.squid.recognizer.CodeRecognizer;
import org.sonar.squid.recognizer.ContainsDetector;
import org.sonar.squid.recognizer.Detector;
import org.sonar.squid.recognizer.EndWithDetector;
import org.sonar.squid.recognizer.KeywordsDetector;
import org.sonar.squid.recognizer.LanguageFootprint;
import java.util.Set;
import java.util.regex.Pattern;

@Rule(
  key = "CommentedCode",
  description = "Commented code shall be avoided and therefore removed.",
  priority = Priority.BLOCKER)

public class CommentedCodeCheck extends SquidCheck<Grammar> implements AstAndTokenVisitor {

  private static final double THRESHOLD = 0.94;

  private final CodeRecognizer codeRecognizer = new CodeRecognizer(THRESHOLD, new CxxRecognizer());
  private final Pattern regexpToDivideStringByLine = Pattern.compile("(\r?\n)|(\r)");

  private static class CxxRecognizer implements LanguageFootprint {

    public Set<Detector> getDetectors() {
      Set<Detector> detectors = Sets.newHashSet();

      detectors.add(new EndWithDetector(0.95, '}', ';', '{'));
      detectors.add(new KeywordsDetector(0.7, "||", "&&"));
      detectors.add(new KeywordsDetector(0.3, CppKeyword.keywordValues()));
      detectors.add(new ContainsDetector(0.95, "for(", "if(", "while(", "catch(", "switch(", "try{", "else{"));

      return detectors;
    }

  }

  public void visitToken(Token token) {
    for (Trivia trivia : token.getTrivia()) {
      if (trivia.isComment() && !trivia.getToken().getOriginalValue().startsWith("///")) {
        String lines[] = regexpToDivideStringByLine.split(getContext().getCommentAnalyser().getContents(
            trivia.getToken().getOriginalValue()));

        for (int lineOffset = 0; lineOffset < lines.length; lineOffset++) {
          if (codeRecognizer.isLineOfCode(lines[lineOffset])) {
            getContext().createLineViolation(this, "Remove this commented out code.",
                trivia.getToken().getLine() + lineOffset);
            break;
          }
        }
      }
    }
  }

}
