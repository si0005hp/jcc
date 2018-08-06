int main(int i)
{
	int x = 0;
	if (0) {
		x = 5;
	}

	int y = 0;
	if (0) {
		x = 5;
	} else {
		x = 10;
	}

	int z = 0;
	if (1) {
		x = 5;
	} else {
		x = 10;
	}

	return x + y + z;
}
