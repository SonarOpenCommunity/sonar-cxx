# include <iostream>
# include <exception>

using namespace std;

// same as complexity.cc, but the whole implementation is extracted to the macro

#define IMPLEMENTATION()           \
    if(true)                       \
        ;                          \
                                   \
    if(true && false)              \
        ;                          \
                                   \
    if(true || false)              \
        ;                          \
                                   \
    for(;;);                       \
                                   \
    while(true);                   \
                                   \
    try{                           \
    }                              \
    catch(std::exception e) {}     \
    catch(...) {}                  \
                                   \
    true ? 1 : 0;                  \
                                   \
    int i = 3;                     \
    switch(i){                     \
    case 2: break;                 \
    case 3: break;                 \
    default: break;                \
    }                              \


int main(){                       // +1
   IMPLEMENTATION()               // macro was expanded but generated code was not considered as complexity source
}
