#include "stdafx.h"

#define BOOST_TEST_MODULE my_module
#include <boost/test/unit_test.hpp>

int add(int i, int j) { return i + j; }

BOOST_AUTO_TEST_CASE(my_test)
{
   BOOST_CHECK(add(2, 2) == 4);
}
