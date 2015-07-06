struct Base1 final { };
struct Derived1 : Base1 { }; // ill-formed because the class Base1 has been marked final

struct A
{
  virtual void foo() final; // A::foo is final
  void bar() final; // Error: non-virtual function cannot be final
};

struct B final : A // struct B is final
{
  void foo(); // Error: foo cannot be overridden as it's final in A
};

struct C : B // Error: B is final
{
};
