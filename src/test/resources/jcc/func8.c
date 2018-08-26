int factrial(int n) {
	if (n < 2) {
		return n;
	}
	return n * factrial(n - 1);
}

int main() {
	int n = factrial(6);
	printf("%d\n", n);
  	return 0;
}
