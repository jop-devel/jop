package jbe.micro;

import jbe.BenchMark;
import jbe.Execute;

public class GetField extends BenchMark {

	public int field;

	public GetField() {

		field = 4711;
	}

	static GetField iv;
	static int stat;



/*
   40:	iload_2
   41:	iload_3
   42:	iadd
   43:	aload	5
   45:	getfield	#2; //Field field:I
   48:	iadd
   49:	istore_2
*/
	public int test(int cnt) {

		int a = 0;
		int b = 123;
		int i;
		if (iv==null) {
			iv = new GetFieldExt();
			stat = 123;
		}
		GetField x = iv;

		for (i=0; i<cnt; ++i) {
			a = a+b+x.field;
		}
		return a;
	}

/*
   35:	iload_2
   36:	iload_3
   37:	iadd
   38:	istore_2
*/
	public int overhead(int cnt) {

		int a = 0;
		int b = 123;
		int i;
		if (iv==null) {
			iv = new GetField();
			stat = 123;
		}

		for (i=0; i<cnt; ++i) {
			a = a+b;
		}
		return a;
	}


	public String getName() {

		return "getfield";
	}

	public static void main(String[] args) {

		BenchMark bm = new GetField();

		Execute.perform(bm);
	}
			
}
