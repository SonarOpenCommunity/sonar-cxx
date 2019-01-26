// Foo.h
#include <string>
#include <ios>
#include <type_traits>

namespace Foo
{
    void fooFunc( );
}

using namespace std; // error

// alias for types or template shall not be detected
// see more details http://en.cppreference.com/w/cpp/language/type_alias
using flags = std::ios_base::fmtflags;

// Foo1.cpp
namespace Foo
{
    void fooFunc( )
    {
        privateFunction( );
    }
}

namespace Foo
{
    void fooFunc( )
    {
        privateFunction( );
    }
}

void fooFunc()
{
    using namespace std;
}

namespace X
{
    using namespace std;
};
