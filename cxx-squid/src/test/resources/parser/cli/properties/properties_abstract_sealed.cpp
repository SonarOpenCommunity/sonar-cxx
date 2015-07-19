// properties_abstract_sealed.cpp
// compile with: /clr
ref struct A {
protected:
  int m_i;

public:
  A() { m_i = 87; }

  // define abstract property
  property int Prop_1 {
    virtual int get() abstract;
    virtual void set(int i) abstract;
  }
};

ref struct B : A {
private:
  int m_i;

public:
  B() { m_i = 86; }

  // implement abstract property
  property int Prop_1 {
    virtual int get() override { return m_i; }
    virtual void set(int i) override { m_i = i; }
  }
};

ref struct C {
private:
  int m_i;

public:
  C() { m_i = 87; }

  // define sealed property
  property int Prop_2 {
    virtual int get() sealed { return m_i; }
    virtual void set(int i) sealed { m_i = i; };
  }
};

int main() {
  B b1;
  // call implementation of abstract property
  System::Console::WriteLine(b1.Prop_1);

  C c1;
  // call sealed property
  System::Console::WriteLine(c1.Prop_2);
}