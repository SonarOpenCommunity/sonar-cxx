/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.highlighter;

import org.sonar.api.source.Highlightable;
import org.sonar.cxx.CxxConfiguration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class CxxHighlighterTest {

  private final CxxHighlighter highlighter = new CxxHighlighter(new CxxConfiguration());

  private Highlightable.HighlightingBuilder highlight(String string) throws Exception {
    Highlightable highlightable = mock(Highlightable.class);
    Highlightable.HighlightingBuilder builder = mock(Highlightable.HighlightingBuilder.class);
    when(highlightable.newHighlighting()).thenReturn(builder);
    highlighter.highlight(highlightable, string);
    verify(builder).done();
    return builder;
  }

  @Test
  public void empty_input() throws Exception {
    Highlightable.HighlightingBuilder builder = highlight("");
    verifyNoMoreInteractions(builder);
  }

  @Test
  public void multiline_comment() throws Exception {
    Highlightable.HighlightingBuilder builder = highlight("  /*Comment*/ ");
    verify(builder).highlight(2, 13, "cd");
  }

  @Test
  public void single_line_comment() throws Exception {
    Highlightable.HighlightingBuilder builder = highlight("  //Comment ");
    verify(builder).highlight(2, 12, "cd");
  }

  @Test
  public void javadoc_comment() throws Exception {
    Highlightable.HighlightingBuilder builder = highlight("  /**Comment*/ ");
    verify(builder).highlight(2, 14, "j");
  }

  @Test
  public void keyword() throws Exception {
    Highlightable.HighlightingBuilder builder = highlight("int x = 0");
    verify(builder).highlight(0, 3, "k");
  }

  @Test
  public void constantDecNumber() throws Exception {
    Highlightable.HighlightingBuilder builder = highlight("int x = 12;");
    verify(builder).highlight(8, 10, "c");
  }

  @Test
  public void constantHexNumber() throws Exception {
    Highlightable.HighlightingBuilder builder = highlight("int x = 0x12;");
    verify(builder).highlight(8, 12, "c");
  }

  @Test
  public void constantOctNumber() throws Exception {
    Highlightable.HighlightingBuilder builder = highlight("int x = 012;");
    verify(builder).highlight(8, 11, "c");
  }

  @Test
  public void constantCharacter() throws Exception {
    Highlightable.HighlightingBuilder builder = highlight("char c = 'c';");
    verify(builder).highlight(9, 12, "c");
  }

  @Test
  public void string() throws Exception {
    Highlightable.HighlightingBuilder builder = highlight("char* x = \"a\";");
    verify(builder).highlight(10, 13, "s");
  }

  @Test
  public void preprocessingDirective() throws Exception {
    Highlightable.HighlightingBuilder builder = highlight(" #pragma test ");
    verify(builder).highlight(1, 14, "p");
  }

}
