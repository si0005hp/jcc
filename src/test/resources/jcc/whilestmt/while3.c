int main(int argc)
{
	int i = 1;

	int x = 0;
	while (x < 5) {
		x = x + 1;

		if (x == 3) {
			continue;
		}

		i = i * 2;
	}

	return i;
}
