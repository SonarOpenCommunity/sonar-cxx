#include <gtest/gtest.h>

class Fixture2 : public ::testing::Test {
protected:
    static void SetUpTestCase() {
        RecordProperty("filename", "tests/unittests/"__FILE__);
    }
};

TEST_F(Fixture2, tc1_good) {
    ASSERT_EQ(1, 1);
}
