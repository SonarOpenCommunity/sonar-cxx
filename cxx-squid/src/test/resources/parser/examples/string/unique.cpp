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
#include <algorithm>
#include <iterator>
#include <locale>
using namespace std;

class bothWhiteSpaces {
  private:
    const locale& loc;    // locale
  public:
    /* constructor
     * - save the locale object
     */
    bothWhiteSpaces (const locale& l) : loc(l) {
    }
    /* function call
     * - returns whether both characters are whitespaces
     */
    bool operator() (char elem1, char elem2) {
        return isspace(elem1,loc) && isspace(elem2,loc);
    }
};

int main()
{
    string contents;

    // don't skip leading whitespaces
    cin.unsetf (ios::skipws);

    // read all characters while compressing whitespaces
    unique_copy(istream_iterator<char>(cin),    // beginning of source
                istream_iterator<char>(),       // end of source
                back_inserter(contents),        // destination
                bothWhiteSpaces(cin.getloc())); // criterion for removing

    // process contents
    // - here: write it to the standard output
    cout << contents;
}
