// Foo.h
namespace Foo
{
    void fooFunc( );
}

// Foo1.cpp
namespace Foo
{
    namespace
    {
        void privateFunction( )
        {
            int avalue { 0 } ;
        }
    }

    void fooFunc( )
    {
        privateFunction( );
    }
}

// Foo2.cpp
namespace
{
    void privateFunction( )
    {
        int xvalue { 1 };
    }
}

namespace Foo
{
    void fooFunc( )
    {
        privateFunction( );
    }
}
