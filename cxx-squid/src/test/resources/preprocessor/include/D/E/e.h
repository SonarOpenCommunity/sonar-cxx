#define INCLUDE "using: include/D/E/e.h"
#include "a.h"

// test: are compilation database settings propagated
#if GLOBAL
#   define PROPAGATED "propagated"
#else
#   define PROPAGATED "not propagated"
#endif
