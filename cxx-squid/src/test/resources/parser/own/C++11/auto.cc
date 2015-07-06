auto i = 0; // i is an 'int'
const auto d = 0.0; // d is a 'const double'
auto b = bind( &func, _1, _2, 13 ); // 'b' cannot (portably) be declared!

auto test()
{
    for ( auto it = vec.begin() ; it != vec.end() ; ++it ) {}
    return true;
}
