int main() {
	int a[] = { 1, 2, 3 };
	int *p = a;

	*p = 10;
	p = p + 1;
	*p = 20;
	p = p + 1;
	*p = 30;

	return foo(a);
}

int foo(int *p) {
	int n = 0;
	n = n + *p;
	p = p + 1;
	n = n + *p;
	p = p + 1;
	n = n + *p;
	return n;
}
