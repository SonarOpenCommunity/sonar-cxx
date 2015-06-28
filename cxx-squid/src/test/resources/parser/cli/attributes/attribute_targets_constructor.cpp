// attribute_targets_constructor.cpp
// compile with: /clr /c
using namespace System;
[AttributeUsage(AttributeTargets::Constructor)]
ref class Attr : public Attribute {};

ref struct MyStruct{
   [Attr] MyStruct(){}   // same as [constructor:Attr]
};
