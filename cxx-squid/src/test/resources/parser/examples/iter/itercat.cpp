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
#include <vector>
#include <iostream>
using namespace std;

int main()
{
   vector<int> coll;

   // insert elements from -3 to 9
   for (int i=-3; i<=9; ++i) {
       coll.push_back (i);
   }

   /* print number of elements by processing the distance between beginning and end
    * - NOTE: uses operator - for iterators
    */
   cout << "number/distance: " << coll.end()-coll.begin() << endl;

   /* print all elements
    * - NOTE: uses operator < instead of operator !=
    */
   vector<int>::iterator pos;
   for (pos=coll.begin(); pos<coll.end(); ++pos) {
       cout << *pos << ' ';
   }
   cout << endl;

   /* print all elements
    * - NOTE: uses operator [] instead of operator *
    */
   for (int i=0; i<coll.size(); ++i) {
       cout << coll.begin()[i] << ' ';
   }
   cout << endl;

   /* print every second element
    * - NOTE: uses operator +=
    */
   for (pos = coll.begin(); pos < coll.end()-1; pos += 2) {
       cout << *pos << ' ';
   }
   cout << endl;
}
