package embjbench;

class BenchIinc extends BenchMark {

	int test(int cnt) {

		int a = 0;
		int i;

		for (i=0; i<cnt; ++i) {
			++a;
		}
		return a;
	}

	int overhead(int cnt) {

		int a = 0;
		int i;

		for (i=0; i<cnt; ++i) {
		}
		return i;
	}

	String getName() {

		return "iinc";
	}

	public static void main(String[] args) {

		BenchMark bm = new BenchIinc();

		Execute.perform(bm);
for (;;);
	}
			
}
