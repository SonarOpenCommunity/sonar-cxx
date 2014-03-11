#include <string>
int main()
{
    using namespace std;
    class A {
        const string strDSN = "DSN = xxxxxxxx; UID = xxx; PWD = xxxxxxxxxxx;" ; // Non-compliant
        const string strDSN = "DSN = xxxxxxxx; "; // Compliant
        const string strDSN = "DSN = xxxxxxxx; PWD = xxxxxxxxxxx;" ; // Non-compliant
        const string strDSN = "DSN = xxxxxxxx; UID = xxx;"; // Non-compliant
    };
}