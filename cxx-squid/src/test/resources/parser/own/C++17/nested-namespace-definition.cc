// Example:

namespace A::B::C {
    int i;
}

// The above has the same effect as:

namespace A {
    namespace B {
        namespace C {
            int i;
        }
    }
}
