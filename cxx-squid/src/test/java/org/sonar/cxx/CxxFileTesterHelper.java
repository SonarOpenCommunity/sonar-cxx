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
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

/**
 *
 * @author jocs
 */
public class CxxFileTesterHelper {

  public static CxxFileTester CreateCxxFileTester(String fileName, String basePath, String module) throws UnsupportedEncodingException, IOException {
    CxxFileTester tester = new CxxFileTester();
    tester.sensorContext = SensorContextTester.create(new File(basePath));

    tester.sensorContext.fileSystem().add(TestInputFileBuilder.create(module, fileName).build());
    tester.cxxFile = tester.sensorContext.fileSystem().inputFile(tester.sensorContext.fileSystem().predicates().hasPath(fileName));

    return tester;
  }

  public static CxxFileTester AddFileToContext(CxxFileTester tester, String fileName, String module) throws UnsupportedEncodingException, IOException {
    tester.sensorContext.fileSystem().add(TestInputFileBuilder.create(module, fileName).build());
    tester.cxxFile = tester.sensorContext.fileSystem().inputFile(tester.sensorContext.fileSystem().predicates().hasPath(fileName));
    return tester;
  }

  public static CxxLanguage mockCxxLanguage() {
    CxxLanguage language = Mockito.mock(CxxLanguage.class);
    when(language.getKey()).thenReturn("c++");
    when(language.getName()).thenReturn("c++");
    when(language.getPropertiesKey()).thenReturn("cxx");
    when(language.IsRecoveryEnabled()).thenReturn(Optional.of(Boolean.TRUE));
    when(language.getFileSuffixes())
      .thenReturn(new String[]{".cpp", ".hpp", ".h", ".cxx", ".c", ".cc", ".hxx", ".hh"});
    when(language.getHeaderFileSuffixes()).thenReturn(new String[] { ".hpp", ".h", ".hxx", ".hh" });

    return language;
  }
}
