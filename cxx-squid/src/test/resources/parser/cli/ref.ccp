// tracking_reference_handles.cpp
// compile with: /clr
using namespace System;

ref struct City {
private:
   Int16 zip_;

public:
   City (int zip) : zip_(zip) {};
   property Int16 zip {
      Int16 get(void) {
         return zip_;
      }   // get
   }   // property
};

void passByRef (City ^% myCity) {
   // cast required so this pointer in City struct is "const City"
   if (myCity->zip == 20100)
      Console::WriteLine("zip == 20100");
   else
      Console::WriteLine("zip != 20100");
}

ref class G {
public:
   int i;
};

void Test(int % i) {
   i++;
}

int main() {
   G ^ g1 = gcnew G;
   G ^% g2 = g1;
   g1 -> i = 12;

   Test(g2->i);   // g2->i will be changed in Test2()

   City ^ Milano = gcnew City(20100);
   passByRef(Milano);
}
