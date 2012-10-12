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
#include <algorithm>
using namespace std;

#include "print.hpp"

#include "assoiter.hpp"

int main()
{
    set<int> coll;   

    // create inserter for coll
    // - inconvenient way
    asso_insert_iterator<set<int> > iter(coll);

    // insert elements with the usual iterator interface
    *iter = 1;
    iter++;
    *iter = 2;
    iter++;
    *iter = 3;

    PRINT_ELEMENTS(coll);

    // create inserter for coll and insert elements
    // - convenient way
    asso_inserter(coll) = 44;
    asso_inserter(coll) = 55;

    PRINT_ELEMENTS(coll);

    // use inserter with an algorithm
    int vals[] = { 33, 67, -4, 13, 5, 2 };
    copy (vals, vals+(sizeof(vals)/sizeof(vals[0])),  // source
          asso_inserter(coll));                       // destination

    PRINT_ELEMENTS(coll);
}
