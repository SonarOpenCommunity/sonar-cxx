// SimpleProperties.cpp
// compile with: /clr
using namespace System;

ref class C {
public:
  property int Size;
};

int main() {
  C^ c = gcnew C;
  c->Size = 111;
  Console::WriteLine("c->Size = {0}", c->Size);
}
