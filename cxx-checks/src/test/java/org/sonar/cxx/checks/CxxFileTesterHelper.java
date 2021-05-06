/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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
package org.sonar.cxx.checks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

public class CxxFileTesterHelper {

  private CxxFileTesterHelper() {
    // utility class
  }

  public static CxxFileTester create(String fileName, String basePath)
    throws UnsupportedEncodingException, IOException {
    return create(fileName, basePath, Charset.defaultCharset());
  }

  public static CxxFileTester create(String fileName, String basePath, Charset charset)
    throws UnsupportedEncodingException, IOException {
    var tester = new CxxFileTester();

    tester.context = SensorContextTester.create(new File(basePath));
    tester.cxxFile = createInputFile(fileName, basePath, charset);
    tester.context.fileSystem().add(tester.cxxFile);

    return tester;
  }

  private static DefaultInputFile createInputFile(String fileName, String basePath, Charset charset) throws IOException {
    TestInputFileBuilder fb = TestInputFileBuilder.create("", fileName);

    fb.setCharset(charset);
    fb.setProjectBaseDir(Paths.get(basePath));
    fb.setContents(getSourceCode(Paths.get(basePath, fileName).toFile(), charset));

    return fb.build();
  }

  private static String getSourceCode(File filename, Charset defaultCharset) throws IOException {
    try ( BOMInputStream bomInputStream = new BOMInputStream(new FileInputStream(filename),
                                                             ByteOrderMark.UTF_8,
                                                             ByteOrderMark.UTF_16LE,
                                                             ByteOrderMark.UTF_16BE,
                                                             ByteOrderMark.UTF_32LE,
                                                             ByteOrderMark.UTF_32BE)) {
      ByteOrderMark bom = bomInputStream.getBOM();
      Charset charset = bom != null ? Charset.forName(bom.getCharsetName()) : defaultCharset;
      byte[] bytes = bomInputStream.readAllBytes();
      return new String(bytes, charset);
    }
  }
}
