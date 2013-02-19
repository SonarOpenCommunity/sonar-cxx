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
    deque<int> coll;
    list<int> subcoll;

    INSERT_ELEMENTS(coll,1,7);
    INSERT_ELEMENTS(coll,1,7);

    INSERT_ELEMENTS(subcoll,3,6);

    PRINT_ELEMENTS(coll,   "coll:    ");
    PRINT_ELEMENTS(subcoll,"subcoll: ");

    // search first occurrence of subcoll in coll
    deque<int>::iterator pos;
    pos = search (coll.begin(), coll.end(),         // range
                  subcoll.begin(), subcoll.end());  // subrange

    // loop while subcoll found as subrange of coll
    while (pos != coll.end()) { 
        // print position of first element
        cout << "subcoll found starting with element "
             << distance(coll.begin(),pos) + 1
             << endl;

        // search next occurrence of subcoll
        ++pos;
        pos = search (pos, coll.end(),                  // range
                      subcoll.begin(), subcoll.end());  // subrange
    }
}
