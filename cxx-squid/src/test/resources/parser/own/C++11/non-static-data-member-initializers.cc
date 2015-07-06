class SomeClass {
public:
    SomeClass() {}
    explicit SomeClass(int new_value) : value(new_value) {}

private:
    int value = 5;
    MyWidget * m_myWidget = 0;
    double d = 1.0;
};
