cmake_minimum_required (VERSION 2.6)
project (PathHandling.Test)

get_filename_component(LibraryName ${CMAKE_CURRENT_LIST_DIR} NAME)

list(APPEND CMAKE_MODULE_PATH ${CMAKE_SOURCE_DIR}/Modules)
include(gtest)

set(CMAKE_EXE_LINKER_FLAGS_DEBUG "${CMAKE_EXE_LINKER_FLAGS_DEBUG} /NODEFAULTLIB:MSVCRTD MSVCRT.lib")

add_executable(${LibraryName} PathHandlingTest.cpp main.cpp)

set (Libs ${GTestDir}/sgtestgmock.lib)
target_link_libraries(${LibraryName} ${Libs})
add_dependencies(${LibraryName} packages_gtest)
