void func1() {
   return;
}

int func2(int a) {
   if( a ) {
      return 1;
   } else {
      return 0;
   }
}

int func3(int a, int b) {
   if( a ) {
      if( b ) {
         return a+b ? 1 : 2;
      } else {
         return a+b ? 3 : 4;
      }
   } else {
      if( b ) {
         return a+b ? 5 : 6;
      } else {
         return a+b ? 7 : 8;
      }
   }
}

class MyClass {
public:
   MyClass() {};
   int Method1(int a, int b) {}
   int Method2(int a, int b) {
      if( a ) {
         if( b ) {
            return a+b ? 1 : 2;
         } else {
            return a+b ? 3 : 4;
         }
      } else {
         if( b ) {
            return a+b ? 5 : 6;
         } else {
            return a+b ? 7 : 8;
         }
      }
   }
   int Method3(int a, int b);
};

int MyClass::Method3(int a, int b) {
   if( a ) {
      if( b ) {
         return a+b ? 1 : 2;
      } else {
         return a+b ? 3 : 4;
      }
   } else {
      if( b ) {
         return a+b ? 5 : 6;
      } else {
         return a+b ? 7 : 8;
      }
   }
}

template <typename T>
class MyTemplate {
public:
   MyTemplate() {};
   int Method1(T a, T b) {}
   int Method2(T a, T b) {
      if( a ) {
         if( b ) {
            return a+b ? 1 : 2;
         } else {
            return a+b ? 3 : 4;
         }
      } else {
         if( b ) {
            return a+b ? 5 : 6;
         } else {
            return a+b ? 7 : 8;
         }
      }
   }
};

std::string func_with_trycatch(int i) {
   try {
      if ((i % 2 == 0 && i % 3 == 0) || (i % 6 == 0)) {
         return std::to_string(i);
      }
   } catch (const std::logic_error& e) {
      return "std::logic_error";
   } catch (const std::bad_cast& e) {
      return std::string { "std::bad_cast" };
   } catch (const std::invalid_argument& e) {
      return std::string { "std::invalid_argument" };
   } catch (const std::length_error& e) {
      return std::string { "std::length_error" };
   } catch (const std::out_of_range& e) {
      return std::string { "std::out_of_range" };
   } catch (const std::exception& e) {
      return std::string("std::exception");
   } catch (...) {
      while (i >= 0) {
         return (i % 3 == 0) ? "unknown exception" : "unexpected exception";
      }
      return (i % 2 == 0) ? "exotic exception" : "strange exception";
   }
}
