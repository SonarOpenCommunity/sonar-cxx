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
using namespace std;

bool nocase_compare (char c1, char c2)
{
    return toupper(c1) == toupper(c2);
}

int main()
{
    string s1("This is a string");
    string s2("STRING");
    
    // compare case insensitive
    if (s1.size() == s2.size() &&        // ensure same sizes
        equal (s1.begin(),s1.end(),      // first source string
               s2.begin(),               // second source string
               nocase_compare)) {        // comparison criterion
        cout << "the strings are equal" << endl;
    }
    else {
        cout << "the strings are not equal" << endl;
    }

    // search case insensitive
    string::iterator pos;
    pos = search (s1.begin(),s1.end(),   // source string in which to search
                  s2.begin(),s2.end(),   // substring to search
                  nocase_compare);       // comparison criterion
    if (pos == s1.end()) {
        cout << "s2 is not a substring of s1" << endl;
    }
    else {
        cout << '"' << s2 << "\" is a substring of \""
             << s1 << "\" (at index " << pos - s1.begin() << ")"
             << endl;
    }
}

