int main(int argc)
{
	int num = 0;

	int i = 3;
	if (i == 3) {
		num = num + 1;
	} else {
		num = num * 2;
	}

	if (i != 3) {
		num = num + 3;
	} else {
		num = num * 4;
	}

	if (i < 3) {
		num = num + 5;
	} else {
		num = num * 6;
	}

	if (i > 3) {
		num = num + 7;
	} else {
		num = num * 8;
	}

	if (i <= 3) {
		num = num + 9;
	} else {
		num = num * 10;
	}

	if (i >= 3) {
		num = num + 11;
	} else {
		num = num * 12;
	}

	return num;
}
