########################################################################################
########################################################################################
# CMAKE PROJECT
########################################################################################
########################################################################################
PROJECT (Utils)

########################################################################################
# C++ INCLUDE DIRECTORIES

# Add a new include path in this list !
INCLUDE_DIRECTORIES (
	${PROJECT_SOURCE_DIR}
)

########################################################################################
# INTERNAL SOURCE LOCATION (source that are not visible from outside)

# Set the base directory of your source files
SET(UTILS_MAIN_DIR_SRCS ".")

# Add your source files here !
SET(UTILS_SRCS
	"${UTILS_MAIN_DIR_SRCS}/utils.cpp"
	"${UTILS_MAIN_DIR_SRCS}/code_chunks.cpp"
        )

# Add your QObject header files here !
SET(UTILS_HDRS)

# Add your QObject header files here !
SET(UTILS_QT_HDRS)


########################################################################################
# QT PROJECT MOC/QRC/UI STUFF

FIND_PACKAGE( Qt4 REQUIRED )

SET(QT_USE_QTNETWORK FALSE)
SET(QT_USE_QTOPENGL FALSE)
SET(QT_USE_QTSQL FALSE)
SET(QT_USE_QTXML FALSE)
SET(QT_USE_QTSVG FALSE)
SET(QT_USE_QTTEST FALSE)
SET(QT_USE_QTDBUS FALSE)
SET(QT_USE_QTSCRIPT FALSE)
SET(QT_USE_QTWEBKIT FALSE)
SET(QT_USE_QTXMLPATTERNS FALSE)
SET(QT_USE_PHONON FALSE)

INCLUDE(${QT_USE_FILE})

# Add your QT headers file here !
QT4_WRAP_CPP(UTILS_MOC_SRCS ${UTILS_QT_HDRS})


########################################################################################
# BINARY TARGET

# Create your library target here !
# utils is a static library that depends on sources code (headers are mentionned only ta appears in vcproj)
ADD_LIBRARY(Utils STATIC
	${UTILS_SRCS}
        ${UTILS_HDRS}
        ${UTILS_MOC_SRCS}
        ${UTILS_QT_HDRS})

