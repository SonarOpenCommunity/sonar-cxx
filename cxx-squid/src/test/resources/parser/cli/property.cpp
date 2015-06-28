// context_sensitive_keywords.cpp
// compile with: /clr
public ref class C {
   int MyInt;
public:
   C() : MyInt(99) {}

   property  int Property_Block {   // context-sensitive keyword
      int get() { return MyInt; };
    }
};

public ref class Xyz
{
private:
  int _x, _y;
  String ^_name;
public:
  property int X
  {
    int get()
    {
      return _x;
    }
    void set(int x)
    {
      _x = x;
    }
  }
  property String ^Name
  {
    void set(String ^N)
    {
      _name = N;
    }
    String ^get()
    {
      return _name;
    }
  }
};

public ref class Button : Control {
private:
  int caption;
public:
  property int Caption {
    int get() {
      return caption;
    }
    void set(int value) {
      if (caption != value) {
        caption = value;
      }
    }
  }
}

int main() {
//   int property = 0;               // variable name
   C ^ MyC = gcnew C();
   //property = MyC->Property_Block;
   //System::Console::WriteLine(++property);
}
