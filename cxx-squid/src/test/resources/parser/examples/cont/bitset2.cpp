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
#include <bitset>
#include <iostream>
#include <string>
#include <limits>
using namespace std;

int main()
{
    /* print some numbers in binary representation
     */
    cout << "267 as binary short:     "
         << bitset<numeric_limits<unsigned short>::digits>(267)
         << endl;

    cout << "267 as binary long:      "
         << bitset<numeric_limits<unsigned long>::digits>(267)
         << endl;

    cout << "10,000,000 with 24 bits: "
         << bitset<24>(1e7) << endl;

    /* transform binary representation into integral number
     */
    cout << "\"1000101011\" as number:  "
         << bitset<100>(string("1000101011")).to_ulong() << endl;
}
