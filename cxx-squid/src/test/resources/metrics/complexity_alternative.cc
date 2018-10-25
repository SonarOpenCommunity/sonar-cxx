// same as complexity.cc but with alternative logical operators 'and' and 'or'
# include <iostream>
# include <exception>

using namespace std;

int main(){
    if(true)
        ;

    if(true and false)
        ;

    if(true or false)
        ;
    
    for(;;);
    
    while(true);
    
    try{
    }
    catch(std::exception e) {}
    catch(...) {}

    true ? 1 : 0;
    
    int i = 3;
    switch(i){
    case 2: break;
    case 3: break;
    default: break;
    }
}
