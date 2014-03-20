#include <stdio.h>
#include <tchar.h>
class Program
{
    int _tmain(int argc, _TCHAR* argv[])
    {
        const long grossSalary = 80000;        // Compliant
        const double Rate = 0.85;               // Compliant
        double rate2 = 0.85;                    // Compliant
        int netSalary1;
        long long netSalary2;
        netSalary1 = Rate * grossSalary;          // Compliant
        netSalary2 = 0.85 * grossSalary;         // Non-Compliant
        netSalary1 = 0;                           // Compliant, exception
        netSalary1 = -1;                          // Compliant, exception
        netSalary1 = +1;                          // Compliant, exception
        netSalary1 = 0x0;                         // Compliant, exception
        netSalary1 = 0x00;                        // Compliant, exception
    }
};
enum Foo
{
    a = 0,                                        // Compliant
    b = 1,                                        // Compliant
    c = 2,                                        // Compliant
    d = 3,                                        // Compliant
    e = 4                                         // Compliant
};

/*
#define MY_NUM 1234

int someFunction()
{
    return MY_NUM;
}

*/
