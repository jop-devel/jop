main() {

	int i;
	for (i='0'; i<'0'+80; ++i) {
		printf("%c", i);
	}
	for (i='0'; i<'0'+80; ++i) {
		printf("%c", i+0x80);
	}
	printf("\n");
	for (i='0'; i<256; ++i) {
		printf("%c %d\n", i, i);
	}
}
