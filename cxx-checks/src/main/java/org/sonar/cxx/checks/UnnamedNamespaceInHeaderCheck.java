/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
 * sonarqube@googlegroups.com
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

import org.apache.commons.lang.StringUtils;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.squidbridge.checks.SquidCheck;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.annotations.Tags;

@Rule(
  key = "UnnamedNamespaceInHeader",
  name = "Unnamed namespaces are not allowed in header files",
  tags = {Tags.CONVENTION},
  priority = Priority.BLOCKER)
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.ARCHITECTURE_RELIABILITY)
@SqaleConstantRemediation("5min")
//similar Vera++ rule T017
public class UnnamedNamespaceInHeaderCheck extends SquidCheck<Grammar> {

  private static final String DEFAULT_NAME_SUFFIX = ".h,.hh,.hpp,.H";

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.unnamedNamespaceDefinition);
    // ToDo use cxx.suffixes.headers ?? No API
    //   conf.getStringArray(CxxPlugin.INCLUDE_DIRECTORIES_KEY)
  }



  @Override
  public void visitNode(AstNode node) {
    if (isHeader(getContext().getFile().getName())) {
      getContext().createFileViolation(this, "Unnamed namespaces are not allowed in header files.", node);
    }
  }

  private boolean isHeader(String name) {
    String[] suffixes = StringUtils.split(DEFAULT_NAME_SUFFIX, ",");
    for (String suff : suffixes) {
      if (name.endsWith(suff)) {
        return true;
      }
    }
    return false;
  }
}
