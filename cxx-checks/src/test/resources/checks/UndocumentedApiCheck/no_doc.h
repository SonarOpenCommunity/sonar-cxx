#define TEST_MACRO() \
	macro

#define EXPORT

class testClass
{
    void defaultMethod();

public:
    void publicMethod();

    enum classEnum {
        classEnumValue
    }

    enumVar;

    EXPORT int publicAttribute;

    int inlineCommentedAttr;

    void inlinePublicMethod() {};

    template <class T>
    void templateMethod();

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

enum testEnumWithType : int
{
    enum_val
};

enum class testScopedEnum
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

typedef int int32;

typedef struct
{
    int a;
    float b;
} typedefStruct;

typedef class
{
public:
    int a;
    float b;
} typedefClass;

typedef union
{
    int a;
    float b;
} typedefUnion;

typedef enum
{
    A,
    B
} typedefEnum;

typedef enum class
{
    A,
    B
} typedefEnumClass;

class OverrideInClassTest
{
    virtual void defaultMethod() override;
public:
    virtual void publicMethod() override;
protected:
    virtual void protectedMethod() override;
private:
    virtual void privateMethod() override;
};

struct OverrideInStructTest
{
    virtual void defaultMethod() override;
};

struct ComplexOverrideInStruct
{
    virtual AAA::BBB::CCC* method() const noexcept override;
};

class ComplexOverrideInClass
{
public:
    virtual AAA::BBB::CCC* method() const noexcept override;
};

using aliasDeclaration1 = void;

template <typename T> using aliasDeclaration2 = whatever<T>;

friend class friendClass;
template<typename S> friend S& operator<<(S&, A const&);

class ClassWithFriend {
public:
    template<typename S> friend S& operator<<(S&, A const&);
};
