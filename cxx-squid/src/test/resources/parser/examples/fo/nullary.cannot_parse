/* The following code example is taken from the book
 * "The C++ Standard Library - A Tutorial and Reference"
 * by Nicolai M. Josuttis, Addison-Wesley, 1999
 *
 * (C) Copyright Nicolai M. Josuttis 1999.
 * Permission to copy, use, modify, sell and distribute this software
 * is granted provided this copyright notice appears in all copies.
 * This software is provided "as is" without express or implied
 * warranty, and with no claim as to its suitability for any purpose.
 */
namespace boost {

/**********************************************************
 * type nullary_function
 * - as supplement to unary_function and binary_function
 **********************************************************/
template <class Result>
struct nullary_function {
    typedef Result result_type;
};

/**********************************************************
 * ptr_fun for functions with no argument
 **********************************************************/
template <class Result>
class pointer_to_nullary_function : public nullary_function<Result>
{
  protected:
    Result (*ptr)();
  public:
    pointer_to_nullary_function() {
    }
    explicit pointer_to_nullary_function(Result (*x)()) : ptr(x) {
    }
    Result operator()() const { 
        return ptr();
    }
};

template <class Result>
inline pointer_to_nullary_function<Result> ptr_fun(Result (*x)())
{
  return pointer_to_nullary_function<Result>(x);
}

}
