#include <iostream>
#include "../lib/component1.hh"

#include "gtest.h"

int main(int argc, char* argv[])
{
    std::cout << "Here is main" << std::endl;
    return Bar().foo();
}
