// MethodNameCheck

void fn_name_ok();
void fn_name_ok() {
}

class MyClass {
   void IsOk() {}
   void Badly_Named_Method() {}
   void TooLongMethodNameBecauseItHasMoreThan30Characters() {}
   void IsOkAlso() {}
   void Badly_Named_Method_2();
};

void MyClass::Badly_Named_Method_2() {

}

class stupid_class {
   void IsOk();
   void Badly_Named_Method_3() {}
};

void stupid_class::IsOk() {

}