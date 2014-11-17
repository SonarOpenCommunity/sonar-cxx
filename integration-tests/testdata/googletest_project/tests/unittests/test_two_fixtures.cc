#include <gtest/gtest.h>
#include <unistd.h>

namespace {
    class Fixture : public ::testing::Test {
    };

    // this will be in one testsuite-tag
    TEST_F(Fixture, test_successfull) {
        usleep(50000);
        EXPECT_EQ(1, 1);
    }
    TEST_F(Fixture, test_failing) {
        EXPECT_EQ(1, 12);
    }
    TEST_F(Fixture, DISABLED_test_skipped) {
        ASSERT_EQ(1, 1);
    }


    // this will be in another testsuite-tag
    TEST(tc1, tc1_good) {
        ASSERT_EQ(1, 1);
    }
}
