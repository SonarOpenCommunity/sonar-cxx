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
    deque<int> coll1;
    vector<int> coll6(6);      // initialize with 6 elements
    vector<int> coll30(30);    // initialize with 30 elements

    INSERT_ELEMENTS(coll1,3,7);
    INSERT_ELEMENTS(coll1,2,6);
    INSERT_ELEMENTS(coll1,1,5);
    PRINT_ELEMENTS(coll1);

    // copy elements of coll1 sorted into coll6
    vector<int>::iterator pos6;
    pos6 = partial_sort_copy (coll1.begin(), coll1.end(),
                              coll6.begin(), coll6.end());

    // print all copied elements
    copy (coll6.begin(), pos6,
          ostream_iterator<int>(cout," "));
    cout << endl;

    // copy elements of coll1 sorted into coll30
    vector<int>::iterator pos30;
    pos30 = partial_sort_copy (coll1.begin(), coll1.end(),
                               coll30.begin(), coll30.end(),
                               greater<int>());

    // print all copied elements
    copy (coll30.begin(), pos30,
          ostream_iterator<int>(cout," "));
    cout << endl;
}
