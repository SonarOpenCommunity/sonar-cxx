// vs-project.cpp : This file contains the 'main' function. Program execution begins and ends there.
//

#include <iostream>

void func(int i)
{
    if (i) {
        int i = 1; // C4189, C4457
        return;
    }
}

int main()
{
    std::cout << "Hello World!\n";
}
