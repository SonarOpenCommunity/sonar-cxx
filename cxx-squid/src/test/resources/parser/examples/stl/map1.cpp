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
#include <map>
#include <string>
using namespace std;

int main()
{
    /* type of the container:
     * - map: elements key/value pairs
     * - string: keys have type string
     * - float: values have type float
     */
    typedef map<string,float> StringFloatMap;

    StringFloatMap coll;

    // insert some elements into the collection
    coll["VAT"] = 0.15;
    coll["Pi"] = 3.1415;
    coll["an arbitrary number"] = 4983.223;
    coll["Null"] = 0;

    /* print all elements
     * - iterate over all elements
     * - element member first is the key
     * - element member second is the value
     */
    StringFloatMap::iterator pos;
    for (pos = coll.begin(); pos != coll.end(); ++pos) {
        cout << "key: \"" << pos->first << "\" "
             << "value: " << pos->second << endl;
    }
}
