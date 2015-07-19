// mcppv2_property_2.cpp
// compile with: /clr
using namespace System;
public ref class C {
  array<int>^ MyArr;

public:
  C() {
    MyArr = gcnew array<int>(5);
  }

  // default indexer
  property int default[int]{
    int get(int index) {
    return MyArr[index];
  }
  void set(int index, int value) {
    MyArr[index] = value;
  }
  }

    // user-defined indexer
    property int indexer1[int]{
    int get(int index) {
    return MyArr[index];
  }
  void set(int index, int value) {
    MyArr[index] = value;
  }
  }
};

int main() {
  C ^ MyC = gcnew C();

  // use the default indexer
  Console::Write("[ ");
  for (int i = 0; i < 5; i++) {
    MyC[i] = i;
    Console::Write("{0} ", MyC[i]);
  }

  Console::WriteLine("]");

  // use the user-defined indexer
  Console::Write("[ ");
  for (int i = 0; i < 5; i++) {
    MyC->indexer1[i] = i * 2;
    Console::Write("{0} ", MyC->indexer1[i]);
  }

  Console::WriteLine("]");
}