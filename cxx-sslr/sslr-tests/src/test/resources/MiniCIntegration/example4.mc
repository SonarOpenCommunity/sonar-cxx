/* Two implemntations of Fibonacci are given here */

int recursiveFibonacci(int n)
{
  if (n <= 1) return n;
  else
  {
    return recursiveFibonacci(n - 1) + recursiveFibonacci(n - 2);
  }
}

int iterativeFibonacci(int n)
{
  int f2 = 0;
  int f1 = 1;
  int i;

  i = 0;
  while (i++ < n)
  {
    int oldF2 = f2;
    f2 = f1;
    f1 = oldF2 + f1;
  }

  return f2;
}
