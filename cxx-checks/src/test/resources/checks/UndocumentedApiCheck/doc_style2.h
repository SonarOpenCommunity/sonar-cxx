#define TEST_MACRO() \
	macro

#define EXPORT

/** doc */
class testClass
{
    void defaultMethod();

public:

    void publicMethod(); ///< doc

    /** doc */
    enum classEnum {
        classEnumValue ///< doc
    }

    enumVar;

    /** doc */
    EXPORT int publicAttribute;

    int inlineCommentedAttr;  ///< doc

    /** doc */
    void inlinePublicMethod() {};

    /** doc */
    template <class T>
    void templateMethod();

    int attr1, ///< doc 
        attr2; ///< doc

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
    int testField; ///< doc
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
    enum_val ///< doc
};

/** doc */
enum testEnumWithType : int
{
    enum_val ///< doc
};

/** doc */
enum class testScopedEnum
{
    enum_val ///< doc
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
    int a; ///< doc
    float b; ///< doc
} typedefStruct;

/** doc */
typedef class
{
public:
    int a; ///< doc
    float b; ///< doc
} typedefClass;

/** doc */
typedef union
{
    int a; ///< doc
    float b; ///< doc
} typedefUnion;

/** doc */
typedef enum
{
    A, ///< doc
    B ///< doc
} typedefEnum;

/** doc */
typedef enum class
{
    A, ///< doc
    B ///< doc
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
