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
    list<int> coll;

    INSERT_ELEMENTS(coll,2,6);
    INSERT_ELEMENTS(coll,4,9);
    PRINT_ELEMENTS(coll);

    // print all elements with value 5 replaced with 55
    replace_copy(coll.begin(), coll.end(),           // source
                 ostream_iterator<int>(cout," "),    // destination
                 5,                                  // old value
                 55);                                // new value
    cout << endl;

    // print all elements with a value less than 5 replaced with 42
    replace_copy_if(coll.begin(), coll.end(),        // source
                    ostream_iterator<int>(cout," "), // destination
                    bind2nd(less<int>(),5),     // replacement criterion
                    42);                        // new value
    cout << endl;

    // print each element while each odd element is replaced with 0
    replace_copy_if(coll.begin(), coll.end(),        // source
                    ostream_iterator<int>(cout," "), // destination
                    bind2nd(modulus<int>(),2),  // replacement criterion
                    0);                         // new value
    cout << endl;
}
