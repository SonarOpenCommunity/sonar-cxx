/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
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
package org.sonar.cxx.parser;

import org.sonar.cxx.CxxConfiguration;

import com.sonar.sslr.impl.Parser;
import com.sonar.sslr.squid.SquidAstVisitorContext;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import com.sonar.sslr.api.Grammar;
import java.net.URISyntaxException;

import java.io.File;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.fail;


public class CxxParserTest extends ParserBaseTest {
  String errSources = "/parser/bad/error_recovery_declaration.cc";
  String[] goodFiles = {"own", "examples"};
  String[] cCompatibilityFiles = {"c-compat"};
  String rootDir = "src/test/resources/parser";
  File erroneousSources = null;

  public CxxParserTest(){
    super();
    try{
      erroneousSources = new File(CxxParserTest.class.getResource(errSources).toURI());
    } catch (java.net.URISyntaxException e) {}
  }

  @Test
  public void testParsingOnDiverseSourceFiles() {
    Collection<File> files = listFiles(goodFiles, new String[] {"cc", "cpp", "hpp"});
    for (File file : files) {
      p.parse(file);
      CxxParser.finishedParsing(file);
    }
  }

  @Test
  public void testParsingInCCompatMode() {
    p = CxxParser.create(mock(SquidAstVisitorContext.class), conf);

    Collection<File> files = listFiles(cCompatibilityFiles, new String[] {"c"});
    for (File file : files) {
      p.parse(file);
      CxxParser.finishedParsing(file);
    }
  }

  @Test
  public void testParseErrorRecovery() {
    // The error recovery works, if:
    // - a syntacticly incorrect file causes a parse error when recovery is disabled
    // - but doesnt cause such an error if we run with default settings

    try{
      p.parse(erroneousSources);
      fail("Parser could not recognize the syntax error");
    }
    catch(com.sonar.sslr.api.RecognitionException re){
    }

    conf.setErrorRecoveryEnabled(true);
    p = CxxParser.create(mock(SquidAstVisitorContext.class), conf);
    p.parse(erroneousSources); //<-- this shouldnt throw now
  }

  private Collection<File> listFiles(String[] dirs, String[] extensions) {
    List<File> files = new ArrayList<File>();
    for(String dir: dirs){
      files.addAll(FileUtils.listFiles(new File(rootDir, dir), extensions, true));
    }
    return files;
  }

}
