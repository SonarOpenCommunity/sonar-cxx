/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx;

import net.sourceforge.pmd.cpd.CPPLanguage;
import net.sourceforge.pmd.cpd.Tokenizer;

import org.sonar.api.batch.AbstractCpdMapping;
import org.sonar.api.resources.Language;

/**
 * {@inheritDoc}
 */
public final class CxxCpdMapping extends AbstractCpdMapping {

  private final CPPLanguage language = new CPPLanguage();
  private CxxLanguage lang;

  /**
   *  {@inheritDoc}
   */
  public CxxCpdMapping(CxxLanguage lang) {
    this.lang = lang;
  }

  /**
   *  {@inheritDoc}
   */
  public Language getLanguage() {
    return lang;
  }

  /**
   *  {@inheritDoc}
   */
  public Tokenizer getTokenizer() {
    return language.getTokenizer();
  }
}
