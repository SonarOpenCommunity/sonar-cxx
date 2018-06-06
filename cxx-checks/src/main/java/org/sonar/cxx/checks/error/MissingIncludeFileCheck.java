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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.cxx.parser.CxxParser;
import org.sonar.cxx.preprocessor.CxxPreprocessor;
import org.sonar.cxx.tag.Tag;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.NoSqale;
import org.sonar.squidbridge.checks.SquidCheck;

@Rule(
  key = "MissingIncludeFile",
  name = "C++ preprocessor unable to locate file referenced by #include directive",
  tags = {Tag.PREPROCESSOR},
  priority = Priority.INFO)
@ActivatedByDefault
@NoSqale
public class MissingIncludeFileCheck extends SquidCheck<Grammar> {

  @Override
  public void leaveFile(AstNode astNode) {
    for (CxxPreprocessor.Include missingInclude : CxxParser.getMissingIncludeFiles(getContext().getFile())) {
      getContext().createLineViolation(this, "Unable to find the source for '" + missingInclude.getPath() + "'.",
        missingInclude.getLine());
    }
  }
}
