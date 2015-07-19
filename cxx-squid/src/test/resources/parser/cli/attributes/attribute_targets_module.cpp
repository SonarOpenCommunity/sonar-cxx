// attribute_targets_module.cpp
// compile with: /clr /c
using namespace System;
[AttributeUsage(AttributeTargets::Module)]
ref class Attr : public Attribute {};

[module:Attr];
