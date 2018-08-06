int foo(int x, int y)
{
  x = x + 4;
  y = y + 8;
  return x + y;
}

int main(int i)
{
  int x;
  x = foo(3, 5);
  return x;
}
