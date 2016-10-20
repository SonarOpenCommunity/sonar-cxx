// MethodNameCheck

void fn_name_ok();
void fn_name_ok() {
}

class MyClass {
   void IsOk1();
   void IsOk2() {}

   void Badly_Named_Method1();
   void Badly_Named_Method2() {} // error

   void TooLongMethodNameBecauseItHasMoreThan30Characters1();
   void TooLongMethodNameBecauseItHasMoreThan30Characters2() {} // error
};

void MyClass::IsOk1()
{
}

void MyClass::Badly_Named_Method1() // error
{
}

void MyClass::TooLongMethodNameBecauseItHasMoreThan30Characters1() // error
{
}

// ignore Ctor and Dtor

class My_Class {
  
  My_Class();
  My_Class() {} // not an error
  
  ~My_Class();
  ~My_Class() {} // not an error
};

My_Class::My_Class() // not an error
{
}

test::My_Class::~My_Class() // not an error
{
}

// EOF
