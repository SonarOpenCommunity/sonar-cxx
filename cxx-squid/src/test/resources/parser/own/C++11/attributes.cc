int [[attr1]] i [[attr2, attr3]];
int [[attr4]] a [10] [[attr5]];

[[ noreturn ]] void f() {
    throw "error";
}

[[attr1]] void [[attr2]] func(int [[attr3]] p) [[attr4]]
{
    [[attr4(arg1, arg2)]] if (cond)
    {
        [[vendor::attr5]] return i;
    }
}

[[attr1]] class C [[attr2]]
{

   C::C [[attr6]] () [[attr7]];

} [[attr3]] c [[attr4]], d [[attr5]];
// attr1 applies to declarator-ids c, d
// attr2 applies to the definition of class C
// attr3 applies to type C
// attr4 applies to declarator-id c
// attr5 applies to declarator-id d

[[attr1]] int [[attr2]] * [[attr3]] ( * [[attr4]] * [[attr5]] f [[attr6]] ) ( ) [[attr7]], e[[attr8]];
// attr1 applies to the pointer-to-pointer to function f, and to e
// attr2 applies to the return type of int
// attr3 applies to the return type *
// attr4 applies to the first * in the pointer-to-pointer to f
// attr5 applies to the second * in the pointer-to-pointer to f
// attr6 applies to the function variable f
// attr7 applies to the function (**f)()
// attr8 applies to e
