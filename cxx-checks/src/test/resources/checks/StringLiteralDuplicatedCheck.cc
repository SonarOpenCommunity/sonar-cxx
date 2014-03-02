#include <stdio.h>
#include <tchar.h>
#include <string>
class A {
    //    @SupressWarnings("allall") // Compliant
    //    @SupressWarnings("allall")
    //    @SupressWarnings("allall")
    //    @SupressWarnings("aaaaa")
public:
    void f() {
        printf("aaaaa"); // Compliant
        printf("bbbbb"); // Non-Compliant
        printf("bbbbb");
        printf("ccccc"); // Non-Compliant
        printf("ccccc");
        printf("ccccc");
        printf("dddd"); // Compliant - too short
        printf("dddd");
    }

    const std::string ACTION_1 = "action1";  // Compliant

    void run() {
        prepare(ACTION_1);                               // Compliant
        execute(ACTION_1);
        release(ACTION_1);
    }
};
