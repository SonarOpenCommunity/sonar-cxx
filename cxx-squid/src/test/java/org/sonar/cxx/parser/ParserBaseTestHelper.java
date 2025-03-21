/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.cxx.parser;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.impl.Parser;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.squidbridge.SquidAstVisitorContextImpl;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;
import org.sonar.cxx.sslr.tests.ParserAssert;

/**
 * SquidAstVisitorContext is mock with a fake file path. You can use this base class for preprocessing tokens. You
 * shouldn't use it for preprocessing "physical" files.
 */
public class ParserBaseTestHelper {

  protected CxxSquidConfiguration squidConfig = null;
  private Parser<Grammar> p = null;
  private Grammar g = null;

  public ParserBaseTestHelper() {
    squidConfig = new CxxSquidConfiguration();
    squidConfig.add(
      CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.ERROR_RECOVERY_ENABLED, "false"
    );

    var file = new File("snippet.cpp").getAbsoluteFile();
    SquidAstVisitorContextImpl<Grammar> context = mock(SquidAstVisitorContextImpl.class);
    when(context.getFile()).thenReturn(file);

    p = CxxParser.create(context, squidConfig);
    g = p.getGrammar();
  }

  public void setRootRule(GrammarRuleKey ruleKey) {
    p.setRootRule(g.rule(ruleKey));
  }

  public void mockRule(GrammarRuleKey ruleKey) {
    g.rule(ruleKey).override(ruleKey.toString());
  }

  public ParserAssert assertThatParser() {
    return org.sonar.cxx.sslr.tests.Assertions.assertThat(p);
  }

  public String parse(String input) {
    return serialize(p.parse(input));
  }

  private String serialize(AstNode root) {
    var values = new LinkedList<String>();
    iterate(root, values);
    return String.join(" ", values);
  }

  private void iterate(@Nullable AstNode node, List<String> values) {
    while (node != null) {
      AstNode child = node.getFirstChild();
      if (child != null) {
        iterate(child, values);
      } else {
        if (node.getType() instanceof CxxGrammarImpl == false) {
          values.add(node.getTokenValue());
        }
      }
      node = node.getNextSibling();
    }
  }

}
