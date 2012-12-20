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
    // source data
    int source[] = { 1, 4, 4, 6, 1, 2, 2, 3, 1, 6, 6, 6, 5, 7,
                      5, 4, 4 };
    int sourceNum = sizeof(source)/sizeof(source[0]);

    list<int> coll;

    // initialize coll with elements from source
    copy (source, source+sourceNum,           // source
          back_inserter(coll));               // destination
    PRINT_ELEMENTS(coll);

    // remove consecutive duplicates
    list<int>::iterator pos;
    pos = unique (coll.begin(), coll.end());

    /* print elements not removed
     * - use new logical end
     */
    copy (coll.begin(), pos,                  // source
          ostream_iterator<int>(cout," "));   // destination
    cout << "\n\n";

    // reinitialize coll with elements from source
    copy (source, source+sourceNum,           // source
          coll.begin());                      // destination
    PRINT_ELEMENTS(coll);

    // remove elements if there was a previous greater element
    coll.erase (unique (coll.begin(), coll.end(),
                        greater<int>()),
                coll.end());
    PRINT_ELEMENTS(coll);
}
