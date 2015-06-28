// keyword__finally.cpp
// compile with: /clr
using namespace System;

ref class MyException : public System::Exception {};

void ThrowMyException() {
  throw gcnew MyException;
}

int main() {
  try {
    ThrowMyException();
  }
  catch (MyException^ e) {
    Console::WriteLine("in catch");
    Console::WriteLine(e->GetType());
  }
  finally {
    Console::WriteLine("in finally");
  }
}
