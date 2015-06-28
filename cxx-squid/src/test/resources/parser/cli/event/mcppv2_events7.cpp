// mcppv2_events7.cpp
// compile with: /clr
using namespace System;

public delegate void MyDel();
public delegate int MyDel2(int, float);

ref class EventSource {
public:
   static MyDel ^ psE;
   static event MyDel2 ^ E2;   // event keyword, compiler generates add, 
                               // remove, and Invoke

   static event MyDel ^ E {
      static void add(MyDel ^ p) {
         psE = static_cast<MyDel^> (Delegate::Combine(psE, p));
      }

      static void remove(MyDel^ p) {
         psE = static_cast<MyDel^> (Delegate::Remove(psE, p));
      }

      static void raise() {
         if (psE != nullptr)   //psE!=0 -> C2679, use nullptr
            psE->Invoke(); 
      }
   }

   static int Fire_E2(int i, float f) {
      return E2(i, f);
   }
};

public ref struct EventReceiver {
   void H1() {
      Console::WriteLine("In event handler H1");
   }

   int H2(int i, float f) {
      Console::WriteLine("In event handler H2 with args {0} and {1}", i.ToString(), f.ToString());
      return 0;
   }
};

int main() {
   EventSource^ pE = gcnew EventSource;
   EventReceiver^ pR = gcnew EventReceiver;
   
   // Called with "this"
   // hook event handlers
   pE->E += gcnew MyDel(pR, &EventReceiver::H1);
   pE->E2 += gcnew MyDel2(pR, &EventReceiver::H2);

   // raise events
   pE->E();
   pE->Fire_E2(11, 11.11);

   // unhook event handlers
   pE->E -= gcnew MyDel(pR, &EventReceiver::H1);
   pE->E2 -= gcnew MyDel2(pR, &EventReceiver::H2);

   // Not called with "this"
   // hook event handler
   EventSource::E += gcnew MyDel(pR, &EventReceiver::H1);
   EventSource::E2 += gcnew MyDel2(pR, &EventReceiver::H2);

   // raise events
   EventSource::E();
   EventSource::Fire_E2(22, 22.22);

   // unhook event handlers
   EventSource::E -= gcnew MyDel(pR, &EventReceiver::H1);
   EventSource::E2 -= gcnew MyDel2(pR, &EventReceiver::H2);
}
