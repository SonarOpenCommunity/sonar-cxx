// header.h

#ifndef HEADER
#define HEADER

template<typename T>
void f();

#define HEADER_IMPL // The macro is set before including header.hpp
#include "header.hpp"
#undef HEADER_IMPL

#endif
