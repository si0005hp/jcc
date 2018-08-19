int main()
{
	int n = 7;
	int r = foo(&n);
	return r;
}

int foo(int *p) {
	int x = *p + 5;
	return x;
}