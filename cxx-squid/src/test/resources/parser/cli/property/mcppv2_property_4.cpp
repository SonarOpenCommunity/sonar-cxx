// mcppv2_property_4.cpp
// compile with: /clr
using namespace System;
interface struct IEFace {
public:
  property int VirtualProperty1;
  property int VirtualProperty2 {
    int get();
    void set(int i);
  }
};

// implement virtual events
ref class PropImpl : public IEFace {
  int MyInt;
public:
  virtual property int VirtualProperty1;

  virtual property int VirtualProperty2 {
    int get() {
      return MyInt;
    }
    void set(int i) {
      MyInt = i;
    }
  }
};

int main() {
  PropImpl ^ MyPI = gcnew PropImpl();
  MyPI->VirtualProperty1 = 93;
  Console::WriteLine(MyPI->VirtualProperty1);

  MyPI->VirtualProperty2 = 43;
  Console::WriteLine(MyPI->VirtualProperty2);
}