#include <stdio.h>

int main(void)
{
    class A {
        void f() {
            if (false) { // Compliant
            }

            if (false) { // Compliant
            }
            else {
            }

            if (false) { // Compliant
                if (false) { // Non-Compliant
                }
            }

            if (false) { // Compliant
                if (false) { // Compliant
                }
                printf("\n");
            }

            if (false) { // Compliant
                int a;
                if (a) { // Compliant
                }
            }

            if (false) { // Compliant
                if (false) { // Compliant
                }
            }
            else {
            }

            if (false) { // Compliant
                if (false) { // Compliant
                }
                else {
                }
            }

            if (false) { // Compliant
            }
            else if (false) { // Compliant
                if (false) { // Non-Compliant
                }
            }

            if (false) // Compliant
            if (true) { // Non-Compliant
            }

            if (false) { // Compliant
                while (true) {
                    if (true) { // Compliant
                    }
                }

                while (true)
                if (true) { // Compliant
                }
            }
        }

        void x()  {
            if (false) { // Compliant
            }
        }

        void x1( )  {
            if (getSomeValue()) { // Compliant
            }

        }
        void x2( )  {
            bool aValue = true;
            if (aValue) { // Compliant
            }

        }

        void declarations( )  {
            if (false) { // Compliant
                if (bool aValue = false) { // Compliant
                }
            }
            if (bool aValue = false) { // Compliant
                if (false) {           // Compliant
                }
            }
        }
    };
}
