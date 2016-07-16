#include \
    "component1.hh"

#include "component1.hh"
#include "component1.hh"
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
    int \
        x; //unused variable

    return 111;
}


void Bar::do_valgrind_errors(){
    /////  lets provoke some valgrind errors  /////

    // Memory leak (definitely lost)
    new float();

    // Condition depends on undefined value
    int a;
    if(a) a = a*a;

    // Invalid read
    int* ip = new int(0);
    delete ip;
    int i = *ip;

    // Invalid write
    ip = new int(0);
    delete ip;
    *ip = 1;

    // Invalid free
    ip = new int(0);
    delete ip;
    delete ip;

    // Mismatched free() / delete / delete []
    ip = new int(0);
    delete [] ip;
}
