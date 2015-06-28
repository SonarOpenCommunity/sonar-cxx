// attribute_targets_all.cpp
// compile with: /clr /c
using namespace System;
[AttributeUsage(AttributeTargets::All)]
ref class Attr : public Attribute {};

[assembly:Attr];
