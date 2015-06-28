
// C++/CLI
ref class MyClass
{
public:
  MyClass();  // constructor
  ~MyClass(); // (deterministic) destructor (implemented as IDisposable.Dispose())
protected:
  !MyClass(); // finalizer (non-deterministic destructor) (implemented as Finalize())

public:
  static void Test()
  {
    MyClass automatic; // Not a handle, no initialization: compiler calls constructor here

    MyClass ^user = gcnew MyClass();
    delete user;

    // Compiler calls automatic's destructor when automatic goes out of scope
  }
};


int main()
{
  array<String^> ^arr = gcnew array<String^>(10);
  int i = 0;

  for each(String^% s in arr)
    s = i++.ToString();

  return 0;
}
