########################################################################################
########################################################################################
# CMAKE SAMPLE ROOT
# 
# This CMAKE file provides the WHOLE configuration of the SAMPLE project
# see http://www.cmake.org/cmake/help/cmake-2-8-docs.html
# see http://www.vtk.org/Wiki/CMake_Useful_Variables
########################################################################################
########################################################################################

########################################################################################
# CMAKE MANAGEMENT - DO NOT MODIFY

# Check the CMAKE version on the developer PC
CMAKE_MINIMUM_REQUIRED(VERSION 2.6)

# Don't build anything unless the version of CMake is high enough.
# The matching ELSE/ENDIF should be the last lines in the file.
IF("${CMAKE_MAJOR_VERSION}.${CMAKE_MINOR_VERSION}" GREATER 2.4)

# Allow IF/ELSE/ELSEIF/ENDIF with empty values (superfluous in 2.6.0) 
#SET(CMAKE_ALLOW_LOOSE_LOOP_CONSTRUCTS true)

# remove cmakelists.txt files from generated solutions
SET(CMAKE_SUPPRESS_REGENERATION  true)

########################################################################################
# PROJECT NAME

# Sets the name of the project.
# This creates the variables projectname_BINARY_DIR and projectname_SOURCE_DIR.
PROJECT (SAMPLE-PROJECT)

IF(NOT PROJECT_TARGET)
SET(PROJECT_TARGET "SAMPLE_GENERIQUE")
ENDIf(NOT PROJECT_TARGET)

IF(NOT EXECUTABLE_NAME)
SET(EXECUTABLE_NAME "SAMPLE")
ENDIf(NOT EXECUTABLE_NAME)

IF(NOT PROJECT_LANGUAGE)
SET(PROJECT_LANGUAGE "en")
ENDIf(NOT PROJECT_LANGUAGE)

MESSAGE("Building ${CMAKE_GENERATOR} for ${PROJECT_TARGET}, executable ${EXECUTABLE_NAME}, language ${PROJECT_LANGUAGE}")
MESSAGE("CXX_FLAGS='${CMAKE_CXX_FLAGS}', C_FLAGS='${CMAKE_C_FLAGS}'")
IF(${CMAKE_GENERATOR} MATCHES "Unix Makefiles" OR ${CMAKE_GENERATOR} MATCHES "MinGW Makefiles")
        IF(${CMAKE_BUILD_TYPE} MATCHES "Release")
		MESSAGE("Release Build, CXX_FLAGS_RELEASE='${CMAKE_CXX_FLAGS_RELEASE}', C_FLAGS_RELEASE='${CMAKE_C_FLAGS_RELEASE}'")
                MESSAGE("Makefile build type can change using cmake option -DCMAKE_BUILD_TYPE=Debug,Release,Coverage instead of '${CMAKE_BUILD_TYPE}'")
        ELSEIF(${CMAKE_BUILD_TYPE} MATCHES "Debug")
		MESSAGE("Debug Build, CXX_FLAGS_DEBUG='${CMAKE_CXX_FLAGS_DEBUG}', C_FLAGS_DEBUG='${CMAKE_C_FLAGS_DEBUG}'")
                MESSAGE("Makefile build type can change using cmake option -DCMAKE_BUILD_TYPE=Debug,Release,Coverage instead of '${CMAKE_BUILD_TYPE}'")
        ELSEIF(${CMAKE_BUILD_TYPE} MATCHES "Coverage")
		SET(CMAKE_BUILD_TYPE "Debug")
                SET(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -fprofile-arcs -ftest-coverage")
		SET(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS_DEBUG} -fprofile-arcs -ftest-coverage")
                SET(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -fprofile-arcs")
                SET(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -fprofile-arcs")
                SET(CMAKE_MODULE_LINKER_FLAGS "${CMAKE_MODULE_LINKER_FLAGS} -fprofile-arcs")
		MESSAGE("Coverage Debug Build, CXX_FLAGS_DEBUG='${CMAKE_CXX_FLAGS_DEBUG}', C_FLAGS_DEBUG='${CMAKE_C_FLAGS_DEBUG}'")
                MESSAGE("Makefile build type can change using cmake option -DCMAKE_BUILD_TYPE=Debug,Release,Coverage instead of '${CMAKE_BUILD_TYPE}'")
        ELSEIF(NOT CMAKE_BUILD_TYPE)
		MESSAGE("Concider using cmake option -DCMAKE_BUILD_TYPE=Debug,Release,Coverage instead of '${CMAKE_BUILD_TYPE}'")
        ENDIF()
ELSE()
        MESSAGE("For Debug Build, CXX_FLAGS_DEBUG='${CMAKE_CXX_FLAGS_DEBUG}', C_FLAGS_DEBUG='${CMAKE_C_FLAGS_DEBUG}'")
        MESSAGE("For Release Build, CXX_FLAGS_RELEASE='${CMAKE_CXX_FLAGS_RELEASE}', C_FLAGS_RELEASE='${CMAKE_C_FLAGS_RELEASE}'")
ENDIF()


########################################################################################
# TARGET FLAGS

# Add your own DEFINE here !

# Compute the LANGUAGE define
STRING( TOUPPER BUILD_${PROJECT_LANGUAGE} BUILD_LANGUAGE )

########################################################################################
# OUTPUT PATH

# Create Directories if not exist
FILE(MAKE_DIRECTORY ${CMAKE_BINARY_DIR})

SET(EXECUTABLE_OUTPUT_PATH ${CMAKE_BINARY_DIR}/bin)
SET(LIBRARY_OUTPUT_PATH ${EXECUTABLE_OUTPUT_PATH})

# Create Directories if not exist)
FILE(MAKE_DIRECTORY ${EXECUTABLE_OUTPUT_PATH})
FILE(MAKE_DIRECTORY ${LIBRARY_OUTPUT_PATH})

########################################################################################
# PROJECTS LIST

# Add your new project/module in this list ! 
SUBDIRS(
  # Project : tests
  tests
  
  # Project : Main SAMPLE
  application

  # Project : utils
  utils

  # other sub dir here
)

########################################################################################
# CMAKE MANAGEMENT - DO NOT MODIFY

# If the version of CMake was too old, complain and build nothing.
# These should be the last lines in this file.
ELSE("${CMAKE_MAJOR_VERSION}.${CMAKE_MINOR_VERSION}" GREATER 2.4)
  MESSAGE(FATAL_ERROR
          "This version of CMake is too old to build the software. Please upgrade to CMake 2.6.")
ENDIF()

