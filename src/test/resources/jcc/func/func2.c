int foo() {
	int i;
	i = bar() + 1;
	return i;
}

int main(int argc)
{
	int i;
	i = foo() + 1;
	return i;
}

int bar() {
	int i;
	i = 9;
	return i;
}