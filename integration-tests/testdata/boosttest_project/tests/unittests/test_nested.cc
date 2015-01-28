#include "stdafx.h"

#define BOOST_TEST_MAIN
#include <boost/test/unit_test.hpp>

BOOST_AUTO_TEST_CASE(WithoutSuite)
{
   BOOST_REQUIRE(false);
}

BOOST_AUTO_TEST_SUITE(FirstLevel)

BOOST_AUTO_TEST_CASE(OnFirstLevel)
{
   BOOST_REQUIRE(false);
}

BOOST_AUTO_TEST_SUITE(SecondLevel)

BOOST_AUTO_TEST_CASE(OnSecondLevel)
{
   BOOST_REQUIRE(false);
}

BOOST_AUTO_TEST_SUITE(ThirdLevel)

BOOST_AUTO_TEST_CASE(OnThirdLevel)
{
   BOOST_REQUIRE(false);
}

BOOST_AUTO_TEST_SUITE_END()
BOOST_AUTO_TEST_SUITE_END()
BOOST_AUTO_TEST_SUITE_END()
