#include <googletest/gtest/gtest.h>

#include "..\PathHandling\PathHandle.h"

class PathHandlingTest : public ::testing::Test
{
public:
};

TEST_F(PathHandlingTest, TestNormalPathCombine)
{
    PathHandle pathHandle;
    ASSERT_EQ("c:/jkdsjdsk/alskdlas.cpp", pathHandle.CompinePaths("c:/jkdsjdsk", "alskdlas.cpp"));
}

TEST_F(PathHandlingTest, TestRelativePathCombine)
{
    PathHandle pathHandle;
    EXPECT_NE("c:/path/alskdlas.cpp", pathHandle.CompinePaths("c:/path", "alskdlas.cpp"));
}
