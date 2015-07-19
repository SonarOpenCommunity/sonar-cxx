// mcppv2_events4.cpp
// compile with: /clr
using namespace System;
#include <stdio.h>

delegate void ClickEventHandler(int, double);
delegate void DblClickEventHandler(String^);

ref class EventSource {
public:
   event ClickEventHandler^ OnClick;
   event DblClickEventHandler^ OnDblClick;

   void FireEvents() {
      OnClick(7, 3.14159);
      OnDblClick("Started");
   }
};

ref struct EventReceiver {
public:
   void Handler1(int x, double y) {
      System::Console::Write("Click(x={0},y={1})\n", x, y);
   };

   void Handler2(String^ s) {
      System::Console::Write("DblClick(s={0})\n", s);
   }

   void Handler3(String^ s) {
      System::Console::WriteLine("DblClickAgain(s={0})\n", s);
   }

   void AddHandlers(EventSource^ pES) {
      pES->OnClick += 
         gcnew ClickEventHandler(this,&EventReceiver::Handler1);
      pES->OnDblClick += 
         gcnew DblClickEventHandler(this,&EventReceiver::Handler2);
      pES->OnDblClick += 
         gcnew DblClickEventHandler(this, &EventReceiver::Handler3);
   }

   void RemoveHandlers(EventSource^ pES) {
      pES->OnClick -= 
         gcnew ClickEventHandler(this, &EventReceiver::Handler1);
      pES->OnDblClick -= 
         gcnew DblClickEventHandler(this, &EventReceiver::Handler2);
      pES->OnDblClick -= 
         gcnew DblClickEventHandler(this, &EventReceiver::Handler3);
   }
};

int main() {
   EventSource^ pES = gcnew EventSource;
   EventReceiver^ pER = gcnew EventReceiver;

   // add handlers
   pER->AddHandlers(pES);
   
   pES->FireEvents();

   // remove handlers
   pER->RemoveHandlers(pES);
}