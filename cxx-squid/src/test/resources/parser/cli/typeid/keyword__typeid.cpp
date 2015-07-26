// keyword__typeid.cpp
// compile with: /clr
using namespace System;

ref struct G {
  int i;
};

int main() {
  G ^ pG = gcnew G;
  Type ^ pType = pG->GetType();
  Type ^ pType2 = G::typeid;

  if (pType == pType2)
    Console::WriteLine("typeid and GetType returned the same System::Type");
  Console::WriteLine(G::typeid);

  typedef float* FloatPtr;
  Console::WriteLine(FloatPtr::typeid);
}
