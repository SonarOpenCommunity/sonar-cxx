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
#include <set>
#include <deque>
#include <algorithm>
#include "print.hpp"
using namespace std;

int main()
{
    set<int,greater<int> > coll1;
    deque<int> coll2;

    // insert elements from 1 to 9
    for (int i=1; i<=9; ++i) {
        coll1.insert(i);
    }

    PRINT_ELEMENTS(coll1,"initialized: ");

    // transform all elements into coll2 by multiplying 10
    transform (coll1.begin(),coll1.end(),        // source
               back_inserter(coll2),             // destination
               bind2nd(multiplies<int>(),10));   // operation

    PRINT_ELEMENTS(coll2,"transformed: ");

    // replace value equal to 70 with 42
    replace_if (coll2.begin(),coll2.end(),       // range
                bind2nd(equal_to<int>(),70),     // replace criterion
                42);                             // new value

    PRINT_ELEMENTS(coll2,"replaced:    ");

    // remove all elements with values less than 50
    coll2.erase(remove_if(coll2.begin(),coll2.end(), // range
                          bind2nd(less<int>(),50)),  // remove criterion
                coll2.end());

    PRINT_ELEMENTS(coll2,"removed:     ");
}
