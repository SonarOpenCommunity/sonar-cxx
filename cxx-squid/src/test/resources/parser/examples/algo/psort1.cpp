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

    INSERT_ELEMENTS(coll,3,7);
    INSERT_ELEMENTS(coll,2,6);
    INSERT_ELEMENTS(coll,1,5);
    PRINT_ELEMENTS(coll);

    // sort until the first five elements are sorted
    partial_sort (coll.begin(),      // beginning of the range
                  coll.begin()+5,    // end of sorted range
                  coll.end());       // end of full range
    PRINT_ELEMENTS(coll);

    // sort inversely until the first five elements are sorted
    partial_sort (coll.begin(),      // beginning of the range
                  coll.begin()+5,    // end of sorted range
                  coll.end(),        // end of full range
                  greater<int>());   // sorting criterion
    PRINT_ELEMENTS(coll);

    // sort all elements
    partial_sort (coll.begin(),      // beginning of the range
                  coll.end(),        // end of sorted range
                  coll.end());       // end of full range
    PRINT_ELEMENTS(coll);
}
