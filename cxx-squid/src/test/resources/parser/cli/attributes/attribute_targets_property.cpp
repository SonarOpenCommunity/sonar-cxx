// attribute_targets_property.cpp
// compile with: /clr /c
using namespace System;
[AttributeUsage(AttributeTargets::Property)]
ref class Attr : public Attribute {};


ref struct MyStruct{
   [Attr] property int Test;   // same as [property:Attr]
};
