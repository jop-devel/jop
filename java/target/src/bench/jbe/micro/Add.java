package jbe.micro;

import jbe.BenchMark;
import jbe.Execute;

public class Add extends BenchMark {

/*
   14:	iload_2
   15:	iload_3
   16:	iadd
   17:	iload_3
   18:	iadd
   19:	istore_2
*/
	public int test(int cnt) {

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
	public int overhead(int cnt) {

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

	public String getName() {

		return "iload_3 iadd";
	}

	public static void main(String[] args) {

		Add bm = new Add();

		Execute.perform(bm);
	}
			
}
