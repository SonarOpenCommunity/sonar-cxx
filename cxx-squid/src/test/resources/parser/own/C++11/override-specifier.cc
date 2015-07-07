struct A
{
  virtual void foo();
  virtual void foo2();
  void bar();
};

struct B : A
{
  void foo() const override;    // Error: B::foo does not override A::foo
                                // (signature mismatch)
  void foo() override;          // OK: B::foo overrides A::foo
  virtual void foo2() override; // OK: B::foo overrides A::foo
  void bar() override;          // Error: A::bar is not virtual
};
