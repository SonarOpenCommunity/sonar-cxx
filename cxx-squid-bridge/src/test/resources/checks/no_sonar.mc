int a = 0;

/* line 1
   line 2
   NOSONAR line 3
   line 4 NOSONAR this is also a no sonar even if not at the beginning */

void myfunction()
{
  int b = a + 42; /* NOSONAR */
}
