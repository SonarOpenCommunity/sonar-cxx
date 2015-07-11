#define TEST_MACRO() \
	macro

#define EXPORT

class testClass
{
	void defaultMethod();

public:
	using aliasDeclaration = int;

	void publicMethod();
	
	enum classEnum {
		classEnumValue
	}

	enumVar;

	EXPORT int publicAttribute;
	 
	int inlineCommentedAttr; 

	void inlinePublicMethod();

	// ignore deleted methods
	A(A const&) = delete;

	// ignore defaulted methods
	A& operator=(A const&) = default;

	void publicDefinedMethod() { }
	 
protected:
	virtual void protectedMethod();
	 
private:
	void privateMethod();
	
	enum privateEnum {
		privateEnumVal
	};
	
	struct privateStruct {
		int privateStructField;
	};
	
	class privateClass {
		int i;
	};
	
	union privateUnion {
		int u;
	};
	
public:
	int inlineCommentedLastAttr;
};


struct testStruct {
    int testField;
};

extern int globalVar; 

void testFunction();

enum emptyEnum
{};

enum testEnum
{
  enum_val
};

union testUnion
{

};

template<T>
struct tmplStruct
{};

void func() {
  for (int i = 0; i < 10; i++) {}
}
