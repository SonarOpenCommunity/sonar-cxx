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
package org.sonar.cxx.checks.error;

import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.RecognitionException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.cxx.tag.Tag;
import org.sonar.cxx.squidbridge.AstScannerExceptionHandler;
import org.sonar.cxx.squidbridge.annotations.ActivatedByDefault;
import org.sonar.cxx.squidbridge.annotations.NoSqale;
import org.sonar.cxx.squidbridge.checks.SquidCheck;

@Rule(
  key = "ParsingError",
  name = "C++ parser failure",
  tags = {Tag.TOOL_ERROR},
  priority = Priority.MAJOR)
@ActivatedByDefault
@NoSqale
public class ParsingErrorCheck extends SquidCheck<Grammar> implements AstScannerExceptionHandler {

  @Override
  public void processException(Exception e) {
    var exception = new StringWriter();
    e.printStackTrace(new PrintWriter(exception));
    getContext().createFileViolation(this, exception.toString());
  }

  @Override
  public void processRecognitionException(RecognitionException e) {
    getContext().createLineViolation(this, e.getMessage(), e.getLine());
  }

}
