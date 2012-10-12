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
    vector<int> coll1;
    list<int> coll2;

    INSERT_ELEMENTS(coll1,1,6);

    for (int i=1; i<=16; i*=2) {
        coll2.push_back(i);
    }
    coll2.push_back(3);

    PRINT_ELEMENTS(coll1,"coll1: ");
    PRINT_ELEMENTS(coll2,"coll2: ");

    // find first mismatch
    pair<vector<int>::iterator,list<int>::iterator> values;
    values = mismatch (coll1.begin(), coll1.end(),  // first range
                       coll2.begin());              // second range
    if (values.first == coll1.end()) {
        cout << "no mismatch" << endl;
    }
    else {
        cout << "first mismatch: "
             << *values.first  << " and "
             << *values.second << endl;
    }

    /* find first position where the element of coll1 is not
     * less than the corresponding element of coll2
     */
    values = mismatch (coll1.begin(), coll1.end(),  // first range
                       coll2.begin(),               // second range
                       less_equal<int>());          // criterion
    if (values.first == coll1.end()) {
        cout << "always less-or-equal" << endl;
    }
    else {
        cout << "not less-or-equal: "
             << *values.first << " and "
             << *values.second << endl;
    }
}
