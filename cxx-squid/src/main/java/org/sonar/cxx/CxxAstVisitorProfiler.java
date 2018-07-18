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
package org.sonar.cxx;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.squidbridge.AstScanner.Builder;
import org.sonar.squidbridge.AstScannerExceptionHandler;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.SquidAstVisitorContext;
import org.sonar.squidbridge.SquidAstVisitorContextImpl;

import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.Token;

public class CxxAstVisitorProfiler {
  private static final Logger LOG = Loggers.get(CxxAstVisitorProfiler.class);

  public static class CxxProfilerDecorator_AstVisitor<G extends Grammar> extends SquidAstVisitor<G> {
    protected SquidAstVisitor<G> v;
    private long startTimestamp = 0;
    private File outFile;

    public CxxProfilerDecorator_AstVisitor(SquidAstVisitor<G> visitor, File outF) {
      v = visitor;
      outFile = outF;
    }

    @Override
    public void setContext(SquidAstVisitorContext<G> context) {
      v.setContext(context);
    }

    @Override
    public SquidAstVisitorContext<G> getContext() {
      return v.getContext();
    }

    @Override
    public void init() {
      v.init();
    }

    @Override
    public void destroy() {
      v.destroy();
    }

    @Override
    public List<AstNodeType> getAstNodeTypesToVisit() {
      return v.getAstNodeTypesToVisit();
    }

    protected void writeEvent(long nanoDuration) {
      String path = "null";
      if (getContext() != null && getContext().getFile() != null) {
        path = getContext().getFile().getAbsolutePath();
      }
      String classname = v.getClass().getName();
      String entry = String.format("{ \"visitor\": \"%s\", \"source_file\": \"%s\", \"duration_ms\": \"%d\" }\n",
          classname, path, TimeUnit.NANOSECONDS.toMillis(nanoDuration));

      try {
        FileUtils.writeStringToFile(outFile, entry, Charset.defaultCharset(), true);
      } catch (IOException e) {
        LOG.debug("Cannot write bench information: {}", e);
      }
    }

    @Override
    public void visitFile(@Nullable AstNode ast) {
      startTimestamp = System.nanoTime();
      v.visitFile(ast);
    }

    @Override
    public void leaveFile(@Nullable AstNode ast) {
      v.leaveFile(ast);
      long nanoDuration = System.nanoTime() - startTimestamp;
      writeEvent(nanoDuration);
    }

    @Override
    public void visitNode(AstNode ast) {
      v.visitNode(ast);
    }

    @Override
    public void leaveNode(AstNode ast) {
      v.leaveNode(ast);
    }
  }

  public static class CxxProfilerDecorator_AstAndTokenVisitor<G extends Grammar>
      extends CxxProfilerDecorator_AstVisitor<G> implements AstAndTokenVisitor {

    public CxxProfilerDecorator_AstAndTokenVisitor(SquidAstVisitor<G> visitor, File outFile) {
      super(visitor, outFile);
    }

    @Override
    public void visitToken(Token token) {
      ((AstAndTokenVisitor) v).visitToken(token);
    }
  }

  public static class CxxProfilerDecorator_AstScannerExceptionHandler<G extends Grammar>
      extends CxxProfilerDecorator_AstVisitor<G> implements AstScannerExceptionHandler {

    public CxxProfilerDecorator_AstScannerExceptionHandler(SquidAstVisitor<G> visitor, File outFile) {
      super(visitor, outFile);
    }

    @Override
    public void processRecognitionException(RecognitionException e) {
      ((AstScannerExceptionHandler) v).processRecognitionException(e);
    }

    @Override
    public void processException(Exception e) {
      ((AstScannerExceptionHandler) v).processException(e);
    }
  }

  public static class DecoratingAstScannerBuilder<G extends Grammar> extends Builder<G> {
    private File outFile;

    public DecoratingAstScannerBuilder(SquidAstVisitorContextImpl<G> context, File f) {
      super(context);
      outFile = f;
    }

    SquidAstVisitor<G> decorate(SquidAstVisitor<G> v) {
      if (v instanceof AstAndTokenVisitor) {
        assert !(v instanceof AstScannerExceptionHandler);
        return new CxxProfilerDecorator_AstAndTokenVisitor<G>(v, outFile);
      } else if (v instanceof AstScannerExceptionHandler) {
        return new CxxProfilerDecorator_AstScannerExceptionHandler<G>(v, outFile);
      } else {
        return new CxxProfilerDecorator_AstVisitor<G>(v, outFile);
      }
    }

    @Override
    public Builder<G> withSquidAstVisitor(SquidAstVisitor<G> visitor) {
      super.withSquidAstVisitor(decorate(visitor));
      return this;
    }

  }

  public static <G extends Grammar> Builder<G> builder(SquidAstVisitorContextImpl<G> context, File outFile) {
    return new DecoratingAstScannerBuilder<G>(context, outFile);
  }

}
