int main(int i)
{
	return foo(0);
}

int foo(int z) {
	int x = 0;
	int y = 0;

	if (1) {
		x = 5;
		y = 7;
		z = 9;
		if (0) {
		} else {
			x = 10;
			if (0) {
			} else if (0) {
			} else {
				y = 15;
			}
		}
	}
	return x + y + z;
}
