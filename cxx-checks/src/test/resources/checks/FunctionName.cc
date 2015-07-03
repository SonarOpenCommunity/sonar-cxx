// FunctionNameCheck

void fn_name_ok();
void fn_name_ok() {
}

void Badly_Named_Function();
void Badly_Named_Function() {
}

void too_long_function_name_because_it_has_more_than_30_characters();
void too_long_function_name_because_it_has_more_than_30_characters() {
}

class MyClass {
   void too_long_function_name_because_it_has_more_than_30_characters() {}
};
