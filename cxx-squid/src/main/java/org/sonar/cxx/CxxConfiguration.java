/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns
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
package org.sonar.cxx;

import org.sonar.squid.api.SquidConfiguration;

import java.nio.charset.Charset;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class CxxConfiguration extends SquidConfiguration {

  private boolean ignoreHeaderComments = false;
  private boolean preprocessorChannelEnabled = true;
  private List<String> defines = new ArrayList<String>();

  public CxxConfiguration() {
  }

  public CxxConfiguration(Charset charset) {
    super(charset);
  }

  public void setIgnoreHeaderComments(boolean ignoreHeaderComments) {
    this.ignoreHeaderComments = ignoreHeaderComments;
  }

  public boolean getIgnoreHeaderComments() {
    return ignoreHeaderComments;
  }

  public void setPreprocessorChannelEnabled(boolean preprocessorChannelEnabled) {
    this.preprocessorChannelEnabled = preprocessorChannelEnabled;
  }

  public boolean getPreprocessorChannelEnabled() {
    return preprocessorChannelEnabled;
  }

  public void setDefines(List<String> defines) {
    this.defines = defines;
  }

  public void setDefines(String[] defines) {
    if(defines != null){
      setDefines(Arrays.asList(defines));
    }
  }

  public List<String> getDefines() {
    return defines;
  }
}
