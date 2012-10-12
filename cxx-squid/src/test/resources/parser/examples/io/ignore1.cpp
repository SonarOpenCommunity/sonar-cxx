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
#include "ignore.hpp"

int main()
{
    int i;
    std::cout << "read int and ignore rest of the line" << std::endl;
    std::cin >> i;

    // ignore the rest of the line
    std::cin >> ignoreLine;

    std::cout << "int: " << i << std::endl;

    std::cout << "read int and ignore two lines" << std::endl;
    std::cin >> i;

    // ignore two lines
    std::cin >> ignoreLine >> ignoreLine;

    std::cout << "int: " << i << std::endl;
}
