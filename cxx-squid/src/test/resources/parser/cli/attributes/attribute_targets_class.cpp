// attribute_targets_class.cpp
// compile with: /clr /c
using namespace System;
[AttributeUsage(AttributeTargets::Class)]
ref class Attr : public System::Attribute {};

[Attr]   // same as [class:Attr]
ref class MyClass {};
