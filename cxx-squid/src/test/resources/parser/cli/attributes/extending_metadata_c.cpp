// extending_metadata_c.cpp
// compile with: /clr /c
using namespace System;
[AttributeUsage(AttributeTargets::Class)]
ref class MyAttr : public Attribute {
public:
   MyAttr() {}
   MyAttr(int i) {}
   property int Priority;
   property int Version;
};

[MyAttr] 
ref class ClassA {};   // No arguments

[MyAttr(Priority = 1)] 
ref class ClassB {};   // Named argument

[MyAttr(123)] 
ref class ClassC {};   // Positional argument

[MyAttr(123, Version = 1)] 
ref class ClassD {};   // Positional and named
