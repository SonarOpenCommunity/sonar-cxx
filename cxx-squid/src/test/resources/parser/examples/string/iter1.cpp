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
#include <string>
#include <iostream>
#include <algorithm>
#include <cctype>
using namespace std;

int main()
{
    // create a string
    string s("The zip code of Hondelage in Germany is 38108");
    cout << "original: " << s << endl;

    // lowercase all characters
    transform (s.begin(), s.end(),    // source
               s.begin(),             // destination
               tolower);              // operation
    cout << "lowered:  " << s << endl;

    // uppercase all characters
    transform (s.begin(), s.end(),    // source
               s.begin(),             // destination
               toupper);              // operation
    cout << "uppered:  " << s << endl;
}

