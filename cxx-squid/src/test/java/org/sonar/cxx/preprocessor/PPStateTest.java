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

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PPStateTest {

  private final String CONTEXT = "contextFile.cpp";
  private PPState state;

  @BeforeEach
  void setUp() {
    var contextFile = new File(CONTEXT);
    state = PPState.build(contextFile);
  }

  @Test
  void testInit() {
    var contextFile = new File("init.cpp");
    state = PPState.build(contextFile);
    assertThat(state.getContextFile()).isEqualTo(contextFile);
    assertThat(state.getFileUnderAnalysisPath()).isEqualTo(contextFile.getAbsolutePath());
  }

  @Test
  void testGetContextFile() {
    var contextFile = new File(CONTEXT);
    state.pushFileState(new File("dummy"));
    assertThat(state.getContextFile()).isEqualTo(contextFile);
  }

  @Test
  void testGetStack() {
    List<String> filenames = ImmutableList.of("A", "B", "C");
    for (var filename : filenames) {
      state.pushFileState(new File(filename));
    }
    List<String> result = new ArrayList();
    for (var item : state.getStack()) {
      result.add(item.getFile().getPath());
    }
    List<String> reverse = new ArrayList(filenames);
    Collections.reverse(reverse);
    reverse.add(CONTEXT);
    assertThat(result).containsExactlyElementsOf(reverse);
  }

  @Test
  void testPushFileState() {
    var file = new File("A");
    state.pushFileState(file);
    assertThat(state.getFileUnderAnalysis()).isEqualTo(file);
    assertThat(state.getFileUnderAnalysisPath()).isEqualTo(file.getAbsolutePath());
  }

  @Test
  void testPopFileState() {
    var fileA = new File("A");
    state.pushFileState(fileA);
    var fileB = new File("B");
    state.pushFileState(fileB);
    state.popFileState();
    assertThat(state.getFileUnderAnalysis()).isEqualTo(fileA);
    assertThat(state.getFileUnderAnalysisPath()).isEqualTo(fileA.getAbsolutePath());
  }

  @Test
  void testSetSkipTokens() {
    assertThat(state.skipTokens()).isFalse();
    state.setSkipTokens(true);
    assertThat(state.skipTokens()).isTrue();
    state.setSkipTokens(false);
    assertThat(state.skipTokens()).isFalse();
  }

  @Test
  void testSetConditionValue() {
    assertThat(state.ifLastConditionWasFalse()).isTrue();
    state.setConditionValue(true);
    assertThat(state.ifLastConditionWasFalse()).isFalse();
    state.setConditionValue(false);
    assertThat(state.ifLastConditionWasFalse()).isTrue();
  }

  @Test
  void testChangeNestingDepth() {
    assertThat(state.isInsideNestedBlock()).isFalse();
    state.changeNestingDepth(+1);
    assertThat(state.isInsideNestedBlock()).isTrue();
    state.changeNestingDepth(-1);
    assertThat(state.isInsideNestedBlock()).isFalse();
  }

  @Test
  void testGetFileUnderAnalysis() {
    var contextFile = new File(CONTEXT);
    assertThat(state.getFileUnderAnalysis()).isEqualTo(contextFile);
  }

  @Test
  void testGetFileUnderAnalysisPath() {
    var contextFile = new File(CONTEXT);
    assertThat(state.getFileUnderAnalysisPath()).isEqualTo(contextFile.getAbsolutePath());
  }

}
