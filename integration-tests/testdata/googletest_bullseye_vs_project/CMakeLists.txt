cmake_minimum_required (VERSION 3.0)
project (PathHandling)
if(WIN32)
set(CMAKE_CXX_FLAGS "-Zi /Zc:rvalueCast")
add_definitions(-D_CRT_SECURE_NO_WARNINGS -D_SCL_SECURE_NO_WARNINGS -D_CRT_SECURE_NO_DEPRECATE -DNT -DNO_WARN_MBCS_MFC_DEPRECATION)

enable_testing()
set(CMAKE_CXX_FLAGS_DEBUG "  /W4 /WX /Od /Oy- /Gm- /EHsc /RTC1 /MD /GS /Zc:wchar_t /Zc:forScope /GR /Gd /TP ")
add_compile_options(/wd4244 /wd4706 /wd4100 /wd4610 /wd4510 /wd4512 /wd4481 /wd4480 /wd4127 /wd4482 -wd4251 -wd34063 -wd34189 -wd34206 -wd34327 -wd34701)
if(CMAKE_GENERATOR_TOOLSET MATCHES "LLVM")
    add_compile_options(-Qunused-arguments -Wno-unknown-pragmas)
    add_compile_options(-Qunused-arguments -Wno-msvc-include -Wno-deprecated-declarations) # gtest
    add_definitions(-DDEVELOPER_VERSION -D_HAS_EXCEPTIONS=0 -DMSVC=1800)
endif()

set(BuildToolsDir C:/Tekla/BuildTools)
endif(WIN32)

if(UNIX)
set(CMAKE_CXX_FLAGS "-std=c++11")
endif(UNIX)


set(NuGetPackagesDir ${CMAKE_BINARY_DIR}/packages)

function(GetLibraryFiles LibraryName)
    file(GLOB headers *.hpp *.h *.inl)
    file(GLOB interface interface/*.hpp interface/*.h)
    file(GLOB sources *.c *.cpp)
    source_group(Sources FILES ${sources})
    source_group(Headers FILES ${headers})
    source_group(Interface FILES ${interface})
    add_library(${LibraryName} ${sources} ${headers} ${interface})
endfunction(GetLibraryFiles)
add_subdirectory(PathHandling)
file(COPY Nuget.config DESTINATION ${CMAKE_BINARY_DIR})
add_subdirectory(PathHandling.Test)

