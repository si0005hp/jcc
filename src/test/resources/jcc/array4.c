int main()
{
	int a[] = {3};
	int *x = a;

	int b[] = {5,7};
	int *y = b + 1;

	int c[] = {9,10,11};
	int *z = c;
	int *zz = c + 1;

	return *x + *y + *z + *zz;
}
