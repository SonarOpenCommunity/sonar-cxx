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
#include <vector>
#include <algorithm>
#include <iterator>
using namespace std;

// include self-defined fopow<>
#include "fopow.hpp"

int main()
{
    vector<int> coll;

    // insert elements from 1 to 9
    for (int i=1; i<=9; ++i) {
        coll.push_back(i);
    }

    // print 3 raised to the power of all elements
    transform (coll.begin(), coll.end(),           // source
               ostream_iterator<float>(cout," "),  // destination
               bind1st(fopow<float,int>(),3));     // operation
    cout << endl;

    // print all elements raised to the power of 3
    transform (coll.begin(), coll.end(),           // source
               ostream_iterator<float>(cout," "),  // destination
               bind2nd(fopow<float,int>(),3));     // operation
    cout << endl;
}
