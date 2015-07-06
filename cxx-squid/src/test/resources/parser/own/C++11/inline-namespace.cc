namespace L {
    inline namespace M {
        inline namespace N {
            /*...*/ 
        }
    }
}

// if you compile the following code the output of the resulting executable is 1
namespace A {
    inline namespace B {
        int foo(bool) { return 1; }
    }
    int foo(int) { return 2; }
}

int main(void) {
    return A::foo(true);
}
