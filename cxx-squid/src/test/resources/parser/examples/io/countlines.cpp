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
#include <iterator>
#include <algorithm>
#include <fstream>
#include <iostream>

int countLines (std::istream& in);

int main (int argc, char* argv[])
{
    int count;

    if (argc == 1) {
       // no argument => count lines of standard input
       count = countLines(std::cin);
    }
    else {
       // count number of lines of all files passed as argument
       std::ifstream in;
       count = 0;
       for (int i=1; i<argc; ++i) {
           in.open(argv[i]);
           if (!in) {
               std::cerr << "failed to open " << argv[1] << "\n";
           }
           else {
               count += countLines(in);
               in.close();
           }
       }
    }
    std::cout << count << std::endl;
}

int countLines (std::istream& in)
{
    return std::count(std::istreambuf_iterator<char>(in),
                      std::istreambuf_iterator<char>(),
                      '\n');
}
