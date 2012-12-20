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
#include <fstream>
using namespace std;

int main()
{
    // open file ``example.dat'' for reading and writing
    filebuf buffer;
    ostream output(&buffer);
    istream input(&buffer);
    buffer.open ("example.dat", ios::in | ios::out | ios::trunc);

    for (int i=1; i<=4; i++) {
        // write one line
        output << i << ". line" << endl;

        // print all file contents
        input.seekg(0);          // seek to the beginning
        char c;
        while (input.get(c)) {
            cout.put(c);
        }
        cout << endl;
        input.clear();           // clear  eofbit and  failbit
    }
}
