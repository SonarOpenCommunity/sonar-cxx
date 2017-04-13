// 1. include via a relpath with a backstep
#include "../include/include1.hh"
// 2. include via a filename from the include root
#include "include2.hh"
// 3. the same, but using <>
#include <include3.hh>
// 4. include via a path relative to the include root
#include "subfolder/include4.hh"
// 5. indirect include of ../include_trd/something 
#include "../include_snd/include_snd_1.hh"
// 6. macro replacement doesnt take place in <> and "" form of the include statement
#define HEADER FOO
#include "HEADER1.hh"
#include <HEADER2.hh>
// 7. ... but it does in the 'free' form of the include statement
#define LOCATION "bar.hh"
#include LOCATION
#define MACRO(p) p
#include MACRO("widget.hh")

INCLUDE1
INCLUDE2
INCLUDE3
INCLUDE4
INCLUDE_SND_SUBFOLDER_1
HEADER1
HEADER2
BAR //void bar(){}
WIDGET //void widget(){}