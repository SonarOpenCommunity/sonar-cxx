/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011-2016 SonarOpenCommunity
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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

/**
 *
 * @author jocs
 */
public class CxxFileTesterHelper {
  
  public static CxxFileTester CreateCxxFileTester(String fileName, String basePath) throws UnsupportedEncodingException, IOException {
    CxxFileTester tester = new CxxFileTester();
    tester.sensorContext = SensorContextTester.create(new File(basePath));

    String content = new String(Files.readAllBytes(new File(tester.sensorContext.fileSystem().baseDir(), fileName).toPath()), "UTF-8");
    tester.sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content));
    tester.cxxFile = tester.sensorContext.fileSystem().inputFile(tester.sensorContext.fileSystem().predicates().hasPath(fileName));
    
    return tester;
  }
  
  public static CxxFileTester CreateCxxFileTester(String fileName, String basePath, String encoding) throws UnsupportedEncodingException, IOException {
    CxxFileTester tester = new CxxFileTester();
    tester.sensorContext = SensorContextTester.create(new File(basePath));

    String content = new String(Files.readAllBytes(new File(tester.sensorContext.fileSystem().baseDir(), fileName).toPath()), encoding);
    tester.sensorContext.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content));
    tester.cxxFile = tester.sensorContext.fileSystem().inputFile(tester.sensorContext.fileSystem().predicates().hasPath(fileName));
    
    return tester;
  }
  
}
