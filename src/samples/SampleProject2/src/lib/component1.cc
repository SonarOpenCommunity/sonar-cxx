#include <iostream>

#include "component1.hh"

/**
 * Does something
 *
 * @return: an int indicating something
 */
int Bar::foo(){
    // single line comment
    
    /*
     * multi-line comment
     */
    int x; //unused variable
    
    //cout << "Bar::foo() called" << endl;

    /////  lets provoke some valgrind errors  /////
    new float(); //memleak
    
    // int a;
    // if(a)       //undefined behavior
    //     std::cout << "fired!!!" << std::endl;

    return 111;
}
