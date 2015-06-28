// attribute_targets_method.cpp
// compile with: /clr /c
using namespace System;
[AttributeUsage(AttributeTargets::Method)]
ref class Attr : public Attribute {};


ref struct MyStruct{
   [Attr] void Test(){}   // same as [method:Attr]
};
