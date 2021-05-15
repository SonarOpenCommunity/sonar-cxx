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
package org.sonar.cxx.squidbridge.checks;

import com.sonar.sslr.api.Grammar;
import java.io.File;
import org.apache.commons.io.FileUtils;
import static org.fest.assertions.Assertions.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.sonar.cxx.squidbridge.metrics.ResourceParser.scanFile;

public class ViolationCounterCheckTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void test() throws Exception {
    String projectsDir = FileUtils.toFile(ResourceParser.class.getResource("/checks/parse_error.mc")).getParentFile()
      .getAbsolutePath();

    File output = temporaryFolder.newFile();

    ViolationCounterCheck.ViolationCounter counter = new ViolationCounterCheck.ViolationCounter();
    ViolationCounterCheck<Grammar> violationCounterCheck = new ViolationCounterCheck<Grammar>(projectsDir, counter);

    AbstractParseErrorCheck<Grammar> parseErrorCheck = new AbstractParseErrorCheck<Grammar>() {
    };

    scanFile("/checks/parse_error.mc", parseErrorCheck, violationCounterCheck);

    counter.saveToFile(output.getAbsolutePath());
    assertThat(output).isFile();

    ViolationCounterCheck.ViolationDifferenceAnalyzer analyzer = new ViolationCounterCheck.ViolationDifferenceAnalyzer(
      new ViolationCounterCheck.ViolationCounter(),
      ViolationCounterCheck.ViolationCounter.loadFromFile(output));
    analyzer.printReport();
    assertThat(analyzer.hasDifferences()).isTrue();

    analyzer = new ViolationCounterCheck.ViolationDifferenceAnalyzer(
      ViolationCounterCheck.ViolationCounter.loadFromFile(output),
      new ViolationCounterCheck.ViolationCounter());
    analyzer.printReport();
    assertThat(analyzer.hasDifferences()).isTrue();

    analyzer = new ViolationCounterCheck.ViolationDifferenceAnalyzer(
      ViolationCounterCheck.ViolationCounter.loadFromFile(output),
      ViolationCounterCheck.ViolationCounter.loadFromFile(output));
    analyzer.printReport();
    assertThat(analyzer.hasDifferences()).isFalse();
  }

}
