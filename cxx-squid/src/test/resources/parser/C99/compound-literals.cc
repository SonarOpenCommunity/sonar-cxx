// C-COMPATIBILITY: C99 compound literals
void compoundLiteral()
{
  struct foo {int a; char b[2];} structure;
  structure = (struct foo) {x + y, 'a', 0};

  int * array;
  array = (int []) { 1, 2, 3, 4, 5};
}
