package embjbench;

class BenchMark {

/*
   14:	iload_2
   15:	iload_3
   16:	iadd
   17:	iload_3
   18:	iadd
   19:	istore_2
*/
	int test(int cnt) {

		int a = 0;
		int b = 123;
		int i;

		for (i=0; i<cnt; ++i) {
			a = a+b+b;
		}
		return a;
	}

/*
   14:	iload_2
   15:	iload_3
   16:	iadd
   17:	istore_2
*/
	int overhead(int cnt) {

		int a = 0;
		int b = 123;
		int i;

		for (i=0; i<cnt; ++i) {
			a = a+b;
		}
		return a;
	}

	int overheadMinus(int cnt) {

		return 0;
	}

	String getName() {

		return "iload_3 iadd";
	}

	public static void main(String[] args) {

		BenchMark bm = new BenchMark();

		Execute.perform(bm);
for (;;);
	}
			
}
