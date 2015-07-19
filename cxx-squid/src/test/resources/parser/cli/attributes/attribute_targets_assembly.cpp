// attribute_targets_assembly.cpp
// compile with: /clr /c
using namespace System;
[AttributeUsage(AttributeTargets::Assembly)]
ref class Attr : public Attribute {};

[assembly:Attr];
