#include <stdio.h>
#include <tchar.h>

int FooY() {
    int x = 0;
    switch(x)
    {
        case 0:
            break;
        default:
            break;
    }

    switch(x)
    {
    case 0: break;
    default: break;
    }

    switch(x)
    {
    case 0: 
        switch(x)
        {
        case 0: break;
        default: break;
        }

            break;
    default: break;
    }

    switch(x)
    {
    case 0: case 1: case 2: case 3: // can this made configurable?
        break; // says needs to be in 33 column
    default: break;
    }
};