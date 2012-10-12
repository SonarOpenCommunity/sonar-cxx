# include <iostream>
# include <exception>

using namespace std;

int main(){
    if(true)
        ;

    if(true && false)
        ;

    if(true || false)
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
