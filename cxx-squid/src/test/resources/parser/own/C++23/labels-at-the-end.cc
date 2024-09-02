void foo()
{
 first: // allowed in C++, now also allowed in C
    int x;

 second: // allowed in both C++ and C
    x = 1;

 last: // C++23: now also allowed in C++
}
