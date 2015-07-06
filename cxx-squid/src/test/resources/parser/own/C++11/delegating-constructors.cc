struct Class : public Base
{
    unsigned char x;
    unsigned char y;

    Class ( int x )
        : Base ( 123 ), // initialize base class
        x ( x ),      // x (member) is initialized with x (parameter)
        y { 0 }       // y initialized to 0
    {}                // empty compound statement

    Class ( double a )
        : y ( a+1 ),
        x ( y ) // x will be initialized before y, its value here is indeterminate
    {} // base class constructor does not appear in the list, it is
    // default-initialized (not the same as if Base() were used, which is value-init)

    Class()
        try // function-try block begins before the function body, which includes init list
        : Base ( 789 ),
        x ( 0 ),
        y ( 0 )
    {
        // ...
    }
    catch (...)
    {
        // exception occurred on initialization
    }
};
