int main()
{
	int n = 0;

	int i = 5;
	if (i >= 5) {
		n = n + 3;
	} else {
		n = n + 9;
	}

	if (i <= 5) {
		n = n + 52;
	} else {
		n = n + 33;
	}

	i = 10;
	if (i >= 5) {
		n = n + 6;
	} else {
		n = n + 18;
	}

	if (i <= 5) {
		n = n + 26;
	} else {
		n = n + 17;
	}

	return n;
}