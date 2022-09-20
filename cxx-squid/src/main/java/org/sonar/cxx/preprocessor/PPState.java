/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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
package org.sonar.cxx.preprocessor;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class PPState {

  private final ArrayDeque<StateItem> stack = new ArrayDeque<>();

  private PPState() {

  }

  public static PPState build(@Nonnull Path contextFile) {
    Objects.requireNonNull(contextFile, "PPState must always be initialized with a contextFile");
    PPState state = new PPState();
    state.pushFileState(contextFile);
    return state;
  }

  public Path getContextFile() {
    return stack.peekLast().getFile();
  }

  public ArrayDeque<StateItem> getStack() {
    return stack.clone();
  }

  public void pushFileState(@Nonnull Path currentFile) {
    Objects.requireNonNull(currentFile, "currentFile can' be null");
    stack.push(new StateItem(currentFile));
  }

  public void popFileState() {
    stack.pop();
  }

  public void setSkipTokens(boolean state) {
    stack.peek().skipToken = state;
  }

  public boolean skipTokens() {
    return stack.peek().skipToken;
  }

  public void setConditionValue(boolean state) {
    stack.peek().condition = state;
  }

  public boolean ifLastConditionWasFalse() {
    return !stack.peek().condition;
  }

  public void changeNestingDepth(int dir) {
    stack.peek().nestingDepth += dir;
  }

  public boolean isInsideNestedBlock() {
    return stack.peek().nestingDepth > 0;
  }

  public Path getFileUnderAnalysis() {
    return stack.peek().getFile();
  }

  public String getFileUnderAnalysisPath() {
    return getFileUnderAnalysis().toAbsolutePath().toString();
  }

  public static final class StateItem {

    private boolean skipToken;
    private boolean condition;
    private int nestingDepth;
    private final Path fileUnderAnalysis;

    private StateItem(@Nullable Path fileUnderAnalysis) {
      this.skipToken = false;
      this.condition = false;
      this.nestingDepth = 0;
      this.fileUnderAnalysis = fileUnderAnalysis;
    }

    public Path getFile() {
      return fileUnderAnalysis;
    }
  }

}
