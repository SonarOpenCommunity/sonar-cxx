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
    case 0: case 1: case 2: case 3:
        break;
    default: break;
    }
};