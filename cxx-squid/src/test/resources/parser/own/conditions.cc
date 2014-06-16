int main(){
    int i = 1;
    switch(i){
    case 1:
        break;
    // EXTENSION: gcc's case range
    case 2 ... 3:
        break;
    default:
        break;
    }
    
    return 0;
}
