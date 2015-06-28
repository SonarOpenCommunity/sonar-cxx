// attribute_targets_struct.cpp
// compile with: /clr /c
using namespace System;
[AttributeUsage(AttributeTargets::Struct)]
ref class Attr : public Attribute {};

[Attr]   // same as [struct:Attr]
value struct MyStruct{};
