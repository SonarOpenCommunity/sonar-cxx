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

bool differenceOne (int elem1, int elem2)
{
    return elem1 + 1 == elem2 || elem1 - 1 == elem2;
}

int main()
{
    // source data
    int source[] = { 1, 4, 4, 6, 1, 2, 2, 3, 1, 6, 6, 6, 5, 7,
                      5, 4, 4 };
    int sourceNum = sizeof(source)/sizeof(source[0]);

    // initialize coll with elements from source
    list<int> coll;
    copy(source, source+sourceNum,      // source
         back_inserter(coll));          // destination
    PRINT_ELEMENTS(coll);

    // print elements with consecutive duplicates removed
    unique_copy(coll.begin(), coll.end(),          // source
                ostream_iterator<int>(cout," "));  // destination
    cout << endl;

    // print elements without consecutive entries that differ by one
    unique_copy(coll.begin(), coll.end(),         // source
                ostream_iterator<int>(cout," "),  // destination
                differenceOne);                   // duplicates criterion
    cout << endl;
}
