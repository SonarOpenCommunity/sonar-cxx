class A{
public:
    int foo();
    int i;
};

int A::foo(){};

A* foo(){
    return new A();
}

A bar(){
    return A();
}

#define some \
    9weird \
    stuff

  #define other \
    9weird \
    stuff

  # define more \
    9weird \
    stuff

int main(int argc, char** argv)
{
    int i = 0;
    i -= 1;
    
    const char* lala = \
        "hello world";
    lala = "hello"
        "evil"
        "world";
    
    return foo()->i;
}
