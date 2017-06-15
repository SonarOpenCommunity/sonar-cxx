#include <stdio.h>
#include <tchar.h>
#include <string>
#define MACRO(ex) printf(#ex)
#define CONCAT_MACRO(ex) printf("eee" #ex "eee")
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
        MACRO(printf()); // Compliant
        MACRO(printf());
        CONCAT_MACRO(printf()); // Compliant
        CONCAT_MACRO(printf());
        printf("ee" "eee"); // Non-Compliant
        printf("ee" "eee");
    }

    const std::string ACTION_1 = "action1";  // Compliant

    void run() {
        prepare(ACTION_1);                               // Compliant
        execute(ACTION_1);
        release(ACTION_1);
    }
    
    void ptrnull() {
        char *ptr  = nullptr;
        char *ptr2 = nullptr;
    }
};
