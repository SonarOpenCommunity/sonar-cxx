#pragma once

// Copyright (C) 2015 Guenter Wirth ETAS GmbH
//
//  Distributed under the Boost Software License, Version 1.0.
//  (See accompanying file LICENSE_1_0.txt or copy at 
//  http://www.boost.org/LICENSE_1_0.txt)
//
//  See http://www.boost.org/libs/test for the library home page.
//
// Description: header to mock boost unit test framework
//

#ifndef CXX_MOCK_UNIT_TEST_HPP
#define CXX_MOCK_UNIT_TEST_HPP

//____________________________________________________________________________//

#define BOOST_AUTO_TEST_SUITE(suite_name) namespace suite_name {
#define BOOST_AUTO_TEST_SUITE_END() }

//____________________________________________________________________________//

#define BOOST_AUTO_TEST_CASE(test_name) struct test_name { void test_method(); }; \
void test_name::test_method()

//____________________________________________________________________________//

#define BOOST_FIXTURE_TEST_CASE(test_name, F) struct test_name : public F { void test_method(); }; \
void test_name::test_method()

//____________________________________________________________________________//

#define BOOST_WARN( P )
#define BOOST_CHECK( P )
#define BOOST_REQUIRE( P )

#define BOOST_WARN_MESSAGE( P, M )
#define BOOST_CHECK_MESSAGE( P, M )
#define BOOST_REQUIRE_MESSAGE( P, M )

#define BOOST_ERROR( M )
#define BOOST_FAIL( M )

#define BOOST_CHECK_THROW_IMPL( S, E, P, prefix, TL )

#define BOOST_WARN_THROW( S, E )
#define BOOST_CHECK_THROW( S, E )
#define BOOST_REQUIRE_THROW( S, E )

#define BOOST_WARN_EXCEPTION( S, E, P )
#define BOOST_CHECK_EXCEPTION( S, E, P )
#define BOOST_REQUIRE_EXCEPTION( S, E, P )

#define BOOST_CHECK_NO_THROW_IMPL( S, TL )

#define BOOST_WARN_NO_THROW( S )
#define BOOST_CHECK_NO_THROW( S )
#define BOOST_REQUIRE_NO_THROW( S )

#define BOOST_WARN_EQUAL( L, R )
#define BOOST_CHECK_EQUAL( L, R )
#define BOOST_REQUIRE_EQUAL( L, R )

#define BOOST_WARN_NE( L, R )
#define BOOST_CHECK_NE( L, R )
#define BOOST_REQUIRE_NE( L, R )

#define BOOST_WARN_LT( L, R )
#define BOOST_CHECK_LT( L, R )
#define BOOST_REQUIRE_LT( L, R )

#define BOOST_WARN_LE( L, R )
#define BOOST_CHECK_LE( L, R )
#define BOOST_REQUIRE_LE( L, R )

#define BOOST_WARN_GT( L, R )
#define BOOST_CHECK_GT( L, R )
#define BOOST_REQUIRE_GT( L, R )

#define BOOST_WARN_GE( L, R )
#define BOOST_CHECK_GE( L, R )
#define BOOST_REQUIRE_GE( L, R )

#define BOOST_WARN_CLOSE( L, R, T )
#define BOOST_CHECK_CLOSE( L, R, T )
#define BOOST_REQUIRE_CLOSE( L, R, T )

#define BOOST_WARN_CLOSE_FRACTION( L, R, T )
#define BOOST_CHECK_CLOSE_FRACTION( L, R, T )
#define BOOST_REQUIRE_CLOSE_FRACTION( L, R, T )

#define BOOST_WARN_SMALL( FPV, T )
#define BOOST_CHECK_SMALL( FPV, T )
#define BOOST_REQUIRE_SMALL( FPV, T )

#define BOOST_WARN_PREDICATE( P, ARGS )
#define BOOST_CHECK_PREDICATE( P, ARGS )
#define BOOST_REQUIRE_PREDICATE( P, ARGS )

#define BOOST_EQUAL_COLLECTIONS_IMPL( L_begin, L_end, R_begin, R_end, TL )
#define BOOST_WARN_EQUAL_COLLECTIONS( L_begin, L_end, R_begin, R_end )
#define BOOST_CHECK_EQUAL_COLLECTIONS( L_begin, L_end, R_begin, R_end )
#define BOOST_REQUIRE_EQUAL_COLLECTIONS( L_begin, L_end, R_begin, R_end )

#define BOOST_BITWISE_EQUAL_IMPL( L, R, TL )

#define BOOST_WARN_BITWISE_EQUAL( L, R )
#define BOOST_CHECK_BITWISE_EQUAL( L, R )
#define BOOST_REQUIRE_BITWISE_EQUAL( L, R )

#define BOOST_IS_DEFINED( symb )

//____________________________________________________________________________//

#endif // CXX_MOCK_UNIT_TEST_HPP
