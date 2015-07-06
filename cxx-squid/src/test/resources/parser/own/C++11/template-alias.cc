template <typename First, typename Second, int Third>
class SomeType;

template <typename Second>
using TypedefName = SomeType<OtherType, Second, 5>;

typedef void (*FunctionType)(double);  // Old style
using FunctionType = void (*)(double); // New introduced syntax

using myint32 = int;
using myfunptr = int(*)( double );
using mymemfunptr = int(Class::*)( double );
