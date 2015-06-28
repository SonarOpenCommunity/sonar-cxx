// mcppv2_events8.cpp
// compile with: /LD /clr
using namespace System;

public delegate void Del(String^ s);

public ref class Source {
public:
   event Del^ Event;
   void Fire(String^ s) {
      Event(s);
   }
};