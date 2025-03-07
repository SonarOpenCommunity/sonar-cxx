/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2025 SonarOpenCommunity
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
package org.sonar.cxx.squidbridge.metrics;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.squidbridge.api.SourceFile;
import static org.sonar.cxx.squidbridge.metrics.ResourceParser.scanFile;
import static org.sonar.cxx.squidbridge.metrics.ResourceParser.scanFileIgnoreHeaderComments;
import org.sonar.cxx.squidbridge.test.miniC.MiniCAstScanner.MiniCMetrics;

class CommentsVisitorTest {

  @Test
  void empty() {
    SourceFile sourceFile = scanFile("/metrics/comments_none.mc");

    assertThat(sourceFile.getInt(MiniCMetrics.COMMENT_LINES)).isZero();

    assertThat(sourceFile.getNoSonarTagLines()).isEmpty();
  }

  @Test
  void comments() {
    SourceFile sourceFile = scanFile("/metrics/comments.mc");

    assertThat(sourceFile.getInt(MiniCMetrics.COMMENT_LINES)).isEqualTo(3);

    assertThat(sourceFile.getNoSonarTagLines()).hasSize(2);
    assertThat(sourceFile.getNoSonarTagLines()).containsOnly(5, 6);
  }

  @Test
  void headerComments() {
    SourceFile sourceFile = scanFileIgnoreHeaderComments("/metrics/header_comments.mc");

    assertThat(sourceFile.getInt(MiniCMetrics.COMMENT_LINES)).isEqualTo(1);
    assertThat(sourceFile.getNoSonarTagLines()).isEmpty();
  }

}
