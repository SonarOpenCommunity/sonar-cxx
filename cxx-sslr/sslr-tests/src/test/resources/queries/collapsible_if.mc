int foo()
{
  int a;
  int b;

  /* Non-Compliant */
  if (a > 0)
  {
    if (b > 0)
    {
      /* ... */
    }
  }

  /* Non-Compliant */
  if (a > 0)
    if (b > 0) { /* ... */ }

  /* Compliant */
  if (a > 0)
    if (b > 0) { /* ... */ }
    else {}

  /* Compliant */
  if (a > 0)
  {
    if (b > 0)
    {
      /* ... */
    }
  }
  else
  {
  }

  /* Compliant */
  if (a > 0)
  {
    if (b > 0)
    {
      /* ... */
    }
    else
    {
    }
  }

  /* Compliant */
  if (a > 0)
  {
    int c = 0;

    if (b)
    {
      /* ... */
    }
  }

  /* Compliant */
  if (a > 0)
  {
    int foo;
  }
}
