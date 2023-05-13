/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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
package org.sonar.cxx.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * MsBuild
 */
public class MsBuild {

  /**
   * the following settings are in use by the feature to read configuration settings from the VC compiler report
   */
  public static final String REPORT_PATH_KEY = "sonar.cxx.msbuild.reportPaths";
  public static final String REPORT_ENCODING_DEF = "sonar.cxx.msbuild.encoding";
  public static final String DEFAULT_ENCODING_DEF = StandardCharsets.UTF_8.name();

  private static final Logger LOG = Loggers.get(MsBuild.class);

  private static final String MSC_IX86_600 = "_M_IX86 600";
  private static final String MSC_X64_100 = "_M_X64 100";

  private static final Pattern[] INCLUDE_PATTERNS = {Pattern.compile("/I\"(.*?)\""),
                                                     Pattern.compile("/I([^\\s\"]++) ")};
  private static final Pattern[] DEFINE_PATTERNS = {Pattern.compile("[/-]D\\s([^\\s]++)"),
                                                    Pattern.compile("[/-]D([^\\s]++)")};
  private static final Pattern PATH_TO_CL_PATTERN = Pattern.compile(
    "^(?>[^\\\\]{0,260}\\\\)+bin\\\\(?>[^\\\\]{1,260}\\\\)*CL.exe\\x20.*$");
  private static final Pattern PLATFORM_X86_PATTERN = Pattern.compile("Building solution configuration \".*\\|x64\".");
  private static final Pattern TOOLSET_V141_PATTERN = Pattern.compile(
    "^(?>[^\\\\]{0,260}\\\\)+VC\\\\Tools\\\\MSVC\\\\14\\.1\\d\\.\\d{1,6}"
      + "\\\\bin\\\\HostX(86|64)\\\\x(86|64)\\\\CL.exe.*$");
  private static final Pattern TOOLSET_V142_PATTERN = Pattern.compile(
    "^(?>[^\\\\]{0,260}\\\\)+VC\\\\Tools\\\\MSVC\\\\14\\.2\\d\\.\\d{1,6}"
      + "\\\\bin\\\\HostX(86|64)\\\\x(86|64)\\\\CL.exe.*$");

  // It seems that the required line in any language has these elements: "ClCompile" and (*.vcxproj)
  private static final Pattern PATH_TO_VCXPROJ = Pattern.compile(
    "^\\S+\\s\\\"ClCompile\\\".+\\\"((?>[^\\\\]{1,260}\\\\)*[^\\\\]{1,260}\\.vcxproj)\\\".*$");

  private String platformToolset = "V143";
  private String platform = "Win32";

  private final CxxSquidConfiguration squidConfig;

  /**
   * CxxVCppBuildLogParser (ctor)
   *
   * @param squidConfig
   */
  public MsBuild(CxxSquidConfiguration squidConfig) {
    this.squidConfig = squidConfig;
  }

  private static List<String> getMatches(Pattern pattern, String text) {
    var matches = new ArrayList<String>();
    var m = pattern.matcher(text);
    while (m.find()) {
      matches.add(m.group(1));
    }
    return matches;
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
  public void parse(String line, String projectPath, String compilationFile) {
    this.parseVCppCompilerCLLine(line, projectPath, compilationFile);
  }

  /**
   *
   * @param buildLog
   * @param baseDir
   * @param encodingName
   */
  public void parse(File buildLog, String baseDir, String encodingName) {
    LOG.info("Processing MsBuild log '{}', Encoding= '{}'", buildLog.getName(), encodingName);

    var detectedPlatform = false;
    try ( var br = new BufferedReader(new InputStreamReader(java.nio.file.Files.newInputStream(buildLog.toPath()),
                                                        encodingName))) {
      String line;
      LOG.debug("build log parser baseDir='{}'", baseDir);
      var currentProjectPath = Path.of(baseDir);

      while ((line = br.readLine()) != null) {
        if (line.trim().startsWith("INCLUDE=")) { // handle environment includes
          String[] includes = line.split("=")[1].split(";");
          for (var include : includes) {
            squidConfig.add(CxxSquidConfiguration.GLOBAL, CxxSquidConfiguration.INCLUDE_DIRECTORIES, include);
          }
        }

        // get base path of project to make
        // Target "ClCompile" in file
        // "C:\Program Files (x86)\MSBuild\Microsoft.Cpp\v4.0\V120\Microsoft.CppCommon.targets"
        // from project
        // "D:\Development\SonarQube\cxx\sonar-cxx\integration-tests\testdata\googletest_bullseye_vs_project\
        //         PathHandling.Test\PathHandling.Test.vcxproj" (target "_ClCompile" depends on it):
        if (PATH_TO_VCXPROJ.matcher(line).matches()) {
          String pathProject = getMatches(PATH_TO_VCXPROJ, line).get(0);
          currentProjectPath = Path.of(pathProject).getParent();

          if (currentProjectPath == null) {
            currentProjectPath = Path.of(baseDir);
          }

          LOG.debug("build log parser currentProjectPath='{}'", currentProjectPath);
        }
        // 1>Task "Message"
        // 1>  Configuration=Debug
        // 1>Done executing task "Message".
        // 1>Task "Message"
        //1>  Platform=Win32
        String lineTrimmed = line.trim();
        if (lineTrimmed.endsWith("Platform=x64") || PLATFORM_X86_PATTERN.matcher(lineTrimmed).matches()) {
          setPlatform("x64");
          LOG.debug("build log parser platform='{}'", this.platform);
        }
        // match "bin\CL.exe", "bin\amd64\CL.exe", "bin\x86_amd64\CL.exe"
        if (PATH_TO_CL_PATTERN.matcher(line).matches()) {
          detectedPlatform = setPlatformToolsetFromLine(line);
          String[] allElems = line.split("\\s++");
          String data = allElems[allElems.length - 1];
          parseCLParameters(line, currentProjectPath, data);
          LOG.debug("build log parser cl.exe line='{}'", line);
        }
      }
    } catch (IOException e) {
      LOG.error("Cannot parse build log: " + e.getMessage());
    }
    if (detectedPlatform) {
      LOG.info("Detected VS platform toolset: {}.{}", platformToolset.substring(0, 3), platformToolset.substring(3));
    } else {
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
                 || TOOLSET_V141_PATTERN.matcher(line).matches()) {
      setPlatformToolset("V141");
      return true;
    } else if (line.contains("\\V142\\Microsoft.CppBuild.targets")
                 || TOOLSET_V142_PATTERN.matcher(line).matches()) {
      setPlatformToolset("V142");
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
      parseVCppCompilerCLLine(line, currentProjectPath.toAbsolutePath().toString(), fileElement);
    } catch (InvalidPathException e) {
      LOG.warn("Cannot extract information from current element: {} - {}", data, e.getMessage());
    } catch (NullPointerException e) {
      LOG.error("Bug in parser, please report: '{}' - '{}'", data + " @ " + currentProjectPath, e);
    }
  }

  private void parseVCppCompilerCLLine(String line, String projectPath, String fileElement) {
    for (var includePattern : INCLUDE_PATTERNS) {
      for (var includeElem : getMatches(includePattern, line)) {
        parseInclude(includeElem, projectPath, fileElement);
      }
    }

    for (var definePattern : DEFINE_PATTERNS) {
      for (var macroElem : getMatches(definePattern, line)) {
        addMacro(macroElem, fileElement);
      }
    }

    // https://msdn.microsoft.com/en-us/library/vstudio/b0084kay(v=vs.100).aspx
    // https://msdn.microsoft.com/en-us/library/vstudio/b0084kay(v=vs.110).aspx
    // https://msdn.microsoft.com/en-us/library/vstudio/b0084kay(v=vs.120).aspx
    // https://msdn.microsoft.com/en-us/library/vstudio/b0084kay(v=vs.140).aspx
    parseCommonCompilerOptions(line, fileElement);

    switch (platformToolset) {
      case "V100":
        parseV100CompilerOptions(line, fileElement);
        break;
      case "V110":
        parseV110CompilerOptions(line, fileElement);
        break;
      case "V120":
        parseV120CompilerOptions(line, fileElement);
        break;
      case "V140":
        parseV140CompilerOptions(line, fileElement);
        break;
      case "V141":
        parseV141CompilerOptions(line, fileElement);
        break;
      case "V142":
        parseV142CompilerOptions(line, fileElement);
        break;
      case "V143":
        parseV143CompilerOptions(line, fileElement);
        break;
      default:
      // do nothing
    }
  }

  private void parseInclude(String element, String project, String fileElement) {
    try {
      var includeRoot = new File(element.replace("\"", ""));
      var p = Path.of(project);
      if (!includeRoot.isAbsolute()) {
        // handle path without drive information but represent absolute path
        var pseudoAbsolute = new File(p.getRoot().toString(), includeRoot.toString());
        if (pseudoAbsolute.exists()) {
          includeRoot = new File(p.getRoot().toString(), includeRoot.getPath());
        } else {
          includeRoot = new File(project, includeRoot.getPath());
        }
      }
      squidConfig.add(fileElement, CxxSquidConfiguration.INCLUDE_DIRECTORIES, includeRoot.getCanonicalPath());
    } catch (IOException e) {
      LOG.error("Cannot parse include path using element '{}' : '{}'", element, e.getMessage());
    }
  }

  private void addMacro(String macroElem, String file) {
    String macro = macroElem.replace('=', ' ');
    squidConfig.add(file, CxxSquidConfiguration.DEFINES, macro);
  }

  private boolean existMacro(String macroElem, String file) {
    String macro = macroElem.replace('=', ' ');
    List<String> values = squidConfig.getValues(file, CxxSquidConfiguration.DEFINES);
    return values.contains(macro);
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
    //    /G5 _M_IX86 = 500 (Default. Future compilers will emit a different value to reflect the
    //                         dominant processor.) Pentium
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
      // In the range 30-39 if no /arch ARM option was specified, indicating the default architecture
      //   for ARM was used (VFPv3).
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

    //_MSC_EXTENSIONS This macro is defined when you compile with the /Ze compiler option (the default).
    //Its value, when defined, is 1.
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
    // Visual Studio 2010 SP1 [10.0]
    addMacro("__cplusplus=199711L", fileElement);
    // __cplusplus_winrt Defined when you use the /ZW option to compile. The value of __cplusplus_winrt is 201009.
    if (line.contains("/ZW ")) {
      addMacro("__cplusplus_winrt=201009", fileElement);
    }
    addMacro("_MSC_VER=1600", fileElement);
    addMacro("_MSC_FULL_VER=160040219", fileElement);
    addMacro("_MFC_VER=0x0A00", fileElement);
    addMacro("_ATL_VER=0x0A00", fileElement);
    if (line.contains("/GX ")) {
      addMacro("_CPPUNWIND", fileElement);
    }
  }

  private void parseV110CompilerOptions(String line, String fileElement) {
    // Visual Studio 2012 Update 4 [11.0]
    addMacro("__cplusplus=199711L", fileElement);
    // __cplusplus_winrt Defined when you use the /ZW option to compile. The value of __cplusplus_winrt is 201009.
    if (line.contains("/ZW ")) {
      addMacro("__cplusplus_winrt=201009", fileElement);
    }
    addMacro("_MSC_VER=1700", fileElement);
    addMacro("_MSC_FULL_VER=170061030", fileElement);
    addMacro("_MFC_VER=0x0B00", fileElement);
    addMacro("_ATL_VER=0x0B00", fileElement);
  }

  private void parseV120CompilerOptions(String line, String fileElement) {
    // Visual Studio 2013 Update 5 [12.0]
    addMacro("__cplusplus=199711L", fileElement);
    // __cplusplus_winrt Defined when you use the /ZW option to compile. The value of __cplusplus_winrt is 201009.
    if (line.contains("/ZW ")) {
      addMacro("__cplusplus_winrt=201009", fileElement);
    }
    addMacro("_MSC_VER=1800", fileElement);
    addMacro("_MSC_FULL_VER=180040629", fileElement);
    addMacro("_MFC_VER=0x0C00", fileElement);
    addMacro("_ATL_VER=0x0C00", fileElement);
  }

  private void parseV140CompilerOptions(String line, String fileElement) {
    // Visual Studio 2015 Update 3 [14.0]
    addMacro("__cplusplus=199711L", fileElement);
    // __cplusplus_winrt Defined when you use the /ZW option to compile. The value of __cplusplus_winrt is 201009.
    if (line.contains("/ZW ")) {
      addMacro("__cplusplus_winrt=201009", fileElement);
    }
    addMacro("_MSC_VER=1900", fileElement);
    addMacro("_MSC_FULL_VER=190024210", fileElement);
    addMacro("_MFC_VER=0x0E00", fileElement);
    addMacro("_ATL_VER=0x0E00", fileElement);
  }

  private void parseV141CompilerOptions(String line, String fileElement) {
    // Visual Studio 2017 version 15.9.11
    addMacro("__cplusplus=199711L", fileElement);
    // __cplusplus_winrt Defined when you use the /ZW option to compile. The value of __cplusplus_winrt is 201009.
    if (line.contains("/ZW ")) {
      addMacro("__cplusplus_winrt=201009", fileElement);
    }
    addMacro("_MSC_VER=1910", fileElement);
    addMacro("_MSC_FULL_VER=191627030", fileElement);
    addMacro("_MFC_VER=0x0E00", fileElement);
    addMacro("_ATL_VER=0x0E00", fileElement);
  }

  private void parseV142CompilerOptions(String line, String fileElement) {
    // Visual Studio 2019 version 16.9.2
    addMacro("__cplusplus=201402L", fileElement);
    // __cplusplus_winrt Defined when you use the /ZW option to compile. The value of __cplusplus_winrt is 201009.
    if (line.contains("/ZW ")) {
      addMacro("__cplusplus_winrt=201009", fileElement);
    }
    addMacro("_MSC_VER=1920", fileElement);
    addMacro("_MSC_FULL_VER=192829913", fileElement);
    addMacro("_MFC_VER=0x0E00", fileElement);
    addMacro("_ATL_VER=0x0E00", fileElement);
  }

  void parseV143CompilerOptions(String line, String fileElement) {
    // Visual Studio 2022 RTW (17.5)
    addMacro("__cplusplus=201402L", fileElement); // C++14
    // __cplusplus_winrt Defined when you use the /ZW option to compile. The value of __cplusplus_winrt is 201009.
    if (line.contains("/ZW ")) {
      addMacro("__cplusplus_winrt=201009", fileElement);
    }
    addMacro("_MSC_VER=1935", fileElement);
    addMacro("_MSC_FULL_VER=193532215", fileElement);
    addMacro("_MFC_VER=0x0E00", fileElement);
    addMacro("_ATL_VER=0x0E00", fileElement);
  }

}
