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
#include <cstdlib>

namespace MyLib {
    double readAndProcessSum (std::istream&);
}

int main()
{
    using namespace std;
    double sum;

    try {
        sum = MyLib::readAndProcessSum(cin);
    }
    catch (const ios::failure& error) {
        cerr << "I/O exception: " << error.what() << endl;
        return EXIT_FAILURE;
    }
    catch (const exception& error) {
        cerr << "standard exception: " << error.what() << endl;
        return EXIT_FAILURE;
    }
    catch (...) {
        cerr << "unknown exception" << endl;
        return EXIT_FAILURE;
    }

    // print sum
    cout << "sum: " << sum << endl;
}

#include <istream>

namespace MyLib {
    double readAndProcessSum (std::istream& strm)
    {
        using std::ios;
        double value, sum;
    
        // save current state of exception flags
        ios::iostate oldExceptions = strm.exceptions();
    
        /* let failbit and badbit throw exceptions
         * - NOTE: failbit is also set at end-of-file
         */
        strm.exceptions (ios::failbit | ios::badbit);
    
        try {
            /* while stream is OK
             * - read value and add it to sum
             */
            sum = 0;
            while (strm >> value) {
                sum += value;
            }
        }
        catch (...) {
            /* if exception not caused by end-of-file
             * - restore old state of exception flags
             * - rethrow exception
             */
            if (!strm.eof()) {
                strm.exceptions(oldExceptions);  // restore exception flags
                throw;                           // rethrow
            }
        }
    
        // restore old state of exception flags
        strm.exceptions (oldExceptions);
    
        // return sum
        return sum;
    }
}
