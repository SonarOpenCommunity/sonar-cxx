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
    PRINT_ELEMENTS(coll);

    // process sum of elements
    cout << "sum: "
         << accumulate (coll.begin(), coll.end(),    // range
                        0)                           // initial value
         << endl;

    // process sum of elements less 100
    cout << "sum: "
         << accumulate (coll.begin(), coll.end(),    // range
                        -100)                        // initial value
         << endl;

    // process product of elements
    cout << "product: "
         << accumulate (coll.begin(), coll.end(),    // range
                        1,                           // initial value
                        multiplies<int>())           // operation
         << endl;

    // process product of elements (use 0 as initial value)
    cout << "product: "
         << accumulate (coll.begin(), coll.end(),    // range
                        0,                           // initial value
                        multiplies<int>())           // operation
         << endl;
}
