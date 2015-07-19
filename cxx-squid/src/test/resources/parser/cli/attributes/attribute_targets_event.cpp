// attribute_targets_event.cpp
// compile with: /clr /c
using namespace System;
[AttributeUsage(AttributeTargets::Event)]
ref class Attr : public Attribute {};

delegate void ClickEventHandler(int, double);

ref struct MyStruct{

   [Attr] event ClickEventHandler^ OnClick;   // same as [event:Attr]
};
