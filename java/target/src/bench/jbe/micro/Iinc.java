package jbe.micro;

import jbe.BenchMark;
import jbe.Execute;

public class Iinc extends BenchMark {

/*
   14:	iload_2
   15:	iload_3
   16:	iinc	3, 1
   19:	iadd
   20:	istore_2
*/
	public int test(int cnt) {

		int a = 0;
		int b = 123;
		int i;

		for (i=0; i<cnt; ++i) {
			a = a+(b++);
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

	public String getName() {

		return "iinc";
	}

	public static void main(String[] args) {

		BenchMark bm = new Iinc();

		Execute.perform(bm);
	}
			
}
