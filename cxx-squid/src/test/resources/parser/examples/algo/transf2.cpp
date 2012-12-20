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

    INSERT_ELEMENTS(coll1,1,9);
    PRINT_ELEMENTS(coll1,"coll1:   ");

    // square each element
    transform (coll1.begin(), coll1.end(),       // first source range
               coll1.begin(),                    // second source range
               coll1.begin(),                    // destination range
               multiplies<int>());               // operation
    PRINT_ELEMENTS(coll1,"squared: ");

    /* add each element traversed forward with each element traversed backward
     * and insert result into coll2
     */
    transform (coll1.begin(), coll1.end(),       // first source range
               coll1.rbegin(),                   // second source range
               back_inserter(coll2),             // destination range
               plus<int>());                     // operation
    PRINT_ELEMENTS(coll2,"coll2:   ");

    // print differences of two corresponding elements
    cout << "diff:    ";
    transform (coll1.begin(), coll1.end(),       // first source range
               coll2.begin(),                    // second source range
               ostream_iterator<int>(cout, " "), // destination range
               minus<int>());                    // operation
    cout << endl;
}
