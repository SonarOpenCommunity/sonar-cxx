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
   deque<int> coll;

   INSERT_ELEMENTS(coll,1,6);
   PRINT_ELEMENTS(coll);

   // print all differences between elements
   adjacent_difference (coll.begin(), coll.end(),         // source
                        ostream_iterator<int>(cout," ")); // dest.
   cout << endl;

   // print all sums with the predecessors
   adjacent_difference (coll.begin(), coll.end(),         // source
                        ostream_iterator<int>(cout," "),  // dest.
                        plus<int>());                     // operation
   cout << endl;

   // print all products between elements
   adjacent_difference (coll.begin(), coll.end(),         // source
                        ostream_iterator<int>(cout," "),  // dest.
                        multiplies<int>());               // operation
   cout << endl;
}
