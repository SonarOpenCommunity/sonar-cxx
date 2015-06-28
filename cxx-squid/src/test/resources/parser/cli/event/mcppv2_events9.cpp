// mcppv2_events9.cpp
// compile with: /clr
#using "mcppv2_events8.dll"
using namespace System;

ref class Receiver {
public:
   void Handler(String^ s) {
      Console::WriteLine(s);
   }
};

int main() {
   Source^ src = gcnew Source;
   Receiver^ rc1 = gcnew Receiver;
   Receiver^ rc2 = gcnew Receiver;
   src -> Event += gcnew Del(rc1, &Receiver::Handler);
   src -> Event += gcnew Del(rc2, &Receiver::Handler);
   src->Fire("hello");
   src -> Event -= gcnew Del(rc1, &Receiver::Handler);
   src -> Event -= gcnew Del(rc2, &Receiver::Handler);
}