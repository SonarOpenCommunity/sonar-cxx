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
