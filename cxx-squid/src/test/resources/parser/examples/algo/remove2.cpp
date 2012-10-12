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
    list<int> coll1;

    INSERT_ELEMENTS(coll1,1,6);
    INSERT_ELEMENTS(coll1,1,9);
    PRINT_ELEMENTS(coll1);

    // print elements without those having the value 3
    remove_copy(coll1.begin(), coll1.end(),       // source
                ostream_iterator<int>(cout," "),  // destination
                3);                               // removed value
    cout << endl;

    // print elements without those having a value greater than 4
    remove_copy_if(coll1.begin(), coll1.end(),      // source
                   ostream_iterator<int>(cout," "), // destination
                   bind2nd(greater<int>(),4));      // removed elements
    cout << endl;

    // copy all elements greater than 3 into a multiset
    multiset<int> coll2;
    remove_copy_if(coll1.begin(), coll1.end(),    // source
                   inserter(coll2,coll2.end()),   // destination
                   bind2nd(less<int>(),4));       // elements not copied
    PRINT_ELEMENTS(coll2);
}
