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
#include <vector>
#include "myalloc.hpp"

int main()
{
    // create a vector, using MyAlloc<> as allocator
    std::vector<int,MyLib::MyAlloc<int> > v;

    // insert elements
    // - causes reallocations
    v.push_back(42);
    v.push_back(56);
    v.push_back(11);
    v.push_back(22);
    v.push_back(33);
    v.push_back(44);
}

