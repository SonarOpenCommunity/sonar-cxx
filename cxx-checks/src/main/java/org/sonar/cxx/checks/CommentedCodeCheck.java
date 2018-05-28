/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.cxx.checks;

import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.cxx.api.CppKeyword;
import org.sonar.cxx.tag.Tag;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.squidbridge.recognizer.CodeRecognizer;
import org.sonar.squidbridge.recognizer.ContainsDetector;
import org.sonar.squidbridge.recognizer.Detector;
import org.sonar.squidbridge.recognizer.EndWithDetector;
import org.sonar.squidbridge.recognizer.KeywordsDetector;
import org.sonar.squidbridge.recognizer.LanguageFootprint;

/**
 * CommentedCodeCheck
 */
@Rule(
  key = "CommentedCode",
  name = "Sections of code should not be 'commented out'",
  tags = {Tag.BAD_PRACTICE},
  priority = Priority.CRITICAL)
@ActivatedByDefault
@SqaleConstantRemediation("5min")
public class CommentedCodeCheck extends SquidCheck<Grammar> implements AstAndTokenVisitor {

  private static final double THRESHOLD = 0.94;

  private final CodeRecognizer codeRecognizer = new CodeRecognizer(THRESHOLD, new CxxRecognizer());

  private final Pattern EOLPattern = Pattern.compile("\\R");

  private static class CxxRecognizer implements LanguageFootprint {

    @Override
    public Set<Detector> getDetectors() {
      Set<Detector> detectors = new HashSet<>();

      detectors.add(new EndWithDetector(0.95, '}', ';', '{'));
      detectors.add(new KeywordsDetector(0.7, "||", "&&"));
      detectors.add(new KeywordsDetector(0.3, CppKeyword.keywordValues()));
      detectors.add(new ContainsDetector(0.95, "for(", "if(", "while(", "catch(", "switch(", "try{", "else{"));

      return detectors;
    }

  }

  @Override
  public void visitToken(Token token) {
    for (Trivia trivia : token.getTrivia()) {
      String value = trivia.getToken().getOriginalValue();
      if (trivia.isComment()
        && !value.startsWith("///")
        && !value.startsWith("//!")
        && !value.startsWith("/**")
        && !value.startsWith("/*!")
        && !value.startsWith("/*@")
        && !value.startsWith("//@")) {
        String[] lines = EOLPattern.split(getContext().getCommentAnalyser().getContents(value));

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
