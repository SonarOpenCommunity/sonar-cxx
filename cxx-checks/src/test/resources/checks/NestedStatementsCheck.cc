#define MACRO(x) if (true) { printf(x); }

class Abc {
  public:

    void compliant() {
      int i = 0;

      if (true) {
        for (int i = 0; i < 1; i++) {
          if (true) {
            if (true) {
              printf("Four levels deep; compliant.");
            }
          } else if (true) {
            switch (i) {
              case 0:
                printf("Five levels deep; compliant.");
                break;
              default:
                break;
            }
          } else if (true) {
            printf("Three levels deep; compliant.");
          } else if (true) {
            printf("Three levels deep; compliant.");
          } else {
            printf("Three levels deep; compliant.");
          }
        }
      }

      if (true) {
        printf("One level deep; compliant.");
      } else if (true) {
        printf("One level deep; compliant.");
      } else if (true) {
        printf("One level deep; compliant.");
      } else if (true) {
        printf("One level deep; compliant.");
      } else if (true) {
        printf("One level deep; compliant.");
      } else if (true) {
        printf("One level deep; compliant.");
      }
    }

    void noncompliant( ) {
      int i = 0;

      if (true) {
        for (int i = 0; i < 1; i++) {
          do {
            if (true) {
              printf("Four levels deep; compliant.");
            } else if (true) {
              printf("Four levels deep; compliant.");
            } else {
              switch (i) {
                case 0:
                  printf("Five levels deep; compliant.");
                  break;
                case 1:
                  if (true) {
                    printf("Six levels deep; non-compliant.");
                  }
                  break;
                default:
                  try {
                    printf("Six levels deep; non-compliant.");
                  } catch (Exception e) {
                    // pass
                  }
                  MACRO("Macro contain sixth level if, but shouldn't trigger; compiant");
                  break;
              }
            }
          } while (true);
        }
      }
    }
};
