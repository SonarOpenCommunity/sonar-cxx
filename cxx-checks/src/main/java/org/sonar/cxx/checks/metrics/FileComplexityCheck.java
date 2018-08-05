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
package org.sonar.cxx.checks.metrics;

import java.util.Optional;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.tag.Tag;
import org.sonar.squidbridge.annotations.SqaleLinearWithOffsetRemediation;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;

@Rule(
  key = "FileComplexity",
  name = "Files should not be too complex",
  tags = {Tag.BRAIN_OVERLOAD},
  priority = Priority.MAJOR)
//@ActivatedByDefault
@SqaleLinearWithOffsetRemediation(
  coeff = "1min",
  offset = "30min",
  effortToFixDescription = "per complexity point above the threshold")
public class FileComplexityCheck extends CxxCyclomaticComplexityCheck<Grammar> {

  private static final int DEFAULT_MAX = 200;

  @RuleProperty(
    key = "max",
    description = "Maximum complexity allowed",
    defaultValue = "" + DEFAULT_MAX)
  private int max = DEFAULT_MAX;

  @Override
  protected Optional<AstNodeType> getScopeType() {
    return Optional.empty();
  }

  @Override
  protected String getScopeName() {
    return "file";
  }

  @Override
  protected int getMaxComplexity() {
    return max;
  }

  public void setMaxComplexity(int max) {
    this.max = max;
  }
}
