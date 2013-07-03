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

template <class charT, class traits>
inline
std::basic_istream<charT,traits>&
operator >> (std::basic_istream<charT,traits>& strm, Fraction& f)
{
    int n, d;

    // read value of numerator
    strm >> n;

    /* if available
     * - read '/' and value of demonimator
     */
    if (strm.peek() == '/') {
        strm.ignore();
        strm >> d;
    }
    else {
        d = 1;
    }

    /* if denominator is zero
     * - set failbit as I/O format error
     */
    if (d == 0) {
        strm.setstate(std::ios::failbit);
        return strm;
    }

    /* if everything is fine so far
     * change the value of the fraction
     */
    if (strm) {
        f = Fraction(n,d);
    }

    return strm;
}
