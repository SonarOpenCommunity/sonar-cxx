########################################################################################
########################################################################################
# CMAKE PROJECT
########################################################################################
########################################################################################
PROJECT (SAMPLE-PROJECT-TESTS)

########################################################################################
# C++ INCLUDE DIRECTORIES

# Add a new include path in this list !
INCLUDE_DIRECTORIES (
	${PROJECT_SOURCE_DIR}
        ${PROJECT_SOURCE_DIR}/..
	${PROJECT_BINARY_DIR} # for uic included generated files
)

########################################################################################
# INTERNAL SOURCE LOCATION (source that are not visible from outside)

# Set the base directory of your source files
SET(SAMPLE_PROJECT_TESTS_MAIN_DIR_SRCS "." )


# Add your source files here !
SET(SAMPLE_PROJECT_TESTS_MAIN_SRCS
        "${SAMPLE_PROJECT_TESTS_MAIN_DIR_SRCS}/main.cpp"
        "${SAMPLE_PROJECT_TESTS_MAIN_DIR_SRCS}/SAMPLE-test.cpp"
)

# Add your header files here !
SET(SAMPLE_PROJECT_TESTS_MAIN_HDRS
)

# Add your header files here !
SET(SAMPLE_PROJECT_TESTS_MAIN_QT_HDRS
        "${SAMPLE_PROJECT_TESTS_MAIN_DIR_SRCS}/SAMPLE-test.h"
)

# Add your source files here !
SET(SAMPLE_PROJECT_TESTS_UI_SRCS
)

# Add your header files here !
SET(SAMPLE_PROJECT_TESTS_UI_HDRS
)

# Add your resource files here !
SET(SAMPLE_PROJECT_TESTS_UI_QRC
)

# Add your ui files here !
SET(SAMPLE_PROJECT_TESTS_UI_UI
)

# Add your header files here !
SET(SAMPLE_PROJECT_TESTS_UI_NO_QOBJECT_HDRS
)

########################################################################################
# QT PROJECT MOC/QRC/UI STUFF

FIND_PACKAGE( Qt4 REQUIRED )

SET(QT_USE_QTNETWORK FALSE)
SET(QT_USE_QTOPENGL FALSE)
SET(QT_USE_QTSQL FALSE)
SET(QT_USE_QTXML TRUE)
SET(QT_USE_QTSVG FALSE)
SET(QT_USE_QTTEST TRUE)
SET(QT_USE_QTDBUS FALSE)
SET(QT_USE_QTSCRIPT FALSE)
SET(QT_USE_QTWEBKIT FALSE)
SET(QT_USE_QTXMLPATTERNS FALSE)
SET(QT_USE_PHONON FALSE)

INCLUDE(${QT_USE_FILE})

# Add your QT resource file here !
QT4_ADD_RESOURCES( SAMPLE_PROJECT_TESTS_QRC_SRCS
        ${SAMPLE_PROJECT_TESTS_UI_QRC})

# Add your QT headers file here !
QT4_WRAP_CPP(SAMPLE_PROJECT_TESTS_MOC_SRCS
        ${SAMPLE_PROJECT_TESTS_MAIN_QT_HDRS}
        ${SAMPLE_PROJECT_TESTS_QAPPLICATION_QOBJ_HDRS}
        ${SAMPLE_PROJECT_TESTS_UI_HDRS}
)

# Add your QT Ui file here
QT4_WRAP_UI( SAMPLE_PROJECT_TESTS_UIC_SRCS
        ${SAMPLE_PROJECT_TESTS_UI_UI})

########################################################################################
# BINARY TARGET

# Define the executable name and custom paths (resources & translations)
SET(SAMPLE_PROJECT_TESTS_EXECUTABLE_NAME SAMPLE-PROJECT-TESTS)

# Executable parameters depends on PF
IF(WIN32)
        SET(SAMPLE_PROJECT_TESTS_EXECUTABLE_TYPE WIN32)
ENDIF()

# SAMPLE_PROJECT_TESTS is a program that depends on sources code (headers are mentionned only to appear in IDE)
ADD_EXECUTABLE(${SAMPLE_PROJECT_TESTS_EXECUTABLE_NAME}
        ${SAMPLE_PROJECT_TESTS_EXECUTABLE_TYPE}
        ${SAMPLE_PROJECT_TESTS_MAIN_SRCS}
        ${SAMPLE_PROJECT_TESTS_MAIN_HDRS}
        ${SAMPLE_PROJECT_TESTS_UI_SRCS}
        ${SAMPLE_PROJECT_TESTS_UI_HDRS}
        ${SAMPLE_PROJECT_TESTS_UI_NO_QOBJECT_HDRS}
        ${SAMPLE_PROJECT_TESTS_QRC_SRCS}
        ${SAMPLE_PROJECT_TESTS_MOC_SRCS}
        ${SAMPLE_PROJECT_TESTS_UIC_SRCS}
)

########################################################################################
# LINK WITH OTHER LIBRARIES

# SAMPLE_PROJECT_TESTS depends on QT and ...
TARGET_LINK_LIBRARIES( ${SAMPLE_PROJECT_TESTS_EXECUTABLE_NAME}
        ${QT_LIBRARIES}
)
