// mcppv2_events10.cpp
// compile with: /clr /W1
using namespace System;
public delegate void Del();
public delegate void Del2(String^ s);

interface struct IEvent {
public:
   // in this case, no raised method is defined
   event Del^ Event1;

   event Del2^ Event2 {
   public:
      void add(Del2^ _d);
      void remove(Del2^ _d);
      void raise(String^ s);
   }

   void fire();
};

ref class EventSource: public IEvent {
public:
   virtual event Del^ Event1;
   event Del2^ Event2 {
      virtual void add(Del2^ _d) {
         d = safe_cast<Del2^>(System::Delegate::Combine(d, _d));
      }

      virtual void remove(Del2^ _d) {
         d = safe_cast<Del2^>(System::Delegate::Remove(d, _d));
      }

      virtual void raise(String^ s) {
         if (d) {
            d->Invoke(s);
         }
      }
   }

   virtual void fire() {
      return Event1();
   }

private:
   Del2^ d;
};

ref class EventReceiver {
public:
   void func() {
      Console::WriteLine("hi");
   }

   void func(String^ str) {
      Console::WriteLine(str);
   }
};

int main () {
   IEvent^ es = gcnew EventSource;
   EventReceiver^ er = gcnew EventReceiver;
   es->Event1 += gcnew Del(er, &EventReceiver::func);
   es->Event2 += gcnew Del2(er, &EventReceiver::func);
   
   es->fire();
   es->Event2("hello from Event2");
   es->Event1 -= gcnew Del(er, &EventReceiver::func);
   es->Event2 -= gcnew Del2(er, &EventReceiver::func);
   es->Event2("hello from Event2");
}
