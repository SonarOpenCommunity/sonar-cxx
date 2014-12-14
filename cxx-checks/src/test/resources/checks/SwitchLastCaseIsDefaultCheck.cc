class A {
  public:
    void f( ) {
      int var = 0;

      switch (var) {
        case '0':
          printf("Missing default");
          break;
      }

      switch (var) {
        default:
          printf("Wrong position");
          break;
        case '0':
          printf("Has default not in last position");
          break;
      }

      switch (var) {
        case '0':
          printf("Has default in last position");
          break;
        default:
          printf("Compliant");
          break;
      }

      switch (var) {
        case '0':
          int var2 = 1;
          switch (var2) {
            case '0':
              printf("Missing default (nested)");
              break;
          }
        default:
          printf("Compliant");
          break;
      }

      switch (var) {
        case '0':
          printf("Missing default");
          int var2 = 1;
          switch (var2) {
            default:
              printf("Wrong position");
              break;
            case '0':
              printf("Has default not in last position");
              break;
          }
      }
    }
};
