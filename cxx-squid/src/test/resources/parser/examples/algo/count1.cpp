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

bool isEven (int elem)
{
    return elem % 2 == 0;
}

int main()
{
    vector<int> coll;
    int num;
    INSERT_ELEMENTS(coll,1,9);
    PRINT_ELEMENTS(coll,"coll: ");

    // count and print elements with value 4
    num = count (coll.begin(), coll.end(),       // range
                 4);                             // value
    cout << "number of elements equal to 4:      " << num << endl;

    // count elements with even value
    num = count_if (coll.begin(), coll.end(),    // range
                    isEven);                     // criterion
    cout << "number of elements with even value: " << num << endl;

    // count elements that are greater than value 4
    num = count_if (coll.begin(), coll.end(),    // range
                    bind2nd(greater<int>(),4));  // criterion
    cout << "number of elements greater than 4:  " << num << endl;
}
