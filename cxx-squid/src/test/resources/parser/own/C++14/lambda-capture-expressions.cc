auto lambda = [value = 1] { return value; };

void example1()
{
  auto u = make_unique<some_type>( some, parameters ); // a unique_ptr is move-only
  go.run( [ u=move(u) ] { do_something_with( u ); } ); // move the unique_ptr into the lambda

  std::unique_ptr<int> ptr(new int(10));
  auto lambda = [value = std::move(ptr)] { return *value; };
}

void example2()
{
  int x = 4;
  int z = [&r = x, y = x+1] {
           r += 2;         // set x to 6; "R is for Renamed Ref"
           return y+2;     // return 7 to initialize z
          }();             // invoke lambda
}
