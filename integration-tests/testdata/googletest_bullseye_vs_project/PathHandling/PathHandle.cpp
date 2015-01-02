#include "PathHandle.h"


PathHandle::PathHandle()
{
}


PathHandle::~PathHandle()
{
}

std::string PathHandle::CompinePaths(std::string path1, std::string path2)
{
    char *pFile = getenv("TEAMCITY_PROJECT_NAME");

    int i = 0;
    char ch = path1.back();

    if(ch == '/')
    {
        return path1.append(path2);
    }

    return path1.append("/").append(path2);
}
