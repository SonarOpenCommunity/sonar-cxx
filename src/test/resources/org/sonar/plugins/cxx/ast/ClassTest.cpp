
class MyClass
{
public:
  void  setValue(int value);
  int   getValue();
  
  int   publicMember;

protected:
  int   sanityCheck();
  
  int   protectedMember;

private:
  void init();
  bool isValid();  
  
  float privateMember;
};