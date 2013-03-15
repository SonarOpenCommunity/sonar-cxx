#include "../sample/sampleclass.h"

#include <iostream>
using namespace std;

void main()
{
    SampleClass testClass("jORGE", 32);
    std::cout << testClass.GetName();
}
