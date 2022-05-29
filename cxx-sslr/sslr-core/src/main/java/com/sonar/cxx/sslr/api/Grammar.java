/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022 SonarOpenCommunity
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
/**
 * fork of SonarSource Language Recognizer: https://github.com/SonarSource/sslr
 * Copyright (C) 2010-2021 SonarSource SA / mailto:info AT sonarsource DOT com / license: LGPL v3
 */
package com.sonar.cxx.sslr.api; // cxx: in use

import com.sonar.cxx.sslr.impl.matcher.RuleDefinition;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.sonar.cxx.sslr.grammar.GrammarException;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;
import org.sonar.cxx.sslr.internal.grammar.MutableParsingRule;
import org.sonar.cxx.sslr.parser.LexerlessGrammar;

/**
 * Use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder} to create instances of this class.
 *
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.</p>
 */
public abstract class Grammar {

  protected Grammar() {
    instanciateRuleFields();
  }

  /**
   * Find all the direct rule fields declared in the given Grammar class.
   * Inherited rule fields are not returned.
   *
   * @param grammarClass
   * the class of the Grammar for which rule fields must be found
   * @return the rule fields declared in this class, excluding the inherited ones
   * @see getAllRuleFields
   */
  public static List<Field> getRuleFields(Class grammarClass) {
    var fields = grammarClass.getDeclaredFields();

    List<Field> ruleFields = new ArrayList<>();
    for (var field : fields) {
      if (Rule.class.isAssignableFrom(field.getType())) {
        ruleFields.add(field);
      }
    }

    return ruleFields;
  }

  /**
   * Find all direct and indirect rule fields declared in the given Grammar class.
   * Inherited rule fields are also returned.
   *
   * @param grammarClass
   * the class of the Grammar for which rule fields must be found
   * @return the rule fields declared in this class, as well as the inherited ones
   * @see getRuleFields
   */
  public static List<Field> getAllRuleFields(Class grammarClass) {
    var ruleFields = getRuleFields(grammarClass);

    var superClass = grammarClass.getSuperclass();
    while (superClass != null) {
      ruleFields.addAll(getRuleFields(superClass));
      superClass = superClass.getSuperclass();
    }

    return ruleFields;
  }

  private void instanciateRuleFields() {
    for (var ruleField : getAllRuleFields(this.getClass())) {
      var ruleName = ruleField.getName();
      try {
        Rule rule;
        if (this instanceof LexerlessGrammar) {
          rule = new MutableParsingRule(ruleName);
        } else {
          rule = new RuleDefinition(ruleName);
        }

        ruleField.setAccessible(true);
        ruleField.set(this, rule);
      } catch (Exception e) {
        throw new GrammarException(e, "Unable to instanciate the rule '" + ruleName + "': " + e.getMessage());
      }
    }
  }

  /**
   * Allows to obtain an instance of grammar rule, which was constructed by
   * {@link org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder} and
   * {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder}.
   *
   * @since 1.18
   */
  public Rule rule(GrammarRuleKey ruleKey) {
    throw new UnsupportedOperationException();
  }

  /**
   * Each Grammar has always an entry point whose name is usually by convention the "Computation Unit".
   *
   * @return the entry point of this Grammar
   */
  public abstract Rule getRootRule();

}
