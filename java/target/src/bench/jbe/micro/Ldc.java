package jbe.micro;

import jbe.BenchMark;
import jbe.Execute;

public class Ldc extends BenchMark {

/*
   14:	iload_2
   15:	iload_3
   16:	iadd
   17:	ldc	#2; //int 12345678
   19:	iadd
   20:	istore_2
*/
	public int test(int cnt) {

		int a = 0;
		int b = 123;
		int i;
		for (i=0; i<cnt; ++i) {
			a = a+b+12345678;
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

		return "ldc";
	}

	public static void main(String[] args) {

		BenchMark bm = new Ldc();

		Execute.perform(bm);
	}
			
}
