#include <iostream>
#include "component1.hh"

int main(int argc, char* argv[])
{
    std::cout << "Here is main" << std::endl;
    return Bar().foo();
}
