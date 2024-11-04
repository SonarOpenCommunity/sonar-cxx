void foo(int p1) {}

class A {
    void foo(int p1, int p2) {};
};

namespace A {
    void foo(int p1) {}

    class A {
        void foo(int p1, int p2) {};
    };
}

namespace B {
    namespace A {
        void foo(int p1) {}

        class A {
            void foo(int p1, int p2) {};
        };
    }
}
