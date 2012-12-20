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

   INSERT_ELEMENTS(coll,1,9);
   PRINT_ELEMENTS(coll);

   // find four consecutive elements with value 3
   deque<int>::iterator pos;
   pos = search_n (coll.begin(), coll.end(),    // range
                   4,                           // count
                   3);                          // value

   // print result
   if (pos != coll.end()) {
       cout << "four consecutive elements with value 3 "
            << "start with " << distance(coll.begin(),pos) +1
            << ". element" << endl;
   }
   else {
       cout << "no four consecutive elements with value 3 found"
            << endl;
   }

   // find four consecutive elements with value greater than 3
   pos = search_n (coll.begin(), coll.end(),    // range
                   4,                           // count
                   3,                           // value
                   greater<int>());             // criterion

   // print result
   if (pos != coll.end()) {
       cout << "four consecutive elements with value > 3 "
            << "start with " << distance(coll.begin(),pos) +1
            << ". element" << endl;
   }
   else {
       cout << "no four consecutive elements with value > 3 found"
            << endl;
   }
}
