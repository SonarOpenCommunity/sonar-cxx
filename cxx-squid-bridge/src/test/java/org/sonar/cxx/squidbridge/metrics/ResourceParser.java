/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2022 SonarOpenCommunity
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

import com.sonar.cxx.sslr.api.Grammar;
import java.io.File;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.Assertions.*;
import org.sonar.cxx.squidbridge.AstScanner;
import org.sonar.cxx.squidbridge.SquidAstVisitor;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.squidbridge.indexer.QueryByType;
import org.sonar.cxx.squidbridge.test.miniC.MiniCAstScanner;

public class ResourceParser {

  public static SourceFile scanFile(String filePath, SquidAstVisitor<Grammar>... visitors) {
    return scanFile(filePath, false, visitors);
  }

  public static SourceFile scanFileIgnoreHeaderComments(String filePath, SquidAstVisitor<Grammar>... visitors) {
    return scanFile(filePath, true, visitors);
  }

  private static SourceFile scanFile(String filePath, boolean ignoreHeaderComments, SquidAstVisitor<Grammar>... visitors) {
    AstScanner<Grammar> scanner = ignoreHeaderComments ? MiniCAstScanner.createIgnoreHeaderComments(visitors)
                                    : MiniCAstScanner
        .create(visitors);
    File file = FileUtils.toFile(ResourceParser.class.getResource(filePath));
    if (file == null || !file.exists()) {
      throw new IllegalArgumentException("The file located under \"" + filePath + "\" was not found.");
    }
    scanner.scanFile(file);
    assertThat(scanner.getIndex().search(new QueryByType(SourceFile.class))).hasSize(1);
    return (SourceFile) scanner.getIndex().search(new QueryByType(SourceFile.class)).iterator().next();
  }

}
