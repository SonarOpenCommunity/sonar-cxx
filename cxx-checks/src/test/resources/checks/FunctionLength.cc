#include <stdarg.h>
struct foo {
  int x;
  double y;
};

int Xfive(void);
int Xsix(void);


int Xfive(void) {
  int lines = 1;
  lines++;
  //comments are not counted
  if (lines == 2) {
    lines+=2;
  }
  return lines+1;
}

int Ysix(void) {
  int lines = 1;
  lines++;
  if (lines == 2)
  {
    lines+=2;
  }
  int extra=2;
  return lines + extra;
}


