#pragma once

#include <string>


class PathHandle
{
public:
    PathHandle();
    ~PathHandle();

    std::string CompinePaths(std::string path1, std::string path2);
};

