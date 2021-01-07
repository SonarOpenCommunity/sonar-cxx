/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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

import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.impl.Parser;
import java.io.File;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.squidbridge.SquidAstVisitorContext;
import org.sonar.sslr.grammar.GrammarRuleKey;

/**
 * SquidAstVisitorContext is mock with a fake file path. You can use this base class for preprocessing tokens. You
 * shouldn't use it for preprocessing "physical" files.
 */
public class ParserBaseTestHelper {

  protected CxxSquidConfiguration squidConfig = null;
  protected Parser<Grammar> p = null;
  protected Grammar g = null;

  public ParserBaseTestHelper() {
    squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.ERROR_RECOVERY_ENABLED,
                    "false");

    var file = new File("snippet.cpp").getAbsoluteFile();
    SquidAstVisitorContext<Grammar> context = mock(SquidAstVisitorContext.class);
    when(context.getFile()).thenReturn(file);

    p = CxxParser.create(context, squidConfig);
    g = p.getGrammar();
  }

  void mockRule(GrammarRuleKey key) {
    g.rule(key).mock();
  }

}
