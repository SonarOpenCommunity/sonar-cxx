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

    INSERT_ELEMENTS(coll,1,9);
    PRINT_ELEMENTS(coll,"coll:      ");

    // rotate one element to the left
    rotate (coll.begin(),      // beginning of range
            coll.begin() + 1,  // new first element
            coll.end());       // end of range
    PRINT_ELEMENTS(coll,"one left:  ");

    // rotate two elements to the right
    rotate (coll.begin(),      // beginning of range
            coll.end() - 2,    // new first element
            coll.end());       // end of range
    PRINT_ELEMENTS(coll,"two right: ");

    // rotate so that element with value 4 is the beginning
    rotate (coll.begin(),                     // beginning of range
            find(coll.begin(),coll.end(),4),  // new first element
            coll.end());                      // end of range
    PRINT_ELEMENTS(coll,"4 first:   ");
}
