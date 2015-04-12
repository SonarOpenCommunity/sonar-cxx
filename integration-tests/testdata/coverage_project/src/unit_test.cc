#include <iostream>
#include "component1.hh"

int main(int argc, char* argv[])
{
    std::cout << "Here is unit test" << std::endl;
    Bar().foo();
    return 0;
}
