#include <stdio.h>
#include <string.h>

int main(int argc, char* argv[])
{
  char* str = NULL;
  if(argc) {
     str = "This is a sample string";
  }
  char* pch = strchr(str, 's');
  return 0;
}
