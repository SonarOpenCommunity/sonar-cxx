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

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PPState {

  private Deque<StateItem> ppState = new ArrayDeque<>();

  private PPState() {

  }

  public static PPState build(@Nonnull File contextFile) {
    Objects.requireNonNull(contextFile, "PPState must always be initialized with a contextFile");
    PPState state = new PPState();
    state.pushFileState(contextFile);
    return state;
  }

  public File getContextFile() {
    return ppState.peekLast().getFile();
  }

  public Deque<StateItem> getStack() {
    return ppState;
  }

  public void pushFileState(@Nonnull File currentFile) {
    Objects.requireNonNull(currentFile, "currentFile can' be null");
    ppState.push(new StateItem(currentFile));
  }

  public void popFileState() {
    ppState.pop();
  }

  public void setSkipTokens(boolean state) {
    ppState.peek().skipToken = state;
  }

  public boolean skipTokens() {
    return ppState.peek().skipToken;
  }

  public void setConditionValue(boolean state) {
    ppState.peek().condition = state;
  }

  public boolean ifLastConditionWasFalse() {
    return !ppState.peek().condition;
  }

  public void changeNestingDepth(int dir) {
    ppState.peek().nestingDepth += dir;
  }

  public boolean isInsideNestedBlock() {
    return ppState.peek().nestingDepth > 0;
  }

  public File getFileUnderAnalysis() {
    return ppState.peek().getFile();
  }

  public String getFileUnderAnalysisPath() {
    return ppState.peek().getFile().getAbsolutePath();
  }

  public static class StateItem {

    private boolean skipToken;
    private boolean condition;
    private int nestingDepth;
    private final File fileUnderAnalysis;

    private StateItem(@Nullable File fileUnderAnalysis) {
      this.skipToken = false;
      this.condition = false;
      this.nestingDepth = 0;
      this.fileUnderAnalysis = fileUnderAnalysis;
    }

    public File getFile() {
      return fileUnderAnalysis;
    }
  }

}
