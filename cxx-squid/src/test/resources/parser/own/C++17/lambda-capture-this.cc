struct my_struct {
  int x;
  void value();
};

void my_struct::value() {
  // Capture *this: This will allow the lambda expression to capture the enclosing object by copy.
  // This will make possible to use safely the lambda expression even after the enclosing object has been destroyed.
  [=, *this](){};
}
