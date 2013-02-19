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
#include <vector>
#include <algorithm>
using namespace std;

// function object to process the mean value
class MeanValue {
  private:
    long num;    // number of elements
    long sum;    // sum of all element values
  public:
    // constructor
    MeanValue () : num(0), sum(0) {
    }

    // ``function call''
    // - process one more element of the sequence
    void operator() (int elem) {
        num++;          // increment count
        sum += elem;    // add value
    }

    // return mean value
    double value () {
        return static_cast<double>(sum) / static_cast<double>(num);
    }
};

int main()
{
    vector<int> coll;

    // insert elments from 1 to 8
    for (int i=1; i<=8; ++i) {
        coll.push_back(i);
    }

    // process and print mean value
    MeanValue mv = for_each (coll.begin(), coll.end(),  // range
                             MeanValue());              // operation
    cout << "mean value: " << mv.value() << endl;
}
