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
#include "algostuff.hpp"
using namespace std;

int main()
{
    vector<int> coll;

    coll.push_back(17);
    coll.push_back(-3);
    coll.push_back(22);
    coll.push_back(13);
    coll.push_back(13);
    coll.push_back(-9);
    PRINT_ELEMENTS(coll,"coll:     ");

    // convert into relative values
    adjacent_difference (coll.begin(), coll.end(),   // source
                         coll.begin());              // destination
    PRINT_ELEMENTS(coll,"relative: ");
     
    // convert into absolute values
    partial_sum (coll.begin(), coll.end(),           // source
                 coll.begin());                      // destination
    PRINT_ELEMENTS(coll,"absolute: ");
}
