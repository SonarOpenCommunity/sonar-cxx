
#include <cstdio>

enum class my_enum
{
   value1 = 1,
   value2 = 2
};

enum struct my_enum2
{
   value1 = 10,
   value2 = 20
};

int main(int argc, char** argv)
{
   my_enum a = my_enum::value1;
   my_enum2 b = my_enum2::value1;

   printf("a=%d\n", static_cast<int>(a));
   printf("b=%d\n", static_cast<int>(b));

   return 0;
}
