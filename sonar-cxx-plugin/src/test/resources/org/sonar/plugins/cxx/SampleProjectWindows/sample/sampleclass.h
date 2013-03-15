#pragma once

#include <string>

using namespace std;

class SampleClass
{
 public:
    SampleClass(string name, int age);
    int GetAge();
    string GetName();

 private:
    SampleClass() {}
    string name;
    int age;
};
