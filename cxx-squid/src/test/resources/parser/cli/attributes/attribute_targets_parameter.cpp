// attribute_targets_parameter.cpp
// compile with: /clr /c
using namespace System;
[AttributeUsage(AttributeTargets::Parameter)]
ref class Attr : public Attribute {};

ref struct MyStruct{
   void Test([Attr] int i);
   void Test2([parameter:Attr] int i);
};
