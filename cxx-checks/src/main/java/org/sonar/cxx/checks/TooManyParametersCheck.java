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
import org.sonar.api.rule.RuleKey;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
//import org.sonar.plugins.cxx.api.JavaFileScanner;
//import org.sonar.plugins.cxx.api.JavaFileScannerContext;
//import org.sonar.plugins.cxx.api.tree.BaseTreeVisitor;
//import org.sonar.plugins.cxx.api.tree.MethodTree;


import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.squid.checks.SquidCheck;

  @Rule(
      key = TooManyParametersCheck.RULE_KEY,
      priority = Priority.MAJOR)
    @BelongsToProfile(title = "Sonar way", priority = Priority.MAJOR)
    public class TooManyParametersCheck extends SquidCheck<Grammar>  {


      public static final String RULE_KEY = "TooManyParameters";
      private final RuleKey ruleKey = RuleKey.of(CheckList.REPOSITORY_KEY, RULE_KEY);


      private static final int DEFAULT_MAXIMUM = 7;


      @RuleProperty(key = "maximumMethodParameters",
        defaultValue = "" + DEFAULT_MAXIMUM)
      public int maximum = DEFAULT_MAXIMUM;


//      private JavaFileScannerContext context;


//      @Override
//      public void scanFile(JavaFileScannerContext context) {
//        this.context = context;
//        scan(context.getTree());
//      }


//      @Override
//      public void visitMethod(MethodTree tree) {
//        int count = tree.parameters().size();
//        if (count > maximum) {
//          context.addIssue(tree, ruleKey, "Method has " + count + " parameters, which is greater than " + maximum + " authorized.");
//        }


//        super.visitMethod(tree);
//      }


    }


