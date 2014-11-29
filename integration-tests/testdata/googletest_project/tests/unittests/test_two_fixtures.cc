#include <gtest/gtest.h>
#include <unistd.h>

namespace {
    class Fixture1 : public ::testing::Test {
    };

    // this will be in one testsuite-tag
    TEST_F(Fixture1, test_successfull) {
        usleep(50000);
        EXPECT_EQ(1, 1);
    }
    TEST_F(Fixture1, test_failing) {
        EXPECT_EQ(1, 12);
    }
    TEST_F(Fixture1, DISABLED_test_skipped) {
        ASSERT_EQ(1, 1);
    }

    class Fixture2 : public ::testing::Test {
    };

    // this will be in another testsuite-tag
    TEST(Fixture2, tc1_good) {
        ASSERT_EQ(1, 1);
    }
}
