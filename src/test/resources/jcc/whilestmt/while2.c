int main(int argc)
{
	int i = 1;

	int x = 0;
	while (1) {
		i = i * 2;
		x = x + 1;

		if (x == 5) {
			break;
		}
	}

	return i;
}
