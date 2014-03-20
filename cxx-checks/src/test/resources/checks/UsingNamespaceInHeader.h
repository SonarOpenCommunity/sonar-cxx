// Foo.h
namespace Foo
{
    void fooFunc( );
}

using namespace std;

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
