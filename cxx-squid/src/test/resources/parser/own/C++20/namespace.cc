namespace A::B::inline C { /*...*/ }
// is equivalent to
namespace A::B { inline namespace C { /*...*/ } }

// inline may appear in front of every namespace name except the first:
namespace A::inline B::C {}
// is equivalent to
namespace A { inline namespace B { namespace C {} } }
