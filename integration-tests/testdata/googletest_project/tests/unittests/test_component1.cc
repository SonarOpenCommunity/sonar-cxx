#include <gtest/gtest.h>
#include <unistd.h>
#include <component1.hh>

namespace {
    class Component1Test : public ::testing::Test {
    protected:
        Bar bar;
    };

    TEST_F(Component1Test, foo_successfull) {
        usleep(50000);
        EXPECT_EQ(bar.foo(), 111);
    }

    TEST_F(Component1Test, foo_failing) {
        EXPECT_EQ(bar.foo(), 112);
    }

    TEST_F(Component1Test, foo_throwing) {
        throw "BOOM";
    }

    TEST_F(Component1Test, DISABLED_foo_skipped) {
        ASSERT_EQ(1, 1);
    }
}
