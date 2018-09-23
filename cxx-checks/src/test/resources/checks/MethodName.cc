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

  class My_Inner_Class {
    My_Inner_Class();
    ~My_Inner_Class();
  };

  template<typename T>
  class My_Inner_Class_With_Template {
    My_Inner_Class_With_Template();
    ~My_Inner_Class_With_Template();

    class Third_Level_Nested_Class {
      Third_Level_Nested_Class();
      ~Third_Level_Nested_Class()

      void Third_Level_Nested_Class_getX();
    };
  };
};

My_Class::My_Class() // not an error
{
}

test::My_Class::~My_Class() // not an error
{
}

test::My_Class::My_Inner_Class::My_Inner_Class() // not an error
{
}

test::My_Class::My_Inner_Class::~My_Inner_Class() // not an error
{
}

template<typename T>
My_Class::My_Inner_Class_With_Template<T>::My_Inner_Class_With_Template() // not an error
{
}

template<typename T>
My_Class::My_Inner_Class_With_Template<T>::~My_Inner_Class_With_Template() // not an error
{
}

template<typename T>
My_Class::My_Inner_Class_With_Template<T>::Third_Level_Nested_Class::Third_Level_Nested_Class() // not an error
{
}

template<typename T>
My_Class::My_Inner_Class_With_Template<T>::Third_Level_Nested_Class::Third_Level_Nested_Class() // not an error
{
}

template<typename T>
void My_Class::My_Inner_Class_With_Template<T>::Third_Level_Nested_Class::Third_Level_Nested_Class_getX() // error
{
}

// EOF
