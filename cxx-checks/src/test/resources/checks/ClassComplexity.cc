class MyClass1 {
public:
   MyClass1() {};
   int Method1(int a, int b) {
      return 0;
   }
};

class MyClass2 {
public:
   // nested class adds complexity to the outer one
   class SimpleNestedClass {
   public:
      SimpleNestedClass() {
      }
      int Method0(int a, int b) {
         return 0;
      }
   };

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

class MyClass3 {
public:
   // nested class adds complexity to the outer one
   class ComplexNestedClass {
   public:
      ComplexNestedClass() {
      }
      int Method0(int in) {
         switch (in) {
         case 0:
            return 1;
         default:
            return 2;
         }
      }
      int Method1() {
         for (int i = 0; i < 10; ++i) {
            if (i != 0 && i % 2 == 0 && i % 3 == 0) {
               return i;
            }
         }
      }
   };
   void Method1() {
   }
};
