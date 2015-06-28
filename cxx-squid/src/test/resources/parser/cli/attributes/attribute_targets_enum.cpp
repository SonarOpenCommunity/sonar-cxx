// attribute_targets_enum.cpp
// compile with: /clr /c
using namespace System;
[AttributeUsage(AttributeTargets::Enum)]
ref class Attr : public Attribute {};

[Attr]   // same as [enum:Attr]
enum struct MyEnum{e, d};
