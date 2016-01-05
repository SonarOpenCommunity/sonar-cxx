void test_function_definition() { // 1
}

int test_return() { // 1
   return 1;
}

int test_if(int p) { // 2
   if( p ) {
      return 1;
   }
   return 0;
}

int test_if_else(int p) { // 2
   if( p ) {
      return 1;
   }
   else {
      return 0;
   }
}

int test_or(int p1, int p2) { // 3
   if( p1 || p2 ) {
      return 1;
   }
   else {
      return 0;
   }
}

int test_and(int p1, int p2) { // 3
   if( p1 && p2 ) {
      return 1;
   }
   else {
      return 0;
   }
}

int test_quest(int p) { // 2
   return p ? 1 : 0;
}

void test_while() { // 2
   int i = 10;
   while(i) {
      --i;
   }
}

void test_for() { // 2
   int j = 0;
   for(int i=0; i<10; ++i) {
      ++j;
   }
}

int test_switch_case(int p) { // 5
   switch(p) {
      case 1: return 1;
      case 2: return 2;
      case 3: return 3;
      default: return -1;
   }
}

int test_catch1() { // 2
   int i = 0;
   try {
      i = i / 0;
   }
   catch(...) {
      i = 0;
   }
}

int test_catch2() { // 3
   int i = 0;
   try {
      i = i / 0;
   }
   catch(const exception& e) {
      i = 0;
   }
   catch(...) {
      i = 0;
   }
}

class ClassA { // 5
   ClassA() {}
   ~ClassA() {}
   void test_method1() {}
   void test_method2() {}
   void test_method3() {}
};

class ClassB { // definition outside
   ClassB();
   ~ClassB();
   void test_method1();
   void test_method2();
   void test_method3();
};

ClassB::ClassB() { // 1
}

ClassB::~ClassB() { // 1
}

void ClassB::test_method1() { // 1
}

void ClassB::test_method2() { // 1
}

void ClassB::test_method3() { // 1
}

// summary:
// functions/methods              : 22
// classes                        : 2
// complexity                     : 38
// complexity in functions/methods: 38
// complexity in classes          : 10
