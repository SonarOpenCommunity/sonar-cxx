#include <string>

using namespace std;
class A {
    const string ip = "0.0.0.0"; // Non-compliant
    const string url = "http://192.168.0.1/admin.html"; // Non-compliant
    const string  url2 = "http://www.example.org"; // Compliant
    int a = 42; // Compliant
    const string  notAnIp1 = "0.0.0.1234"; // Compliant
    const string  notAnIp2 = "1234.0.0.0"; // Compliant
    const string  notAnIp3 = "1234.0.0.0.0.1234"; // Compliant
    const string  notAnIp4 = ".0.0.0.0"; // Compliant
    char chBuffer[5120 * 4]; // Compliant
    const std::string  printerUID { "1.2.840.10008.5.1.1.16"}; // Compliant
    };

