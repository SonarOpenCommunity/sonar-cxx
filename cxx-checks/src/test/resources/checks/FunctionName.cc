// FunctionNameCheck

void fn_name_ok();
void fn_name_ok() {
}

void Badly_Named_Function();
void Badly_Named_Function() { // error
}

void too_long_function_name_because_it_has_more_than_30_characters();
void too_long_function_name_because_it_has_more_than_30_characters() { // error
}

class MyClass {
   void method_name_ok1();
   void method_name_ok2() {}

   void too_long_function_name_because_it_has_more_than_30_characters1();
   void too_long_function_name_because_it_has_more_than_30_characters2() {}
};

void MyClass::method_name_ok1()
{
}

void MyClass::too_long_function_name_because_it_has_more_than_30_characters1()
{
}
