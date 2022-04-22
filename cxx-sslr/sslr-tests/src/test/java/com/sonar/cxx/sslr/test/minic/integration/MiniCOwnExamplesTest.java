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
package com.sonar.cxx.sslr.test.minic.integration;

import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.impl.Parser;
import com.sonar.cxx.sslr.test.minic.MiniCParser;
import java.io.File;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class MiniCOwnExamplesTest {

  private static final Parser<Grammar> parser = MiniCParser.create();

  @Test
  void test() throws Exception {
    var files = FileUtils.listFiles(new File("src/test/resources/MiniCIntegration"), null, true);
    assertThat(files).isNotEmpty();
    for (var file : files) {
      try {
        parser.parse(file);
      } catch (RuntimeException e) {
        e.printStackTrace();
        throw e;
      }
    }
  }

}
