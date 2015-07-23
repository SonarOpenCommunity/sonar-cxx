#include <iostream>
#include <memory>
#include <vector>
#include <utility>
using namespace std;


//template <class T> ostream& operator<< (ostream& strm, const auto_ptr<T>& p);

//ostream& somefunc();
class sometype;
sometype& func();

template <class T> void func();
void foo(int a, int b);
int* var;
extern int* bar;
int var2;
long var3;
long long var4;
unsigned long var5;
std::vector<std::vector<int> > oldStyle;
vector<vector<int>> newStyle;
std::pair<const char*, std::vector<long int>> longNewStyle;

template<class T, class Compare = std::less<T>>
class list
{
   typedef unsigned char byte;
   using PatchMap = std::vector<std::pair<size_t, std::vector<byte>>>;
};

template <class T>
struct List
{
    List<T>(){}
    template <class U>
    List<T>(List<U> & rhs) {}
};

template <int i>
class X
{
} ;

template <class T>
class Y
{
    T data;
} ;

typedef unsigned int* B;

void issue_49 ()
{
  X<(1>2)> x2;
  Y<X<1>> x3;
  vector<pair<int,int>> s;
  List<unsigned*> ld;
  static_cast<List<B>>(ld);
  vector<vector<vector<int>>> vvvi;
}

template<bool C, class T, class F> struct if_log2_             : F {};
template<        class T, class F> struct if_log2_<true, T, F> : T {};
template<unsigned A, unsigned R = 0> struct log2
	: if_log2_<A == 1, std::integral_constant<int, R>, log2<A / 2, R + 1>> {};

void issue_666 ()
{
  X<log2<23>::value> x5;
}
