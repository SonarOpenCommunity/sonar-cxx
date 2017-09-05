#ifdef __has_include                           // Check if __has_include is present
#  if __has_include("i1/has_include.h")        // Check for a standard library
#    include "i1/has_include1"
#  elif __has_include("i2/has_include.h")      // Check for an experimental version
#    include "i2/has_include"
#  elif __has_include("i3/has_include.h")      // Try with an external library
#    include "i3/has_include.h"
#  else                                        // Not found at all
#     error "Missing has_include.h"
#  endif
#endif

void main()
{
   int iii = 0 SEMICOLON // syntax error if include is not working
}
