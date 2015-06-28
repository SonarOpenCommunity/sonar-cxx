// attribute_targets_interface.cpp
// compile with: /clr /c
using namespace System;
[AttributeUsage(AttributeTargets::Interface)]
ref class Attr : public Attribute {};

[Attr]   // same as [event:Attr]
interface struct MyStruct{};
