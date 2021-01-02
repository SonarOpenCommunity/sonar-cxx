// generic lambda, operator() is a template with two parameters
auto glambda = []<class T>(T a, auto&& b) { return a < b; };
 
// generic lambda, operator() is a template with one parameter pack
auto f = []<typename ...Ts>(Ts&& ...ts) {
   return foo(std::forward<Ts>(ts)...);
};
