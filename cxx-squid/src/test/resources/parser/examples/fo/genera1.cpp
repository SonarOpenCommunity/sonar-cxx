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
#include "print.hpp"
using namespace std;

class IntSequence {
  private:
    int value;
  public:
    // constructor
    IntSequence (int initialValue)
     : value(initialValue) {
    }

    // ``function call''
    int operator() () {
        return value++;
    }
};

int main()
{
    list<int> coll;

    // insert values from 1 to 9
    generate_n (back_inserter(coll),    // start
                9,                      // number of elements
                IntSequence(1));        // generates values

    PRINT_ELEMENTS(coll);

    // replace second to last element but one with values starting at 42
    generate (++coll.begin(),           // start
              --coll.end(),             // end
              IntSequence(42));         // generates values

    PRINT_ELEMENTS(coll);
}
