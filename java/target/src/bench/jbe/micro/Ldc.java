package jbe.micro;

import jbe.BenchMark;
import jbe.Execute;

public class Ldc extends BenchMark {

	public int test(int cnt) {

		int a = 0;
		int b = 123;
		int i;

/*
   14:	ldc	#2; //int 12345678
   16:	istore_3
   17:	iload_2
   18:	iload_3
   19:	iadd
   20:	istore_2
*/
		for (i=0; i<cnt; ++i) {
			b = 12345678;
			a += b;
		}
		return a;
	}

	public int overhead(int cnt) {

		int a = 0;
		int b = 123;
		int i;

/*
   14:	iconst_0
   15:	istore_3
   16:	iload_2
   17:	iload_3
   18:	iadd
   19:	istore_2
*/
		for (i=0; i<cnt; ++i) {
			b = 0;
			a += b;
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
