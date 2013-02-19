/* The following code example is taken from the book
 * "The C++ Standard Library - A Tutorial and Reference"
 * by Nicolai M. Josuttis, Addison-Wesley, 1999
 *
 * (C) Copyright Nicolai M. Josuttis 1999.
 * Permission to copy, use, modify, sell and distribute this software
 * is granted provided this copyright notice appears in all copies.
 * This software is provided "as is" without express or implied
 * warranty, and with no claim as to its suitability for any purpose.
 */
#include <iostream>
#include "ignoreparam.hpp"

int main()
{
    char c;
    std::cout << "ignore two lines and print frist character following them\n";
    std::cin >> ignore(2) >> c;
    std::cout << "c: " << c << std::endl;
}
