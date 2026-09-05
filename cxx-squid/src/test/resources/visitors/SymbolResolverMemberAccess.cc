struct Inner {
    int val;
};

struct Outer {
    int fld;
    Inner inner;
};

void useIt() {
    Outer s;
    int fld = 5;
    s.fld = 1;
    s.inner.val = 2;
}
