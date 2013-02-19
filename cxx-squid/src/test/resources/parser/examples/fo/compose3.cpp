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
#include <algorithm>
#include <functional>
#include <string>
#include <cctype>
#include "compose22.hpp"
using namespace std;

int main()
{
    string s("Internationalization");
    string sub("Nation");

    // search substring case insensitive
    string::iterator pos;
    pos = search (s.begin(),s.end(),           // string to search in
                  sub.begin(),sub.end(),       // substring to search
                  compose_f_gx_hy(equal_to<int>(), // compar. criterion
                                  ptr_fun(::toupper),
                                  ptr_fun(::toupper)));

    if (pos != s.end()) {
        cout << "\"" << sub << "\" is part of \"" << s << "\""
             << endl;
    }
}
