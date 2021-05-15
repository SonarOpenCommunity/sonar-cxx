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

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.util.List;
import static org.fest.assertions.Assertions.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.sonar.api.utils.log.Logger;

public class ProgressReportTest {

  @Rule
  public final Timeout timeout = new Timeout(5000);

  @Test(timeout = 5000)
  public void test() throws Exception {
    Logger logger = mock(Logger.class);

    var report = new ProgressReport(ProgressReport.class.getName(), 100, logger, "analyzed");

    File file = mock(File.class);
    when(file.getAbsolutePath()).thenReturn("foo");
    report.start(ImmutableList.of(file, file));

    // Wait for start message
    waitForMessage(logger);

    // Wait for at least one progress message
    waitForMessage(logger);

    report.stop();

    // Waits for the thread to die
    // Note: We cannot simply wait for a message here, because it could either be a progress or a stop one
    report.join();

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(logger, atLeast(3)).info(captor.capture());

    List<String> messages = captor.getAllValues();
    assertThat(messages.size()).isGreaterThanOrEqualTo(3);
    assertThat(messages.get(0)).isEqualTo("2 source files to be analyzed");
    for (var i = 1; i < messages.size() - 1; i++) {
      assertThat(messages.get(i)).isEqualTo("0/2 files analyzed, current file: foo");
    }
    assertThat(messages.get(messages.size() - 1)).isEqualTo("2/2" + " source files have been analyzed");
  }

  @Test(timeout = 5000)
  public void testCancel() throws InterruptedException {
    Logger logger = mock(Logger.class);

    var report = new ProgressReport(ProgressReport.class.getName(), 100, logger, "analyzed");
    File file = mock(File.class);
    when(file.getAbsolutePath()).thenReturn("foo");
    report.start(ImmutableList.of(file, file));

    // Wait for start message
    waitForMessage(logger);

    report.cancel();
    report.join();

  }

  private static void waitForMessage(Logger logger) throws InterruptedException {
    synchronized (logger) {
      logger.wait();
    }
  }

}
