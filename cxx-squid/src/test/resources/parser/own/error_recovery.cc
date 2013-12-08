

int main(){
    {
        [[[[ lalla error bla // should be consumed by the err. recovery of
             // on compound statement level
        {
            {aaaaaaaaaaaa}
        }
    }
    
    //intc c c c c c i = 1;     // on simple declaration level: doesnt work
    int i = 1;
           
    switch(i){
    case 1:
        break;
    default:
        break;
    }
    
    //return lala 0;      //on jump statement level: doesnt work
    return 0;
}
