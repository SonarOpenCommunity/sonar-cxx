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

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.base.Throwables;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.impl.Parser;
import com.sonar.sslr.test.minic.MiniCParser;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.cxx.squidbridge.api.SourceProject;
import org.sonar.cxx.squidbridge.test.miniC.MiniCAstScanner.MiniCMetrics;

public class ProgressAstScannerTest {

  @Test
  public void test() throws Exception {
    SquidAstVisitorContextImpl<Grammar> context = new SquidAstVisitorContextImpl<Grammar>(new SourceProject(""));
    Parser<Grammar> parser = MiniCParser.create();
    AstScanner<Grammar> scanner = new ProgressAstScanner.Builder<Grammar>(context)
      .setBaseParser(parser)
      .setFilesMetric(MiniCMetrics.FILES)
      .build();

    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
      .getLogger(Logger.ROOT_LOGGER_NAME);
    @SuppressWarnings("unchecked")
    Appender<LoggingEvent> mockAppender = mock(Appender.class);
    root.addAppender(mockAppender);

    scanner.scanFile(new File("src/test/resources/metrics/lines.mc"));

    verify(mockAppender).doAppend(argThat(new ArgumentMatcher<LoggingEvent>() {
      @Override
      public boolean matches(final Object argument) {
        return ((LoggingEvent) argument).getFormattedMessage().contains("1 source files to be analyzed");
      }
    }));
  }

  @Test(timeout = 5000)
  public void testInterrupt() throws Exception {
    SquidAstVisitorContextImpl<Grammar> context = new SquidAstVisitorContextImpl<Grammar>(new SourceProject(""));
    CountDownLatch latch = new CountDownLatch(1);
    ProgressReport progress = mock(ProgressReport.class);
    Parser<Grammar> parser = new BlockingParser(latch);

    AstScanner<Grammar> scanner = new ProgressAstScanner.Builder<Grammar>(context)
      .setProgressReport(progress)
      .setBaseParser(parser)
      .setFilesMetric(MiniCMetrics.FILES)
      .build();

    File[] files = {new File("src/test/resources/metrics/lines.mc"), new File("src/test/resources/metrics/lines2.mc")};
    ScannerRunner runner = new ScannerRunner(scanner, files);
    runner.start();

    // wait for first parsing to start
    latch.await();
    verify(progress).start(any(Collection.class));

    runner.interrupt();
    Awaitility.waitAtMost(Duration.TWO_SECONDS).until(new AssertProgressReportCancelled(progress));
    verifyNoMoreInteractions(progress);
  }

  private class AssertProgressReportCancelled implements Runnable {

    private final ProgressReport progress;

    AssertProgressReportCancelled(ProgressReport progress) {
      this.progress = progress;
    }

    public void run() {
      verify(progress).cancel();
    }
  }

  private static class BlockingParser extends Parser<Grammar> {

    private final CountDownLatch latch;

    protected BlockingParser(CountDownLatch latch) {
      super(null);
      this.latch = latch;
    }

    @Override
    public AstNode parse(File f) {
      latch.countDown();
      try {
        Thread.sleep(10_000);
      } catch (InterruptedException e) {
        throw Throwables.propagate(e);
      }
      return null;
    }
  }

  private static class ScannerRunner extends Thread {

    private AstScanner<Grammar> scanner;
    private File[] f;

    ScannerRunner(AstScanner<Grammar> scanner, File[] f) {
      this.setName("TestScannerRunner");
      this.scanner = scanner;
      this.f = f;
    }

    @Override
    public void run() {
      try {
        scanner.scanFiles(Arrays.asList(f));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}
