

int main(){
    {
        [[[[ lalla error bla // should be consumed by the err. recovery of
             // on compound statement level
    }
    
    int i = 1;     // on simple declaration level: doesnt work
    switch(i){
    case 1:
        break;
    default:
        break;
    }
    
    //return lala 0;      //on jump statement level: doesnt work
    return 0;
}
