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
#include <limits>

// Ignore: manipulator that ignores N lines
class Ignore {
  public:
    int num;
    Ignore(int n) : num(n) {
    }
};
    
// convenience function
Ignore ignore(int n)
{
    return Ignore(n);
}

std::istream& operator >> (std::istream& strm, const Ignore& manip)
{
    for (int i=0; i<manip.num; ++i) {
        strm.ignore(std::numeric_limits<int>::max(),'\n'); 
    }
    return strm;
}
