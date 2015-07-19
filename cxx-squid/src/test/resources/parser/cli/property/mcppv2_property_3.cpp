// mcppv2_property_3.cpp
// compile with: /clr
using namespace System;

ref class StaticProperties {
  static int MyInt;
  static int MyInt2;

public:
  static property int Static_Data_Member_Property;

  static property int Static_Block_Property {
    int get() {
      return MyInt;
    }

    void set(int value) {
      MyInt = value;
    }
  }
};

int main() {
  StaticProperties::Static_Data_Member_Property = 96;
  Console::WriteLine(StaticProperties::Static_Data_Member_Property);

  StaticProperties::Static_Block_Property = 47;
  Console::WriteLine(StaticProperties::Static_Block_Property);
}