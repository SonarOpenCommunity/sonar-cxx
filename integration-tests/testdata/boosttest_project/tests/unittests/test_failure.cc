#include "stdafx.h"

#define BOOST_TEST_MODULE my_module
#include <boost/test/unit_test.hpp>

int add(int i, int j) { return i + j; }

BOOST_AUTO_TEST_SUITE(my_test_suite)

BOOST_AUTO_TEST_CASE(test_check)
{
   BOOST_CHECK(add(2, 2) == 3);         // #1 continues on error
}

BOOST_AUTO_TEST_CASE(test_require)
{
   BOOST_REQUIRE(add(2, 2) == 3);       // #2 throws on error
}

BOOST_AUTO_TEST_CASE(test_error)
{
   if (add(2, 2) != 3)
      BOOST_ERROR("Ouch...");           // #3 continues on error
}

BOOST_AUTO_TEST_CASE(test_fail)
{
   if (add(2, 2) != 3)
      BOOST_FAIL("Ouch...");            // #4 throws on error
}

BOOST_AUTO_TEST_CASE(test_exception)
{
   if (add(2, 2) != 3) throw "Ouch..."; // #5 throws on error
}

BOOST_AUTO_TEST_CASE(test_message)
{
   BOOST_CHECK_MESSAGE(add(2, 2) == 3,  // #6 continues on error
      "add(..) result: " << add(2, 2));
}

BOOST_AUTO_TEST_CASE(test_check_equal)
{
   BOOST_CHECK_EQUAL(add(2, 2), 3);     // #7 continues on error
}

BOOST_AUTO_TEST_SUITE_END()
