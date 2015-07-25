// keyword__typeid_2.cpp
// compile with: /clr
using namespace System;
using namespace System::Security;
using namespace System::Security::Permissions;

typedef int ^ handle_to_int;
typedef int * pointer_to_int;

public ref class MyClass {};

class MyClass2 {};

[attribute(AttributeTargets::All)]
ref class AtClass {
public:
  AtClass(Type ^) {
    Console::WriteLine("in AtClass Type ^ constructor");
  }
};

[attribute(AttributeTargets::All)]
ref class AtClass2 {
public:
  AtClass2() {
    Console::WriteLine("in AtClass2 constructor");
  }
};

// Apply the AtClass and AtClass2 attributes to class B
[AtClass(MyClass::typeid), AtClass2]
[AttributeUsage(AttributeTargets::All)]
ref class B : Attribute {};

int main() {
  Type ^ MyType = B::typeid;

  Console::WriteLine(MyType->IsClass);

  array<Object^>^ MyArray = MyType->GetCustomAttributes(true);
  for (int i = 0; i < MyArray->Length; i++)
    Console::WriteLine(MyArray[i]);

  if (int::typeid != pointer_to_int::typeid)
    Console::WriteLine("int::typeid != pointer_to_int::typeid, as expected");

  if (int::typeid == handle_to_int::typeid)
    Console::WriteLine("int::typeid == handle_to_int::typeid, as expected");
}