/* multi-line definitions/declarations and comments at the end of last line */

/**
 docu MyEnum (issue #2161)
 */
typedef enum {
    A = 0, ///< docu A
    B =
        1, ///< docu B
} MyEnum;

/**
 docu MyClass
 */
class MyClass {
public:
   void method1() = 0; ///< docu method1
   void method2() =
                    0; ///< docu method2
};

/**
 docu MyType
 */
struct MyType {
   int var1 = 1; ///< docu var1
   int var2 =
              2; ///< docu var2
};
