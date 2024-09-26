void foo() {
   // variable definition
   auto v(x);
   auto v{x};
   ClassTemplate v(x);
   ClassTemplate v{x};

   // new expression
   new auto(x);
   new auto{x};
   new ClassTemplate(x);
   new ClassTemplate{x};
}

// function-style cast
template<typename T, typename U>
T cast1(U const &u) {
  return auto(u);
}
template<typename T, typename U>
T cast2(U const &u) {
  return auto{u};
}
template<typename T, typename U>
T cast3(U const &u) {
  return ClassTemplate(u);
}
template<typename T, typename U>
T cast4(U const &u) {
  return ClassTemplate{u};
}
