#include <string>
#include <ios>
#include <type_traits>

int doSomething();
int doSomethingElse();
bool condition = true;
#define TEST(a,b) if (a) {    \
                  } if (b) {  \
                  }
#define TEST2() doSomething();
// without alias

class TooManyStatementsPerLine {
    int a; int b; // OK - not a statement
    void myfunc() {
        doSomething(); doSomethingElse(); // NOK
        ; // OK
        if (a) {  } // OK
        if (a) {  } if (b) {  } // NOK
        while (condition); // OK
        TEST(a,b) // OK
        if (a) {  } TEST(a,b) // NOK
    label: while (condition) { // OK
            break; // OK
        }
        int a = 0; a++; // NOK
        switch(x) {
        case 0: x++; break; //(N)OK, depending on parameters
        case 1:
            x++; break;     //NOK
        }
    }
};

// alias shall not be considered
// http://en.cppreference.com/w/cpp/language/type_alias

// type alias, identical to
// typedef std::ios_base::fmtflags flags;
using flags = typename std::ios_base::fmtflags;
// the name 'flags' now denotes a type:
flags fl = std::ios_base::dec;

// type alias, identical to
// typedef void (*func)(int, int);
using func = void(*) (int, int);
// the name 'func' now denotes a pointer to function:
void example(int, int) {}
func fn = example;

// template type alias
template<class T> using ptr = T*;
// the name 'ptr<T>' is now an alias for pointer to T
ptr<int> x;

// type alias used to hide a template parameter 
template <class CharT> using mystring =
std::basic_string<CharT, std::char_traits<CharT>>;
mystring<char> str;

// type alias can introduce a member typedef name
template<typename T>
struct Container {
	using value_type = T;
};
// which can be used in generic programming
template<typename Container>
void fn2(const Container& c)
{
	typename Container::value_type n;
}

// type alias used to simplify the syntax of std::enable_if
template <typename T> using Invoke =
typename T::type;
template <typename Condition> using EnableIf =
Invoke<std::enable_if<Condition::value>>;
template <typename T, typename = EnableIf<std::is_polymorphic<T>>>
int fpoly_only(T t) { return 1; }

struct S { virtual ~S() {} };
int main()
{
	Container<int> c;
	fn2(c); // Container::value_type will be int in this function

			//    fpoly_only(c); // error, enable_if prohibits this
	S s;
	fpoly_only(s); // okay, enable_if allows this
	TEST2(); // OK - empty statement after generated code
}

template <typename RBM, typename Trainer>
void update_normal(RBM& rbm, Trainer& t) {
    using rbm_t = RBM; // OK
}

void testme()
{
// empty expression statement should not create an issue
if(parameterSet->GetParameter<unsigned int>("OutputWidth", m_OutputSize0)); 
}
