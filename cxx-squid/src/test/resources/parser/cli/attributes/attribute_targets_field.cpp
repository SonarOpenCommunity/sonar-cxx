// attribute_targets_field.cpp
// compile with: /clr /c
using namespace System;
[AttributeUsage(AttributeTargets::Field)]
ref class Attr : public Attribute {};

ref struct MyStruct{
   [Attr] int Test;   // same as [field:Attr]
};
