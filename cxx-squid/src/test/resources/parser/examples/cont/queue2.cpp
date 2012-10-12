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
#include <string>
#include "Queue.hpp"      // use special queue class
using namespace std;

int main()
{
   try {    
      Queue<string> q;

      // insert three elements into the queue
      q.push("These ");
      q.push("are ");
      q.push("more than ");

      // read and print two elements from the queue
      cout << q.pop();
      cout << q.pop();

      // push two new elements
      q.push("four ");
      q.push("words!");

      // skip one element
      q.pop();

      // read and print two elements from the queue
      cout << q.pop();
      cout << q.pop() << endl;

      // print number of remaining elements
      cout << "number of elements in the queue: " << q.size()
           << endl;

      // read and print one element
      cout << q.pop() << endl;
   }
   catch (const exception& e) {
      cerr << "EXCEPTION: " << e.what() << endl;
   }
}
