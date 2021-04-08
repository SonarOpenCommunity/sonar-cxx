// header.hpp

#ifdef HEADER_IMPL // This is an ifdef, not an ifndef!

template<typename T>
void f() {
  // do something...
  int i = 0;
}

void g() {
  // do something...
  int i = 0;
}

#endif
