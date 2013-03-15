#include "sampleclass.h"

SampleClass::SampleClass(string name, int age) : name(name), age(age)
{
}

int SampleClass::GetAge()
{
    return age;
}

string SampleClass::GetName()
{
    return name;
}
