#define TEST_MACRO() \
	macro

#define EXPORT

/** doc */
class testClass
{
    void defaultMethod();

public:
    /** doc */
    void publicMethod();

    /** doc */
    enum classEnum {
        /** doc */
        classEnumValue
    }

    enumVar;

    /** doc */
    EXPORT int publicAttribute;

    /** doc */
    int inlineCommentedAttr;

    /** doc */
    void inlinePublicMethod() {};

    /** doc */
    template <class T>
    void templateMethod();

protected:
    /** doc */
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
    /** doc */
    int inlineCommentedLastAttr;
};


/** doc */
struct testStruct {
    /** doc */
    int testField;
};

/** doc */
extern int globalVar;

/** doc */
void testFunction();

/** doc */
enum emptyEnum
{};

/** doc */
enum testEnum
{
    /** doc */
    enum_val
};

/** doc */
enum testEnumWithType : int
{
    /** doc */
    enum_val
};

/** doc */
enum class testScopedEnum
{
    /** doc */
    enum_val
};

/** doc */
union testUnion
{

};

/** doc */
template<T>
struct tmplStruct
{};

/** doc */
void func() {
    for (int i = 0; i < 10; i++) {}
}

/** doc */
typedef int int32;

/** doc */
typedef struct
{
    /** doc */
    int a;
    /** doc */
    float b;
} typedefStruct;

/** doc */
typedef class
{
public:
    /** doc */
    int a;
    /** doc */
    float b;
} typedefClass;

/** doc */
typedef union
{
    /** doc */
    int a;
    /** doc */
    float b;
} typedefUnion;

/** doc */
typedef enum
{
    /** doc */
    A,
    /** doc */
    B
} typedefEnum;

/** doc */
typedef enum class
{
    /** doc */
    A,
    /** doc */
    B
} typedefEnumClass;

/** doc */
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

/** doc */
struct OverrideInStructTest
{
    virtual void defaultMethod() override;
};

/** doc */
struct ComplexOverrideInStruct
{
    virtual AAA::BBB::CCC* method() const noexcept override;
};

/** doc */
class ComplexOverrideInClass
{
public:
    virtual AAA::BBB::CCC* method() const noexcept override;
};

/** doc */
using aliasDeclaration1 = void;

/** doc */
template <typename T> using aliasDeclaration2 = whatever<T>;

friend class friendClass;
template<typename S> friend S& operator<<(S&, A const&);

/** doc */
class ClassWithFriend {
public:
    template<typename S> friend S& operator<<(S&, A const&);
};
