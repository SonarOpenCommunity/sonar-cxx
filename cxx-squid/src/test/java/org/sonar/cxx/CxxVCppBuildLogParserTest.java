/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2019 SonarOpenCommunity
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

import org.assertj.core.api.SoftAssertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonar.api.internal.apachecommons.lang.SystemUtils;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

/**
 *
 * @author rudolfgrauberger
 */
public class CxxVCppBuildLogParserTest {

    private static final String VC_CHARSET = "UTF8";

    public static final String OVERALLINCLUDEKEY = "CxxOverallInclude";
    public static final String OVERALLDEFINEKEY = "CxxOverallDefine";
    public static final String REFERENCE_DETAILED_LOG = "src/test/resources/logfile/msbuild-detailed-en.txt";
    public static final String UNIQUE_FILE = "D:\\Development\\Source\\Cpp\\Dummy\\src\\main.cpp";

    @BeforeClass
    public static void init()
    {
        org.junit.Assume.assumeTrue(SystemUtils.IS_OS_WINDOWS);
    }

    @Test
    public void shouldTranslateRelativeIncludesRelativeToProjectFolderFromDetailedReferenceLog() {

        List<String> includes = getIncludesForReferenceLogFile();

        SoftAssertions softly = new SoftAssertions();

        // Absolute path
        softly.assertThat(includes).contains("D:\\Development\\Source\\ThirdParty\\VS2017\\Firebird-2.5.8\\include");
        // Relative paths
        softly.assertThat(includes).contains("D:\\Development\\Source\\ThirdParty\\VS2017\\Boost-1.67.0");
        softly.assertThat(includes).contains("D:\\Development\\Source\\Cpp\\Dummy\\includes");
        softly.assertThat(includes).contains("D:\\Development\\Source\\Cpp\\Dummy\\release");
        softly.assertThat(includes).hasSize(4);
        softly.assertAll();
    }

    @Test
    public void shouldTranslateRelativeIncludesFromDetailedGermanLogAsSameAsFromDetailedReferenceLog() {

        List<String> refIncludes = getIncludesForReferenceLogFile();
        List<String> includes = getIncludesForUniqueFile("src/test/resources/logfile/msbuild-detailed-de.txt");

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(includes).containsExactlyInAnyOrderElementsOf(refIncludes);
        softly.assertAll();
    }

    @Test
    public void shouldTranslateRelativeIncludesFromDetailedFrenchLogAsSameAsFromDetailedReferenceLog() {

        List<String> refIncludes = getIncludesForReferenceLogFile();
        List<String> includes = getIncludesForUniqueFile("src/test/resources/logfile/msbuild-detailed-fr.txt");

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(includes).containsExactlyInAnyOrderElementsOf(refIncludes);
        softly.assertAll();
    }

    private List<String> getIncludesForReferenceLogFile() {
        return getIncludesForUniqueFile(REFERENCE_DETAILED_LOG);
    }

    private List<String> getIncludesForUniqueFile(String log) {
        Map<String, List<String>> uniqueIncludes = new HashMap<>();
        uniqueIncludes.put(OVERALLINCLUDEKEY, new ArrayList<>());
        Map<String, Set<String>> uniqueDefines = new HashMap<>();
        uniqueDefines.put(OVERALLDEFINEKEY, new HashSet<>());

        File logFile = new File(log);

        CxxVCppBuildLogParser parser = new CxxVCppBuildLogParser(uniqueIncludes, uniqueDefines);
        parser.parseVCppLog(logFile, ".", VC_CHARSET);

        SoftAssertions softly = new SoftAssertions();

        List<String> includes = uniqueIncludes.get(UNIQUE_FILE);
        return includes;
    }
}
