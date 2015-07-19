// attribute_targets_delegate.cpp
// compile with: /clr /c
using namespace System;
[AttributeUsage(AttributeTargets::Delegate)]
ref class Attr : public Attribute {};

// ToDo: make this work
[Attr] delegate void Test();
[delegate:Attr] /*delegate*/ void Test2();

