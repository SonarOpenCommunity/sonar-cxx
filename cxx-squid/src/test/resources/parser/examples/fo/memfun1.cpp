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
//#define mem_fun1 mem_fun
#include <iostream>
#include <vector>
#include <string>
#include <algorithm>
#include <functional>


class Person {
  private:
    std::string name;
  public:
    //...
    void print () const {
        std::cout << name << std::endl;
    }
    void printWithPrefix (std::string prefix) const {
        std::cout << prefix << name << std::endl;
    }
};

void foo (const std::vector<Person>& coll)
{
    using std::for_each;
    using std::bind2nd;
    using std::mem_fun_ref;

    // call member function print() for each element
    for_each (coll.begin(), coll.end(),
              mem_fun_ref(&Person::print));

    // call member function printWithPrefix() for each element
    // - "person: " is passed as an argument to the member function
    for_each (coll.begin(), coll.end(),
              bind2nd(mem_fun_ref(&Person::printWithPrefix),
                      "person: "));
}


void ptrfoo (const std::vector<Person*>& coll)
                                   // ^^^ pointer !
{
    using std::for_each;
    using std::bind2nd;
    using std::mem_fun;

    // call member function print() for each referred object
    for_each (coll.begin(), coll.end(),
              mem_fun(&Person::print));

    // call member function printWithPrefix() for each referred object
    // - "person: " is passed as an argument to the member function
    for_each (coll.begin(), coll.end(),
              bind2nd(mem_fun(&Person::printWithPrefix),
                      "person: "));
}


int main()
{
    std::vector<Person> coll(5);
    foo(coll);

    std::vector<Person*> coll2;
    coll2.push_back(new Person);
    ptrfoo(coll2);
}

