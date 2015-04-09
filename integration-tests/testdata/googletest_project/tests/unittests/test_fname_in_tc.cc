#include <gtest/gtest.h>

TEST(TS, test_successfull) {
    RecordProperty("filename", "tests/unittests/"__FILE__);
    EXPECT_EQ(1, 1);
}
