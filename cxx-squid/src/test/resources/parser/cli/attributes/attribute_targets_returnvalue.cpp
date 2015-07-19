// attribute_targets_returnvalue.cpp
// compile with: /clr /c
using namespace System;
[AttributeUsage(AttributeTargets::ReturnValue)]
ref class Attr : public Attribute {};

ref struct MyStruct {
   // Note required specifier
   [returnvalue:Attr] int Test() { return 0; }
};


