package jbe.micro;

import jbe.BenchMark;
import jbe.Execute;

public class GetStatic extends BenchMark {

	public GetStatic() {

		stat = 4711;
	}

	public static int stat;



/*
   14:	iload_2
   15:	iload_3
   16:	iadd
   17:	getstatic	#2; //Field stat:I
   20:	iadd
   21:	istore_2
*/
	public int test(int cnt) {

		int a = 0;
		int b = 123;
		int i;

		for (i=0; i<cnt; ++i) {
			a = a+b+stat;
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

		return "getstatic";
	}

	public static void main(String[] args) {

		BenchMark bm = new GetStatic();

		Execute.perform(bm);
	}
			
}
