// 1. include via a relpath with a backstep
#include "../include/include1.hh"
// 2. include a via filename from the include root
#include "include2.hh"
// 3. the same, but using <>
#include <include3.hh>
// 4. include via a path relative to the include root
#include "subfolder/include4.hh"
// 5. indirect include of ../include_trd/something 
#include "../include_snd/include_snd_1.hh"
INCLUDE1
INCLUDE2
INCLUDE3
INCLUDE4
INCLUDE_SND_SUBFOLDER_1
