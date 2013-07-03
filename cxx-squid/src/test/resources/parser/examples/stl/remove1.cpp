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
#include <list>
#include <algorithm>
#include <iterator>
using namespace std;

int main()
{
    list<int> coll;

    // insert elements from 6 to 1 and 1 to 6
    for (int i=1; i<=6; ++i) {
        coll.push_front(i);
        coll.push_back(i);
    }

    // print all elements of the collection
    cout << "pre:  ";
    copy (coll.begin(), coll.end(),           // source
          ostream_iterator<int>(cout," "));   // destination
    cout << endl;

    // remove all elements with value 3
    remove (coll.begin(), coll.end(),         // range
            3);                               // value

    // print all elements of the collection
    cout << "post: ";
    copy (coll.begin(), coll.end(),           // source
          ostream_iterator<int>(cout," "));   // destination
    cout << endl;
}
