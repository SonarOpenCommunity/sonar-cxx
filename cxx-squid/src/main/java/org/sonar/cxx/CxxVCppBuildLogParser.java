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
package org.sonar.cxx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.LoggerFactory;

public class CxxVCppBuildLogParser {

  private enum VSVersion { V100, V110, V120, V140 };

  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger("CxxVCppBuildLogParser");

  private final HashMap<String, Set<String>> uniqueIncludes;
  private final HashMap<String, Set<String>> uniqueDefines;

  private VSVersion platformToolset = VSVersion.V100;
  private String platform = "Win32";

  public CxxVCppBuildLogParser(HashMap<String, Set<String>> uniqueIncludesIn,
          HashMap<String, Set<String>> uniqueDefinesIn) {
    uniqueIncludes = uniqueIncludesIn;
    uniqueDefines = uniqueDefinesIn;
  }

  public void parseVCppLog(File buildLog, String baseDir, String charsetName) {

      try {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(buildLog), charsetName));
        String line;
        LOG.debug("build log parser baseDir='{}'", baseDir);
        Path currentProjectPath = Paths.get(baseDir);

        Set<String> overallIncludes = uniqueIncludes.get(CxxConfiguration.OverallIncludeKey);

        while ((line = br.readLine()) != null) {
          if (line.startsWith("  INCLUDE=")) { // handle environment includes 
            String[] includes = line.split("=")[1].split(";");
            for(String include : includes) {
              if (!overallIncludes.contains(include)) {
                overallIncludes.add(include);
              }
            }
          }

          // get base path of project to make 
          // Target "ClCompile" in file "C:\Program Files (x86)\MSBuild\Microsoft.Cpp\v4.0\V120\Microsoft.CppCommon.targets" from project "D:\Development\SonarQube\cxx\sonar-cxx\integration-tests\testdata\googletest_bullseye_vs_project\PathHandling.Test\PathHandling.Test.vcxproj" (target "_ClCompile" depends on it):
          if (line.startsWith("Target \"ClCompile\" in file")) {
            currentProjectPath = Paths.get(line.split("\" from project \"")[1].split("\\s+")[0].replace("\"", "")).getParent();
            if (currentProjectPath == null) {
                currentProjectPath = Paths.get(baseDir);
              }
            }

          if(line.contains("\\V100\\Microsoft.CppBuild.targets")) {
            platformToolset = VSVersion.V100;
          } else if(line.contains("\\V110\\Microsoft.CppBuild.targets")) {
            platformToolset = VSVersion.V110;
          } else if(line.contains("\\V120\\Microsoft.CppBuild.targets")) {
            platformToolset = VSVersion.V120;
          } else if(line.contains("\\V140\\Microsoft.CppBuild.targets")) {
            platformToolset = VSVersion.V140;
          }

          // 1>Task "Message"
          // 1>  Configuration=Debug
          // 1>Done executing task "Message".
          // 1>Task "Message"
          //1>  Platform=Win32         
          if(line.trim().endsWith("Platform=x64")) {
            platform = "x64";
          }          

          if (line.contains("\\bin\\CL.exe")) {
            String[] allElems = line.split("\\s+");
            String data = allElems[allElems.length - 1];
            try {
              String fileElement = Paths.get(currentProjectPath.toAbsolutePath().toString(), data).toAbsolutePath().toString();

              if (!uniqueDefines.containsKey(fileElement)) {
                uniqueDefines.put(fileElement, new HashSet<String>());
              }

              if (!uniqueIncludes.containsKey(fileElement)) {
                uniqueIncludes.put(fileElement, new HashSet<String>());
              }

              parseVCppCompilerCLLine(line, currentProjectPath.toAbsolutePath().toString(), fileElement);             
            } catch (InvalidPathException ex) {
              LOG.warn("Cannot extract information from current element: " + data + " : " + ex.getMessage());
            } catch (NullPointerException ex) {
                LOG.error("Bug in parser, please report: '{}' - '{}'", ex.getMessage(), data + " @ " + currentProjectPath);
                LOG.error("StackTrace: '{}'", ex.getStackTrace());
            }

          }
        }
        br.close();
      } catch (IOException ex) {
        LOG.error("Cannot parse build log", ex);
      }
  }

  private void parseVCppCompilerCLLine(String line, String projectPath, String fileElement) {

    for (String includeElem : getMatches(Pattern.compile("/I\"(.*?)\""), line)) {
      ParseInclude(includeElem, projectPath, fileElement);
    }

    for (String includeElem : getMatches(Pattern.compile("/I([^\\s\"]+) "),
        line)) {
      ParseInclude(includeElem, projectPath, fileElement);
    }

    for (String macroElem : getMatches(Pattern.compile("[/-]D\\s([^\\s]+)"),
        line)) {
      AddMacro(macroElem, fileElement);
    }
    
    for (String macroElem : getMatches(Pattern.compile("[/-]D([^\\s]+)"),
        line)) {
      AddMacro(macroElem, fileElement);
    }
    
    // https://msdn.microsoft.com/en-us/library/vstudio/b0084kay(v=vs.100).aspx
    // https://msdn.microsoft.com/en-us/library/vstudio/b0084kay(v=vs.110).aspx
    // https://msdn.microsoft.com/en-us/library/vstudio/b0084kay(v=vs.120).aspx 
    // https://msdn.microsoft.com/en-us/library/vstudio/b0084kay(v=vs.140).aspx
    ParseCommonCompilerOptions(line, fileElement);
    
    if (platformToolset.equals(VSVersion.V100)) {
      ParseV100CompilerOptions(line, fileElement);
    } else if (platformToolset.equals(VSVersion.V110)) {
      ParseV110CompilerOptions(line, fileElement);
    } else if (platformToolset.equals(VSVersion.V120)) {
      ParseV120CompilerOptions(line, fileElement);
    } else if (platformToolset.equals(VSVersion.V140)) {
      ParseV140CompilerOptions(line, fileElement);
    }    
  }

  private List<String> getMatches(Pattern pattern, String text) {
    List<String> matches = new ArrayList<>();
    Matcher m = pattern.matcher(text);
    while (m.find()) {
      matches.add(m.group(1));
    }
    return matches;
  }

  private void ParseInclude(String element, String project, String fileElement) {
    
    Set<String> includesPerUnit = uniqueIncludes.get(fileElement);
    
    try {
      File includeRoot = new File(element.replace("\"", ""));
      String includePath;
      if (!includeRoot.isAbsolute()) {
        includeRoot = new File(project, includeRoot.getPath());
        includePath = includeRoot.getCanonicalPath();
      } else {
        includePath = includeRoot.getCanonicalPath();
      }
      if (!includesPerUnit.contains(includePath)) {
        includesPerUnit.add(includePath);
      }
    } catch (java.io.IOException io) {
      LOG.error("Cannot parse include path using element '{}' : '{}'", element,
          io.getMessage());
    }
  }

  private void AddMacro(String macroElem, String file) {
    
    Set<String> definesPerUnit = uniqueDefines.get(file);
    
    String macro = macroElem.replace("=", " ");
    if (!definesPerUnit.contains(macro)) {
      definesPerUnit.add(macro);
    }
  }

  private void ParseCommonCompilerOptions(String line, String fileElement) {    
    // Always Defined //
    //_INTEGRAL_MAX_BITS Reports the maximum size (in bits) for an integral type.    
    AddMacro("_INTEGRAL_MAX_BITS=32", fileElement);    
    //_MFC_VER Defines the MFC version. For example, in Visual Studio 2010, _MFC_VER is defined as 0x0A00.
    AddMacro("_MFC_VER=1", fileElement);            
    //_MSC_BUILD Evaluates to the revision number component of the compiler's version number. The revision number is the fourth component of the period-delimited version number. For example, if the version number of the Visual C++ compiler is 15.00.20706.01, the _MSC_BUILD macro evaluates to 1.
    AddMacro("_MSC_BUILD=1", fileElement);    
    //_MSC_FULL_VER Evaluates to the major, minor, and build number components of the compiler's version number. The major number is the first component of the period-delimited version number, the minor number is the second component, and the build number is the third component. For example, if the version number of the Visual C++ compiler is 15.00.20706.01, the _MSC_FULL_VER macro evaluates to 150020706. Type cl /? at the command line to view the compiler's version number.
    AddMacro("_MSC_FULL_VER=150020706", fileElement);    
    //_MSC_VER Evaluates to the major and minor number components of the compiler's version number. The major number is the first component of the period-delimited version number and the minor number is the second component.
    AddMacro("_MSC_VER=1700", fileElement); 
    //__COUNTER__ Expands to an integer starting with 0 and incrementing by 1 every time it is used in a source file or included headers of the source file. __COUNTER__ remembers its state when you use precompiled headers.
    AddMacro("__COUNTER__=0", fileElement);     
    //__DATE__ The compilation date of the current source file. The date is a string literal of the form Mmm dd yyyy. The month name Mmm is the same as for dates generated by the library function asctime declared in TIME.H.
    AddMacro("__DATE__=\"??? ?? ????\"", fileElement);
    //__FILE__ The name of the current source file. __FILE__ expands to a string surrounded by double quotation marks. To ensure that the full path to the file is displayed, use /FC (Full Path of Source Code File in Diagnostics).
    AddMacro("__FILE__=\"file\"", fileElement);
    //__LINE__ The line number in the current source file. The line number is a decimal integer constant. It can be changed with a #line directive.
    AddMacro("__LINE__=1", fileElement);
    //__TIME__ The most recent compilation time of the current source file. The time is a string literal of the form hh:mm:ss.
    AddMacro("__TIME__=\"??:??:??\"", fileElement);
    //__TIMESTAMP__ The date and time of the last modification of the current source file, expressed as a string literal in the form Ddd Mmm Date hh:mm:ss yyyy, where Ddd is the abbreviated day of the week and Date is an integer from 1 to 31.
    AddMacro("__TIMESTAMP__=\"??? ?? ???? ??:??:??\"", fileElement);    
    //_ATL_VER Defines the ATL version. In Visual Studio 2010, _ATL_VER is defined as 0x0A00.
    AddMacro("_ATL_VER=1", fileElement);

    //__STDC__ Indicates full conformance with the ANSI C standard. Defined as the integer constant 1 only if the /Za compiler option is given and you are not compiling C++ code; otherwise is undefined.
    if (line.contains("/Za ")) {
      AddMacro("__STDC__=1", fileElement);
    }
    
    //_CHAR_UNSIGNED Default char type is unsigned. Defined when /J is specified.
    if (line.contains("/J ")) {
      AddMacro("_CHAR_UNSIGNED", fileElement);    
    }
    
    //_CPPRTTI Defined for code compiled with /GR (Enable Run-Time Type Information).
    if (line.contains("/GR ")) {
      AddMacro("_CPPRTTI", fileElement); 
    } 
    
    //_MANAGED Defined to be 1 when /clr is specified.
    if (line.contains("/clr ")) {
      AddMacro("_MANAGED", fileElement); 
    }      
    //_M_CEE_PURE Defined for a compilation that uses /clr:pure.
    if (line.contains("/clr:pure ")) {
      AddMacro("_M_CEE_PURE", fileElement);    
    }    
    //_M_CEE_SAFE Defined for a compilation that uses /clr:safe.
    if (line.contains("/clr:safe ")) {
      AddMacro("_M_CEE_SAFE", fileElement);    
    }        
    //__CLR_VER Defines the version of the common language runtime used when the application was compiled. The value returned will be in the following format:    
    //__cplusplus_cli Defined when you compile with /clr, /clr:pure, or /clr:safe. Value of __cplusplus_cli is 200406. __cplusplus_cli is in effect throughout the translation unit.    
    //_M_CEE Defined for a compilation that uses any form of /clr (/clr:oldSyntax, /clr:safe, for example).    
    if (line.contains("/clr")) {      
      
      AddMacro("_M_CEE", fileElement);      
      if (line.contains("/clr ") ||
              line.contains("/clr:pure ") ||
              line.contains("/clr:safe ")) {
        AddMacro("__CLR_VER", fileElement);     
        AddMacro("__cplusplus_cli", fileElement);        
      }      
    } 
                   
    //_MSC_EXTENSIONS This macro is defined when you compile with the /Ze compiler option (the default). Its value, when defined, is 1.
    if (line.contains("/Ze ")) {
      AddMacro("_MSC_EXTENSIONS", fileElement);    
    }
         
    //__MSVC_RUNTIME_CHECKS Defined when one of the /RTC compiler options is specified.
    if (line.contains("/RTC ")) {
      AddMacro("__MSVC_RUNTIME_CHECKS", fileElement);    
    }
    
    //_DEBUG Defined when you compile with /LDd, /MDd, and /MTd.
    if (line.contains("/LDd ") ||
            line.contains("/MDd ") ||
            line.contains("/MTd ")) {
      AddMacro("_DEBUG", fileElement);    
    }     
    //_DLL Defined when /MD or /MDd (Multithreaded DLL) is specified. 
    if (line.contains("/MD ") ||
            line.contains("/MDd ")) {
      AddMacro("_DLL", fileElement);    
    }    
    //_MT Defined when /MD or /MDd (Multithreaded DLL) or /MT or /MTd (Multithreaded) is specified.
    if (line.contains("/MD ") ||
            line.contains("/MDd ") ||
            line.contains("/MT ") ||
            line.contains("/MTd ")) {
      AddMacro("_MT", fileElement);    
    }    
    
    //_OPENMP Defined when compiling with /openmp, returns an integer representing the date of the OpenMP specification implemented by Visual C++.
    if (line.contains("/openmp ")) {
      AddMacro("_OPENMP", fileElement);    
    }
    
    //_VC_NODEFAULTLIB Defined when /Zl is used; see /Zl (Omit Default Library Name) for more information.
    if (line.contains("/Zl ")) {
      AddMacro("_VC_NODEFAULTLIB", fileElement);    
    }    

    //_NATIVE_WCHAR_T_DEFINED Defined when /Zc:wchar_t is used.    
    //_WCHAR_T_DEFINED Defined when /Zc:wchar_t is used or if wchar_t is defined in a system header file included in your project.
    if (line.contains("/Zc:wchar_t ")) {
      AddMacro("_WCHAR_T_DEFINED", fileElement);   
      AddMacro("_NATIVE_WCHAR_T_DEFINED", fileElement);
    }      
    
    //_Wp64 Defined when specifying /Wp64.
    if (line.contains("/Wp64 ")) {
      AddMacro("_Wp64", fileElement);    
    }   
    
    //_M_AMD64 Defined for x64 processors.
    //_WIN32 Defined for applications for Win32 and Win64. Always defined.
    //_WIN64 Defined for applications for Win64.
    //_M_X64 Defined for x64 processors.    
    //_M_IX86 Defined for x86 processors. See the Values for _M_IX86 table below for more information. This is not defined for x64 processors.
    //_M_IA64 Defined for Itanium Processor Family 64-bit processors.
    //_M_IX86_FP Expands to a value indicating which /arch compiler option was used:
    //    0 if /arch was not used.    
    //    1 if /arch:SSE was used.
    //    2 if /arch:SSE2 was used.   
    if(platform.equals("x64")) {
      AddMacro("_WIN32", fileElement);
      AddMacro("_WIN64", fileElement);
      AddMacro("_M_X64", fileElement);
      AddMacro("_M_IA64", fileElement);
      AddMacro("_M_AMD64", fileElement);
    }
    
    if(platform.equals("Win32")) {
      AddMacro("_WIN32", fileElement);
      AddMacro("_M_IX86", fileElement);
      AddMacro("_M_IX86_FP", fileElement);
    }        
    
    // Weird Stuff, context sensite //
    //__cplusplus Defined for C++ programs only.
    //__FUNCDNAME__ Valid only in a function. Defines the decorated name of the enclosing function as a string.
    //__FUNCDNAME__ is not expanded if you use the /EP or /P compiler option.
    //__FUNCSIG__ Valid only in a function. Defines the signature of the enclosing function as a string.
    //__FUNCSIG__ is not expanded if you use the /EP or /P compiler option.       
    //__FUNCTION__ Valid only in a function. Defines the undecorated name of the enclosing function as a string. __FUNCTION__ is not expanded if you use the /EP or /P compiler option.
        
    // DEPRECATED STUFF //
    //_M_MPPC Defined for Power Macintosh platforms (no longer supported).
    //_M_MRX000 Defined for MIPS platforms (no longer supported).
    //_M_PPC Defined for PowerPC platforms (no longer supported).
    //_M_ALPHA Defined for DEC ALPHA platforms (no longer supported).
  }       

  private void ParseV100CompilerOptions(String line, String fileElement) {
    // _CPPUNWIND Defined for code compiled with /GX (Enable Exception Handling).
    if (line.contains("/GX ")) {
      AddMacro("_CPPUNWIND", fileElement);    
    } 

    // _M_ALPHA Defined for DEC ALPHA platforms (no longer supported).
    // _M_IA64 Defined for Itanium Processor Family 64-bit processors.
    // _M_MPPC Defined for Power Macintosh platforms (no longer supported).
    // _M_MRX000 Defined for MIPS platforms (no longer supported).
    // _M_PPC Defined for PowerPC platforms (no longer supported).    
    // _M_IX86 
    //    /GB _M_IX86 = 600 Blend
    //    /G5 _M_IX86 = 500 (Default. Future compilers will emit a different value to reflect the dominant processor.) Pentium
    //    /G6 _M_IX86 = 600  Pentium Pro, Pentium II, and Pentium III 
    //    /G3 _M_IX86 = 300  80386
    //    /G4 _M_IX86 = 400  80486
    if (line.contains("/GB ") ||
            line.contains("/G5") || 
            line.contains("/G6") ||
            line.contains("/G3") ||
            line.contains("/G4")) {
      AddMacro("_M_IX86", fileElement);    
    }    
  }

  private void ParseV110CompilerOptions(String line, String fileElement) {
    // _M_ALPHA Defined for DEC ALPHA platforms (no longer supported).
    // _M_IA64 Defined for Itanium Processor Family 64-bit processors.    
    // _M_MPPC Defined for Power Macintosh platforms (no longer supported).
    // _M_MRX000 Defined for MIPS platforms (no longer supported).
    // _M_PPC Defined for PowerPC platforms (no longer supported).
    // __cplusplus_winrt Defined when you use the /ZW option to compile. The value of __cplusplus_winrt is 201009.
    if (line.contains("/ZW ")) {
      AddMacro("__cplusplus_winrt", fileElement);    
    }

    // _CPPUNWIND Defined for code compiled by using one of the /EH (Exception Handling Model) flags.
    if (line.contains("/EHs ") || 
            line.contains("/EHa ") || 
            line.contains("/EHsc ") ||
            line.contains("/EHac ")) {
      AddMacro("_CPPUNWIND", fileElement);    
    } 

    // _M_ARM_FP Expands to a value indicating which /arch compiler option was used:
    //    In the range 30-39 if no /arch ARM option was specified, indicating the default architecture for ARM was used (VFPv3).
    //    In the range 40-49 if /arch:VFPv4 was used.
    //    See /arch (x86) for more information.    
    // _M_IX86 
    //    /GB _M_IX86 = 600 Blend
    //    /G5 _M_IX86 = 500 (Default. Future compilers will emit a different value to reflect the dominant processor.) Pentium
    //    /G6 _M_IX86 = 600  Pentium Pro, Pentium II, and Pentium III 
    //    /G3 _M_IX86 = 300  80386
    //    /G4 _M_IX86 = 400  80486    
    if (line.contains("/GB ") ||
            line.contains("/G5") || 
            line.contains("/G6") ||
            line.contains("/G3") ||
            line.contains("/G4")) {
      AddMacro("_M_IX86", fileElement);    
    }  
  }

  private void ParseV120CompilerOptions(String line, String fileElement) {
    // __AVX__ Defined when /arch:AVX or /arch:AVX2 is specified.
    // __AVX2__ Defined when /arch:AVX2 is specified.
    if (line.contains("/arch:AVX ")) {
      AddMacro("__AVX__", fileElement);    
    }

    if (line.contains("/arch:AVX2 ")) {
      AddMacro("__AVX__", fileElement);
      AddMacro("__AVX2__", fileElement);    
    }
    
    // _M_ARM Defined for compilations that target ARM processors.
    // __cplusplus_winrt Defined when you use the /ZW option to compile. The value of __cplusplus_winrt is 201009.
    if (line.contains("/ZW ")) {
      AddMacro("__cplusplus_winrt", fileElement);    
    }  

    // _CPPUNWIND Defined for code compiled by using one of the /EH (Exception Handling Model) flags.
    if (line.contains("/EHs ") || 
            line.contains("/EHa ") || 
            line.contains("/EHsc ") ||
            line.contains("/EHac ")) {
      AddMacro("_CPPUNWIND", fileElement);    
    } 

    // _M_ARM_FP Expands to a value indicating which /arch compiler option was used:
    //    In the range 30-39 if no /arch ARM option was specified, indicating the default architecture for ARM was used (VFPv3).
    //    In the range 40-49 if /arch:VFPv4 was used.
    //    See /arch (x86) for more information.       
    if (line.contains("/arch:IA32 ") || 
            line.contains("/arch:SSE ") || 
            line.contains("/arch:SSE2 ") ||
            line.contains("/arch:AVX2 ") ||
            line.contains("/arch:AVX ")) {
      AddMacro("_M_ARM_FP", fileElement);    
    }
  }
  
  private void ParseV140CompilerOptions(String line, String fileElement) {
    // TBD when vs 2015 release and documentation updated
  }  
}
