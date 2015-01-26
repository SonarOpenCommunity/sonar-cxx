int five()
{
  int lines = 1
  lines++;
  //comment doesn't count
  if (lines == 2) {
    lines +=2;
    }
  return lines+1;
}
int six()
{
  int lines = 1;
  lines++;
  if (lines == 2) 
   { lines +=2; }
  int extra =1;
  return lines + ++extra;
}