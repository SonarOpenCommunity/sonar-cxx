int myFunction()
{
  if (0) { /* This is acceptable, and must be excluded */
    return 0;
  }

  if (1) return 1; /* This however is not acceptable, NOK */
}
