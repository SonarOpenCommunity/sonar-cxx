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
package org.sonar.cxx.sensors.eclipsecdt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Eclipse CDT operates with absolute offsets in order to address tokens in the
 * source file. We use binary search over the line offsets for the conversion to
 * the SonarQube format (see {@link TextPointer} )
 *
 * Inspired by {@link DefaultInputFile}
 */
public class LinebasedOffsetTranslator {
  private final Integer lineOffsets[];
  private final Integer maxOffset;

  LinebasedOffsetTranslator(String path) throws IOException {
    lineOffsets = readLineOffsets(path);
    maxOffset = lineOffsets[lineOffsets.length - 1];
  }

  private static Integer[] readLineOffsets(String path) throws IOException {
    List<Integer> lineOffsetsList = new ArrayList<>();

    lineOffsetsList.add(0);
    int currentOffset = 0;

    try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
      for (String line; (line = br.readLine()) != null;) {
        // 1 stands for the length of '\n'
        currentOffset = currentOffset + line.length() + 1;
        lineOffsetsList.add(currentOffset);
      }
    }

    return lineOffsetsList.toArray(new Integer[lineOffsetsList.size()]);
  }

  private Integer findLine(Integer globalOffset) {
    return Math.abs(Arrays.binarySearch(lineOffsets, globalOffset) + 1);
  }

  public LinebasedTextPointer newPointer(int globalOffset) throws EclipseCDTException {
    if (globalOffset < 0 || globalOffset > maxOffset) {
      throw new EclipseCDTException(Integer.valueOf(globalOffset).toString() + " must be in range [0, "
          + Integer.valueOf(maxOffset).toString() + "]");
    }

    int line = findLine(globalOffset);
    int startLineOffset = lineOffsets[line - 1];
    return new LinebasedTextPointer(line, globalOffset - startLineOffset);
  }
}
