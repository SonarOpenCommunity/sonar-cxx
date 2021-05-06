/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021 SonarOpenCommunity
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
 * fork of SSLR Squid Bridge: https://github.com/SonarSource/sslr-squid-bridge/tree/2.6.1
 * Copyright (C) 2010 SonarSource / mailto: sonarqube@googlegroups.com / license: LGPL v3
 */
package org.sonar.cxx.squidbridge;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import java.io.File;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

public class ProgressAstScanner<G extends Grammar> extends AstScanner<G> {

  private final ProgressReport progressReport;

  protected ProgressAstScanner(Builder<G> builder) {
    super(builder);
    this.progressReport = builder.progressReport;
  }

  @Override
  public void scanFiles(Collection<File> files) {
    progressReport.start(files);
    boolean success = false;
    try {
      super.scanFiles(files);
      success = true;
    } finally {
      if (success) {
        progressReport.stop();
      } else {
        progressReport.cancel();
      }
    }
  }

  public static class Builder<G extends Grammar> extends AstScanner.Builder<G> {

    private ProgressReport progressReport = new ProgressReport("Report about progress of code analyzer",
                                                               TimeUnit.SECONDS.toMillis(10));

    public Builder(SquidAstVisitorContextImpl<G> context) {
      super(context);
    }

    public Builder<G> setProgressReport(ProgressReport progressReport) {
      this.progressReport = progressReport;
      return this;
    }

    @Override
    public AstScanner<G> build() {
      super.withSquidAstVisitor(new SquidAstVisitor<G>() {

        @Override
        public void leaveFile(@Nullable AstNode astNode) {
          progressReport.nextFile();
        }

      });

      return new ProgressAstScanner<G>(this);
    }

  }

}
