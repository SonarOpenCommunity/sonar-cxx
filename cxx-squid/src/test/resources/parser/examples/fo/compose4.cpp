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
#include <list>
#include <algorithm>
#include <functional>
#include <cstdlib>
#include "print.hpp"
#include "compose10.hpp"
using namespace std;
using namespace boost;


int main()
{
    list<int> coll;

    // insert five random numbers
    generate_n (back_inserter(coll),      // beginning of destination range
                5,                        // count
                rand);                    // new value generator
    PRINT_ELEMENTS(coll);

    // overwrite with five new random numbers
    // in the range between 0 (including) and 10 (excluding)
    generate (coll.begin(), coll.end(),   // destination range
              compose_f_g(bind2nd(modulus<int>(),10),
                          ptr_fun(rand)));
    PRINT_ELEMENTS(coll);
}
