struct Declarators {
  int function(int a);
  int *functionReturningPointer(int b);
  int (parenthesizedFunction)(int c);
  auto trailingReturnFunction(int d) -> int;
  int (*functionPointer)(int e);
  int (&functionReference)(int f);
  int (*functionPointerArray[2])(int g);
  int field;
  int *pointerField;
};
