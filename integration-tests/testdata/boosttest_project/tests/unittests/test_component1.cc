#include "stdafx.h"

#define BOOST_TEST_MODULE my_module
#include <boost/test/unit_test.hpp>

#include <component1.hh>

// Fixture
struct Component1Test {
   Bar bar;
};

BOOST_FIXTURE_TEST_CASE(foo_successfull, Component1Test) {
   usleep(50000);
   BOOST_CHECK_EQUAL(bar.foo(), 111);
}

BOOST_FIXTURE_TEST_CASE(foo_failing, Component1Test) {
   BOOST_CHECK_EQUAL(bar.foo(), 112);
}

BOOST_FIXTURE_TEST_CASE(foo_throwing, Component1Test) {
   throw "BOOM";
}
