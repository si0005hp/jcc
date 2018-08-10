int main(int argc)
{
	int l = 1;
	l = l << 1;
	l = l << 2 << 3;

	int r = l >> 1 >> 2 >> 3;

	return l + r;
}