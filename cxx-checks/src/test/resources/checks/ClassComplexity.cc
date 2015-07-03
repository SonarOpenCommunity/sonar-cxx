class MyClass1 {
public:
   MyClass1() {};
   int Method1(int a, int b) {
      return 0;
   }
};

class MyClass2 {
public:
   MyClass2() {};
   int Method1(int a, int b) {
      return 0;
   }
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
};
