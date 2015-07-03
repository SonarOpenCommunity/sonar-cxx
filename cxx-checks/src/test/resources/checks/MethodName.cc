// MethodNameCheck

void fn_name_ok();
void fn_name_ok() {
}

class MyClass {
   void IsOk() {}
   void Badly_Named_Method() {}
   void TooLongMethodNameBecauseItHasMoreThan30Characters() {}
};
