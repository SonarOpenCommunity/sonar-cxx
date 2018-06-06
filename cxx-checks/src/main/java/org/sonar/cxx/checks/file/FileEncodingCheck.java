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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.cxx.visitors.CxxCharsetAwareVisitor;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.NoSqale;
import org.sonar.squidbridge.checks.SquidCheck;

/**
 * FileEncodingCheck
 */
@Rule(
  key = "FileEncoding",
  name = "Verify that all characters of the file can be encoded with the predefined charset.",
  priority = Priority.MINOR)
@ActivatedByDefault
@NoSqale
public class FileEncodingCheck extends SquidCheck<Grammar> implements CxxCharsetAwareVisitor {

  private static final Logger LOG = Loggers.get(FileEncodingCheck.class);
  private Charset charset = Charset.forName("UTF-8");

  @Override
  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  @Override
  public void visitFile(AstNode astNode) {
    try {
      Files.readAllLines(getContext().getFile().toPath(), charset);
    } catch (IOException e) {
      getContext().createFileViolation(this,
        "Not all characters of the file can be encoded with the predefined charset "
        + charset.name() + ".");
      if (LOG.isDebugEnabled()) {
        LOG.debug("Cannot Read File", e);
      }
    }
  }

}
