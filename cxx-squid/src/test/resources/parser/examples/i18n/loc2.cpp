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
#include <locale>
#include <string>
#include <cstdlib>
using namespace std;

int main()
{
    // create the default locale from the user's environment
    locale langLocale("");

    // and assign it to the standard ouput channel
    cout.imbue(langLocale);

    // process the name of the locale
    bool isGerman;
    if (langLocale.name() == "de_DE" ||
        langLocale.name() == "de" ||
        langLocale.name() == "german") {
          isGerman = true;
    }
    else {
          isGerman = false;
    }

    // read locale for the input
    if (isGerman) {
        cout << "Sprachumgebung fuer Eingaben: ";
    }
    else {
        cout << "Locale for input: ";
    }
    string s;
    cin >> s;
    if (!cin) {
        if (isGerman) {
            cerr << "FEHLER beim Einlesen der Sprachumgebung"
                 << endl;
        }
        else {
            cerr << "ERROR while reading the locale" << endl;
        }
        return EXIT_FAILURE;
    }
    locale cinLocale(s.c_str());

    // and assign it to the standard input channel
    cin.imbue(cinLocale);

    // read and output floating-point values in a loop
    double value;
    while (cin >> value) {
        cout << value << endl;
    }
}
