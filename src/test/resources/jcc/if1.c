int main(int i)
{
	int x = 0;
	if (0) {
		x = 5;
	}

	int y = 0;
	if (0) {
		y = 5;
	} else {
		y = 10;
	}

	int z = 0;
	if (1) {
		z = 5;
	} else {
		z = 10;
	}

	return x + y + z;
}
