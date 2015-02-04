#include <gtest/gtest.h>

class Fixture2 : public ::testing::Test {
protected:
    static void SetUpTestCase() {
        // put invalid filename here in. The test will run successfully,
        // because the filename in the testcase has higher precedence
        RecordProperty("filename", "trash");
    }
};

TEST_F(Fixture2, tc1_good) {
    RecordProperty("filename", "tests/unittests/"__FILE__);
    ASSERT_EQ(1, 1);
}
