include_directories(
interface
../tools/interface
)

get_filename_component(LibraryName ${CMAKE_CURRENT_LIST_DIR} NAME)
GetLibraryFiles(${LibraryName})
