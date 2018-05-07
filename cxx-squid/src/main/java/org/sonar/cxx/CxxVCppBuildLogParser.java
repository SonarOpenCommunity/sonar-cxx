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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * CxxVCppBuildLogParser
 */
public class CxxVCppBuildLogParser {

  private static final String MSC_IX86_600 = "_M_IX86 600";

  private static final String MSC_X64_100 = "_M_X64 100";

  private static final Logger LOG = Loggers.get(CxxVCppBuildLogParser.class);

  private final Map<String, List<String>> uniqueIncludes;
  private final Map<String, Set<String>> uniqueDefines;

  private String platformToolset = "V120";
  private String platform = "Win32";
  private static final String CPPWINRTVERSION = "__cplusplus_winrt=201009";
  private static final String CPPVERSION = "__cplusplus=199711L";

  private static final Pattern[] includePatterns = { Pattern.compile("/I\"(.*?)\""),
      Pattern.compile("/I([^\\s\"]+) ") };
  private static final Pattern[] definePatterns = { Pattern.compile("[/-]D\\s([^\\s]+)"),
      Pattern.compile("[/-]D([^\\s]+)") };
  private static final Pattern pathToCLPattern = Pattern.compile("^.*\\\\bin\\\\.*CL.exe\\x20.*$");
  private static final Pattern plattformX86Pattern = Pattern.compile("Building solution configuration \".*\\|x64\".");
  private static final Pattern toolsetV141Pattern = Pattern
      .compile("^.*VC\\\\Tools\\\\MSVC\\\\14\\.1\\d\\.\\d+\\\\bin\\\\HostX(86|64)\\\\x(86|64)\\\\CL.exe.*$");

  /**
   * CxxVCppBuildLogParser (ctor)
   *
   * @param uniqueIncludesIn
   * @param uniqueDefinesIn
   */
  public CxxVCppBuildLogParser(Map<String, List<String>> uniqueIncludesIn,
      Map<String, Set<String>> uniqueDefinesIn) {
    uniqueIncludes = uniqueIncludesIn;
    uniqueDefines = uniqueDefinesIn;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  /**
   *
   * @param platformToolset
   */
  public void setPlatformToolset(String platformToolset) {
    this.platformToolset = platformToolset;
  }

  /**
   * Can be used to create a list of includes, defines and options for a single line If it follows the format of VC++
   *
   * @param line
   * @param projectPath
   * @param compilationFile
   */
  public void parseVCppLine(String line, String projectPath, String compilationFile) {
    this.parseVCppCompilerCLLine(line, projectPath, compilationFile);
  }

  /**
   *
   * @param buildLog
   * @param baseDir
   * @param charsetName
   */
  public void parseVCppLog(File buildLog, String baseDir, String charsetName) {
    boolean detectedPlatform = false;
    try (InputStream input = java.nio.file.Files.newInputStream(buildLog.toPath())) {
      BufferedReader br = new BufferedReader(new InputStreamReader(input, charsetName));
      String line;
      if (LOG.isDebugEnabled()) {
        LOG.debug("build log parser baseDir='{}'", baseDir);
      }
      Path currentProjectPath = Paths.get(baseDir);

      List<String> overallIncludes = uniqueIncludes.get(CxxConfiguration.OVERALLINCLUDEKEY);

      while ((line = br.readLine()) != null) {
        if (line.trim().startsWith("INCLUDE=")) { // handle environment includes
          String[] includes = line.split("=")[1].split(";");
          for (String include : includes) {
            if (!overallIncludes.contains(include)) {
              overallIncludes.add(include);
            }
          }
        }

        // get base path of project to make
        // Target "ClCompile" in file
        // "C:\Program Files (x86)\MSBuild\Microsoft.Cpp\v4.0\V120\Microsoft.CppCommon.targets"
        // from project
        // "D:\Development\SonarQube\cxx\sonar-cxx\integration-tests\testdata\googletest_bullseye_vs_project\
        //         PathHandling.Test\PathHandling.Test.vcxproj" (target "_ClCompile" depends on it):
        if (line.contains("Target \"ClCompile\" in file")) {
          String pathProject = line.split("\" from project \"")[1].split("\\s+")[0].replace("\"", "");
          if (pathProject.endsWith(":")) {
            pathProject = pathProject.substring(0, pathProject.length() - 2);
          }
          currentProjectPath = Paths.get(pathProject).getParent();
          if (currentProjectPath == null) {
            currentProjectPath = Paths.get(baseDir);
          }
          if (LOG.isDebugEnabled()) {
            LOG.debug("build log parser currentProjectPath='{}'", currentProjectPath);
          }
        }
        // 1>Task "Message"
        // 1>  Configuration=Debug
        // 1>Done executing task "Message".
        // 1>Task "Message"
        //1>  Platform=Win32
        String lineTrimmed = line.trim();
        if (lineTrimmed.endsWith("Platform=x64") || plattformX86Pattern.matcher(lineTrimmed).matches()) {
          setPlatform("x64");
          if (LOG.isDebugEnabled()) {
            LOG.debug("build log parser platform='{}'", this.platform);
          }
        }
        // match "bin\CL.exe", "bin\amd64\CL.exe", "bin\x86_amd64\CL.exe"
        if (pathToCLPattern.matcher(line).matches()) {
          detectedPlatform = setPlatformToolsetFromLine(line);
          String[] allElems = line.split("\\s+");
          String data = allElems[allElems.length - 1];
          parseCLParameters(line, currentProjectPath, data);
          if (LOG.isDebugEnabled()) {
            LOG.debug("build log parser cl.exe line='{}'", line);
          }
        }
      }
      br.close();
    } catch (IOException ex) {
      LOG.error("Cannot parse build log", ex);
    }
    if (!detectedPlatform) {
      LOG.info("Could not assign VS platform toolset - use default: {}", platformToolset);
    }
  }

  /**
   * setPlatformToolsetFromLine
   *
   * @param line - which contains "cl.exe" string
   */
  private boolean setPlatformToolsetFromLine(String line) {
    if (line.contains("\\V100\\Microsoft.CppBuild.targets")
      || line.contains("Microsoft Visual Studio 10.0\\VC\\bin\\CL.exe")) {
      setPlatformToolset("V100");
      return true;
    } else if (line.contains("\\V110\\Microsoft.CppBuild.targets")
      || line.contains("Microsoft Visual Studio 11.0\\VC\\bin\\CL.exe")) {
      setPlatformToolset("V110");
      return true;
    } else if (line.contains("\\V120\\Microsoft.CppBuild.targets")
      || line.contains("Microsoft Visual Studio 12.0\\VC\\bin\\CL.exe")) {
      setPlatformToolset("V120");
      return true;
    } else if (line.contains("\\V140\\Microsoft.CppBuild.targets")
      || line.contains("Microsoft Visual Studio 14.0\\VC\\bin\\CL.exe")
      || line.contains("Microsoft Visual Studio 14.0\\VC\\bin\\amd64\\cl.exe")) {
      setPlatformToolset("V140");
      return true;
    } else if (line.contains("\\V141\\Microsoft.CppBuild.targets")
        || toolsetV141Pattern.matcher(line).matches()) {
      setPlatformToolset("V141");
      return true;
    } else {
      // do nothing
    }
    return false;
  }

  /**
   * @param line
   * @param currentProjectPath
   * @param data
   */
  private void parseCLParameters(String line, Path currentProjectPath, String data) {
    String path = data.replaceAll("\"", "");
    String fileElement;
    try {
      // a) if path is empty: fileElement == currentProjectPath
      // b) if path is absolute: fileElement == path
      // c) otherwise fileElement == currentProjectPath\path
      fileElement = currentProjectPath.resolve(path).toAbsolutePath().toString();

      if (!uniqueDefines.containsKey(fileElement)) {
        uniqueDefines.put(fileElement, new HashSet<String>());
      }

      if (!uniqueIncludes.containsKey(fileElement)) {
        uniqueIncludes.put(fileElement, new ArrayList<String>());
      }

      parseVCppCompilerCLLine(line, currentProjectPath.toAbsolutePath().toString(), fileElement);
    } catch (InvalidPathException ex) {
      LOG.warn("Cannot extract information from current element: {} - {}", data, ex);
    } catch (NullPointerException ex2) {
      LOG.error("Bug in parser, please report: '{}' - '{}'", data + " @ " + currentProjectPath, ex2);
    }
  }

  private void parseVCppCompilerCLLine(String line, String projectPath, String fileElement) {
    for (Pattern includePattern : includePatterns) {
      for (String includeElem : getMatches(includePattern, line)) {
        parseInclude(includeElem, projectPath, fileElement);
      }
    }

    for (Pattern definePattern : definePatterns) {
      for (String macroElem : getMatches(definePattern, line)) {
        addMacro(macroElem, fileElement);
      }
    }


    // https://msdn.microsoft.com/en-us/library/vstudio/b0084kay(v=vs.100).aspx
    // https://msdn.microsoft.com/en-us/library/vstudio/b0084kay(v=vs.110).aspx
    // https://msdn.microsoft.com/en-us/library/vstudio/b0084kay(v=vs.120).aspx
    // https://msdn.microsoft.com/en-us/library/vstudio/b0084kay(v=vs.140).aspx
    parseCommonCompilerOptions(line, fileElement);

    if ("V100".equals(platformToolset)) {
      parseV100CompilerOptions(line, fileElement);
    } else if ("V110".equals(platformToolset)) {
      parseV110CompilerOptions(line, fileElement);
    } else if ("V120".equals(platformToolset)) {
      parseV120CompilerOptions(line, fileElement);
    } else if ("V140".equals(platformToolset)) {
      parseV140CompilerOptions(line, fileElement);
    } else if ("V141".equals(platformToolset)) {
      parseV141CompilerOptions(line, fileElement);
    } else {
      // do nothing
    }
  }

  private static List<String> getMatches(Pattern pattern, String text) {
    List<String> matches = new ArrayList<>();
    Matcher m = pattern.matcher(text);
    while (m.find()) {
      matches.add(m.group(1));
    }
    return matches;
  }

  private void parseInclude(String element, String project, String fileElement) {

    List<String> includesPerUnit = uniqueIncludes.get(fileElement);

    try {
      File includeRoot = new File(element.replace("\"", ""));
      String includePath;
      Path p = Paths.get(project);
      if (!includeRoot.isAbsolute()) {
        // handle path without drive information but represent absolute path
        File pseudoAbsolute = new File(p.getRoot().toString(), includeRoot.toString());
        if (pseudoAbsolute.exists()) {
          includeRoot = new File(p.getRoot().toString(), includeRoot.getPath());
        } else {
          includeRoot = new File(project, includeRoot.getPath());
        }
      }
      includePath = includeRoot.getCanonicalPath();
      if (!includesPerUnit.contains(includePath)) {
        includesPerUnit.add(includePath);
      }
    } catch (IOException io) {
      if (LOG.isDebugEnabled()) {
        LOG.error("Cannot parse include path using element '{}' : '{}'", element, io);
      } else {
        LOG.error("Cannot parse include path using element '{}' : '{}'", element, io.getMessage());
      }
    }
  }

  private void addMacro(String macroElem, String file) {

    Set<String> definesPerUnit = uniqueDefines.get(file);

    String macro = macroElem.replace('=', ' ');
    if (!definesPerUnit.contains(macro)) {
      definesPerUnit.add(macro);
    }
  }

  private boolean existMacro(String macroElem, String file) {
    Set<String> definesPerUnit = uniqueDefines.get(file);
    String macro = macroElem.replace('=', ' ');
    return definesPerUnit.contains(macro);
  }

  private void parseCommonCompilerOptions(String line, String fileElement) {
    // Always Defined //
    //_INTEGRAL_MAX_BITS Reports the maximum size (in bits) for an integral type.
    addMacro("_INTEGRAL_MAX_BITS=64", fileElement);
    //_MSC_BUILD Evaluates to the revision number component of the compiler's version number. The revision number is
    // the fourth component of the period-delimited version number. For example, if the version number of the
    // Visual C++ compiler is 15.00.20706.01, the _MSC_BUILD macro evaluates to 1.
    addMacro("_MSC_BUILD=1", fileElement);
    //__COUNTER__ Expands to an integer starting with 0 and incrementing by 1 every time it is used in a source file
    // or included headers of the source file. __COUNTER__ remembers its state when you use precompiled headers.
    addMacro("__COUNTER__=0", fileElement);
    //__DATE__ The compilation date of the current source file. The date is a string literal of the form Mmm dd yyyy.
    // The month name Mmm is the same as for dates generated by the library function asctime declared in TIME.H.
    addMacro("__DATE__=\"??? ?? ????\"", fileElement);
    //__FILE__ The name of the current source file. __FILE__ expands to a string surrounded by double quotation marks.
    // To ensure that the full path to the file is displayed, use /FC (Full Path of Source Code File in Diagnostics).
    addMacro("__FILE__=\"file\"", fileElement);
    //__LINE__ The line number in the current source file. The line number is a decimal integer constant.
    // It can be changed with a #line directive.
    addMacro("__LINE__=1", fileElement);
    //__TIME__ The most recent compilation time of the current source file.
    // The time is a string literal of the form hh:mm:ss.
    addMacro("__TIME__=\"??:??:??\"", fileElement);
    //__TIMESTAMP__ The date and time of the last modification of the current source file,
    // expressed as a string literal in the form Ddd Mmm Date hh:mm:ss yyyy, where Ddd is
    // the abbreviated day of the week and Date is an integer from 1 to 31.
    addMacro("__TIMESTAMP__=\"??? ?? ???? ??:??:??\"", fileElement);
    // _M_IX86
    //    /GB _M_IX86 = 600 Blend
    //    /G5 _M_IX86 = 500 (Default. Future compilers will emit a different value to reflect the dominant processor.) Pentium
    //    /G6 _M_IX86 = 600  Pentium Pro, Pentium II, and Pentium III
    //    /G3 _M_IX86 = 300  80386
    //    /G4 _M_IX86 = 400  80486
    if (line.contains("/GB ") || line.contains("/G6")) {
      addMacro("_M_IX86=600", fileElement);
    }
    if (line.contains("/G5")) {
      addMacro("_M_IX86=500", fileElement);
    }
    if (line.contains("/G3")) {
      addMacro("_M_IX86=300", fileElement);
    }
    if (line.contains("/G4")) {
      addMacro("_M_IX86=400", fileElement);
    }
    //_M_IX86_FP Expands to a value indicating which /arch compiler option was used:
    //    0 if /arch was not used.
    //    1 if /arch:SSE was used.
    //    2 if /arch:SSE2 was used.
    // Expands to an integer literal value indicating which /arch compiler option was used.
    // The default value is '2' if /arch was not specified
    addMacro("_M_IX86_FP=2", fileElement);
    if (line.contains("/arch:IA32")) {
      addMacro("_M_IX86_FP=0", fileElement);
    }
    if (line.contains("/arch:SSE")) {
      addMacro("_M_IX86_FP=1", fileElement);
    }
    //arch:ARMv7VE or /arch:VFPv4
    if (line.contains("/arch:ARMv7VE")) {
      addMacro("_M_ARM=7", fileElement);
      addMacro("_M_ARM_ARMV7VE=1", fileElement);
    }
    if (line.contains("/arch:VFPv4")) {
      addMacro("_M_ARM=7", fileElement);
    }
    // WinCE and WinRT
    // see https://en.wikipedia.org/wiki/ARM_architecture
    if (line.contains("/arch:IA32 ")
      || line.contains("/arch:SSE ")
      || line.contains("/arch:SSE2 ")
      || line.contains("/arch:AVX2 ")
      || line.contains("/arch:AVX ")
      || line.contains("/arch:VFPv4 ")
      || line.contains("/arch:ARMv7VE ")) {
      // In the range 30-39 if no /arch ARM option was specified, indicating the default architecture for ARM was used (VFPv3).
      // In the range 40-49 if /arch:VFPv4 was used.
      addMacro("_M_ARM_FP", fileElement);
    }
    // __STDC__ Indicates full conformance with the ANSI C standard. Defined as the integer constant 1 only if
    // the /Za compiler option is given and you are not compiling C++ code; otherwise is undefined.
    if (line.contains("/Za ")) {
      addMacro("__STDC__=1", fileElement);
    }

    //_CHAR_UNSIGNED Default char type is unsigned. Defined when /J is specified.
    if (line.contains("/J ")) {
      addMacro("_CHAR_UNSIGNED=1", fileElement);
    }

    //_CPPRTTI Defined for code compiled with /GR (Enable Run-Time Type Information).
    if (line.contains("/GR ")) {
      addMacro("_CPPRTTI", fileElement);
    }

    //_MANAGED Defined to be 1 when /clr is specified.
    if (line.contains("/clr ")) {
      addMacro("_MANAGED", fileElement);
    }
    //_M_CEE_PURE Defined for a compilation that uses /clr:pure.
    if (line.contains("/clr:pure ")) {
      addMacro("_M_CEE_PURE", fileElement);
    }
    //_M_CEE_SAFE Defined for a compilation that uses /clr:safe.
    if (line.contains("/clr:safe ")) {
      addMacro("_M_CEE_SAFE", fileElement);
    }
    // __CLR_VER Defines the version of the common language runtime used when the application was compiled.
    // The value returned will be in the following format:
    // __cplusplus_cli Defined when you compile with /clr, /clr:pure, or /clr:safe. Value of __cplusplus_cli is 200406.
    // __cplusplus_cli is in effect throughout the translation unit.
    //_M_CEE Defined for a compilation that uses any form of /clr (/clr:oldSyntax, /clr:safe, for example).
    if (line.contains("/clr")) {

      addMacro("_M_CEE", fileElement);
      addMacro("__cplusplus_cli=200406", fileElement);
      addMacro("__CLR_VER", fileElement);
      if (line.contains("/clr:pure ")) {
        addMacro("_M_CEE_PURE", fileElement);
      }
      if (line.contains("/clr:safe ")) {
        addMacro("_M_CEE_SAFE", fileElement);
      }
    }

    //_MSC_EXTENSIONS This macro is defined when you compile with the /Ze compiler option (the default). Its value, when defined, is 1.
    if (line.contains("/Ze ")) {
      addMacro("_MSC_EXTENSIONS", fileElement);
    }

    //__MSVC_RUNTIME_CHECKS Defined when one of the /RTC compiler options is specified.
    if (line.contains("/RTC ")) {
      addMacro("__MSVC_RUNTIME_CHECKS", fileElement);
    }

    //_DEBUG Defined when you compile with /LDd, /MDd, and /MTd.
    if (line.contains("/LDd ")) {
      addMacro("_DEBUG", fileElement);
    }
    //_DLL Defined when /MD or /MDd (Multithreaded DLL) is specified.
    if (line.contains("/MD ") || line.contains("/MDd ")) {
      addMacro("_DLL", fileElement);
    }
    //_MT Defined when /MD (Multithreaded DLL) or /MT (Multithreaded) is specified.
    if (line.contains("/MD ") || line.contains("/MT ")) {
      addMacro("_MT", fileElement);
    }
    //_MT Defined when /MDd (Multithreaded DLL) or /MTd (Multithreaded) is specified.
    if (line.contains("/MDd ") || line.contains("/MTd ")) {
      addMacro("_MT", fileElement);
      addMacro("_DEBUG", fileElement);
    }
    //_OPENMP Defined when compiling with /openmp, returns an integer representing the date of the
    // OpenMP specification implemented by Visual C++.
    if (line.contains("/openmp ")) {
      addMacro("_OPENMP=200203", fileElement);
    }

    //_VC_NODEFAULTLIB Defined when /Zl is used; see /Zl (Omit Default Library Name) for more information.
    if (line.contains("/Zl ")) {
      addMacro("_VC_NODEFAULTLIB", fileElement);
    }

    //_NATIVE_WCHAR_T_DEFINED Defined when /Zc:wchar_t is used.
    //_WCHAR_T_DEFINED Defined when /Zc:wchar_t is used or if wchar_t is defined in a system header file
    // included in your project.
    if (line.contains("/Zc:wchar_t ")) {
      addMacro("_WCHAR_T_DEFINED=1", fileElement);
      addMacro("_NATIVE_WCHAR_T_DEFINED=1", fileElement);
    }

    //_Wp64 Defined when specifying /Wp64. Deprecated in Visual Studio 2010 and Visual Studio 2012,
    // and not supported starting in Visual Studio 2013
    if (line.contains("/Wp64 ")) {
      addMacro("_Wp64", fileElement);
    }

    //_M_AMD64 Defined for x64 processors.
    //_WIN32 Defined for applications for Win32 and Win64. Always defined.
    //_WIN64 Defined for applications for Win64.
    //_M_X64 Defined for x64 processors.
    //_M_IX86 Defined for x86 processors. See the Values for _M_IX86 table below for more information.
    //  This is not defined for x64 processors.
    //_M_IA64 Defined for Itanium Processor Family 64-bit processors.
    if ("x64".equals(platform) || line.contains("/D WIN64")) {
      // Defined for compilations that target x64 processors.
      addMacro("_WIN32", fileElement);
      // This is not defined for x86 processors.
      addMacro("_WIN64", fileElement);
      addMacro("_M_X64=100", fileElement);
      addMacro("_M_IA64", fileElement);
      addMacro("_M_AMD64", fileElement);
    } else if ("Win32".equals(platform)) {
      // Defined for compilations that target x86 processors.
      addMacro("_WIN32", fileElement);
      //This is not defined for x64 processors.
      addMacro("_M_IX86=600", fileElement);
    } else {
      // do nothing
    }
    // VC++ 17.0, 18.0, 19.0
    // _CPPUNWIND Defined for code compiled by using one of the /EH (Exception Handling Model) flags.
    if (line.contains("/EHs ")
      || line.contains("/EHa ")
      || line.contains("/EHsc ")
      || line.contains("/EHac ")) {
      addMacro("_CPPUNWIND", fileElement);
    }
    if (line.contains("/favor:ATOM") && (existMacro(MSC_X64_100, fileElement)
      || existMacro(MSC_IX86_600, fileElement))) {
      addMacro("__ATOM__=1", fileElement);
    }
    if (line.contains("/arch:AVX") && (existMacro(MSC_X64_100, fileElement)
      || existMacro(MSC_IX86_600, fileElement))) {
      addMacro("__AVX__=1", fileElement);
    }
    if (line.contains("/arch:AVX2") && (existMacro(MSC_X64_100, fileElement)
      || existMacro(MSC_IX86_600, fileElement))) {
      addMacro("__AVX2__=1", fileElement);
    }
  }

  private void parseV100CompilerOptions(String line, String fileElement) {
    // VC++ V16.0 - VS2010 (V10.0)
    addMacro(CPPVERSION, fileElement);
    // __cplusplus_winrt Defined when you use the /ZW option to compile. The value of __cplusplus_winrt is 201009.
    if (line.contains("/ZW ")) {
      addMacro(CPPWINRTVERSION, fileElement);
    }
    addMacro("_MSC_VER=1600", fileElement);
    // VS2010 SP1
    addMacro("_MSC_FULL_VER=16004021901", fileElement);
    //_MFC_VER Defines the MFC version. For example, in Visual Studio 2010, _MFC_VER is defined as 0x0C00.
    addMacro("_MFC_VER=0x0A00", fileElement);
    addMacro("_ATL_VER=0x0A00", fileElement);
    // VC++ 16.0
    if (line.contains("/GX ")) {
      addMacro("_CPPUNWIND", fileElement);
    }
  }

  private void parseV110CompilerOptions(String line, String fileElement) {
    // VC++ V17.0 - VS2012 (V11.0)
    addMacro(CPPVERSION, fileElement);
    // __cplusplus_winrt Defined when you use the /ZW option to compile. The value of __cplusplus_winrt is 201009.
    if (line.contains("/ZW ")) {
      addMacro(CPPWINRTVERSION, fileElement);
    }
    addMacro("_MSC_VER=1700", fileElement);
    // VS2012 Update 4
    addMacro("_MSC_FULL_VER=1700610301", fileElement);
    //_MFC_VER Defines the MFC version (see afxver_.h)
    addMacro("_MFC_VER=0x0B00", fileElement);
    addMacro("_ATL_VER=0x0B00", fileElement);
  }

  private void parseV120CompilerOptions(String line, String fileElement) {
    // VC++ V18.0 - VS2013 (V12.0)
    addMacro(CPPVERSION, fileElement);
    // __cplusplus_winrt Defined when you use the /ZW option to compile. The value of __cplusplus_winrt is 201009.
    if (line.contains("/ZW ")) {
      addMacro(CPPWINRTVERSION, fileElement);
    }
    addMacro("_MSC_VER=1800", fileElement);
    // VS2013 Update 4
    addMacro("_MSC_FULL_VER=180031101", fileElement);
    //_MFC_VER Defines the MFC version (see afxver_.h)
    addMacro("_MFC_VER=0x0C00", fileElement);
    addMacro("_ATL_VER=0x0C00", fileElement);
  }

  private void parseV140CompilerOptions(String line, String fileElement) {
    // VC++ V19.0 - VS2015 (V14.0)
    addMacro(CPPVERSION, fileElement);
    // __cplusplus_winrt Defined when you use the /ZW option to compile. The value of __cplusplus_winrt is 201009.
    if (line.contains("/ZW ")) {
      addMacro(CPPWINRTVERSION, fileElement);
    }
    addMacro("_MSC_VER=1900", fileElement);
    // VS2015 Update 3 V19.00.24215.1
    addMacro("_MSC_FULL_VER=190024215", fileElement);
    //_MFC_VER Defines the MFC version (see afxver_.h)
    addMacro("_MFC_VER=0x0E00", fileElement);
    addMacro("_ATL_VER=0x0E00", fileElement);
  }

  private void parseV141CompilerOptions(String line, String fileElement) {
    // VC++ V19.1 - VS2017 (V15.0)
    addMacro(CPPVERSION, fileElement);
    // __cplusplus_winrt Defined when you use the /ZW option to compile. The value of __cplusplus_winrt is 201009.
    if (line.contains("/ZW ")) {
      addMacro(CPPWINRTVERSION, fileElement);
    }
    addMacro("_MSC_VER=1910", fileElement);
    // VS2017 RC
    addMacro("_MSC_FULL_VER=191024629", fileElement);
    //_MFC_VER Defines the MFC version (see afxver_.h)
    addMacro("_MFC_VER=0x0E00", fileElement);
    addMacro("_ATL_VER=0x0E00", fileElement);
  }
}
