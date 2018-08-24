int foo(int *p)
{
	return *p + 5;
}

int main(int i)
{
	int a = 10;
	int x = foo(&a);
	int b[1] = {20};
	int y = foo(b);
	return x + y;
}
