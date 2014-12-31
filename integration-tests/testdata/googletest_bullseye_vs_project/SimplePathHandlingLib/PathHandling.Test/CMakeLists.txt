cmake_minimum_required (VERSION 2.6)
project (BinaryTreeTests)

get_filename_component(LibraryName ${CMAKE_CURRENT_LIST_DIR} NAME)

list(APPEND CMAKE_MODULE_PATH ${CMAKE_SOURCE_DIR}/Modules)
include(gtest)

set(CMAKE_EXE_LINKER_FLAGS_DEBUG "${CMAKE_EXE_LINKER_FLAGS_DEBUG} /NODEFAULTLIB:MSVCRTD MSVCRT.lib")
include_directories(
../tools/interface
../eql/interface
../memlib/interface
)

add_executable(${LibraryName} binarytree_test.cpp main.cpp)

set (Libs tools binarytree memlib ${GTestDir}/sgtestgmock.lib)
target_link_libraries(${LibraryName} ${Libs})
add_dependencies(${LibraryName} packages_gtest)
