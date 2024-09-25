/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2024 SonarOpenCommunity
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
package org.sonar.cxx.sslr.channel; // cxx: in use

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration parameters used by a CodeReader to handle some specificities.
 */
public class CodeReaderConfiguration {

  public static final int DEFAULT_TAB_WIDTH = 1;

  private int tabWidth = DEFAULT_TAB_WIDTH;

  private List<CodeReaderFilter<?>> codeReaderFilters = new ArrayList<>();

  /**
   * @return the tabWidth
   */
  public int getTabWidth() {
    return tabWidth;
  }

  /**
   * @param tabWidth
   * the tabWidth to set
   */
  public void setTabWidth(int tabWidth) {
    this.tabWidth = tabWidth;
  }

  /**
   * @return the codeReaderFilters
   */
  @SuppressWarnings("rawtypes")
  public CodeReaderFilter[] getCodeReaderFilters() {
    return codeReaderFilters.toArray(new CodeReaderFilter[codeReaderFilters.size()]);
  }

  /**
   * @param codeReaderFilters
   * the codeReaderFilters to set
   */
  public void setCodeReaderFilters(CodeReaderFilter<?>... codeReaderFilters) {
    this.codeReaderFilters = new ArrayList<>(Arrays.asList(codeReaderFilters));
  }

  /**
   * Adds a code reader filter
   *
   * @param codeReaderFilter
   * the codeReaderFilter to add
   */
  public void addCodeReaderFilters(CodeReaderFilter<?> codeReaderFilter) {
    this.codeReaderFilters.add(codeReaderFilter);
  }

  public CodeReaderConfiguration cloneWithoutCodeReaderFilters() {
    var clone = new CodeReaderConfiguration();
    clone.setTabWidth(tabWidth);
    return clone;
  }

}
