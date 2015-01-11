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

