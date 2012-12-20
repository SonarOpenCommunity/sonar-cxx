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
    /* initialize source collection with ``..........abcdef..........''
     */
    vector<char> source(10,'.');
    for (int c='a'; c<='f'; c++) {
        source.push_back(c);
    }
    source.insert(source.end(),10,'.');
    PRINT_ELEMENTS(source,"source: ");

    // copy all letters three elements in front of the 'a'
    vector<char> c1(source.begin(),source.end());
    copy (c1.begin()+10, c1.begin()+16,  // source range
          c1.begin()+7);                 // destination range
    PRINT_ELEMENTS(c1,"c1:     ");

    // copy all letters three elements behind the 'f'
    vector<char> c2(source.begin(),source.end());
    copy_backward (c2.begin()+10, c2.begin()+16,  // source range
                   c2.begin()+19);                // destination range
    PRINT_ELEMENTS(c2,"c2:     ");
}
