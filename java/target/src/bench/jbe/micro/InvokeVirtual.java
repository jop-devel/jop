package jbe.micro;

import jbe.BenchMark;
import jbe.Execute;

public class InvokeVirtual extends BenchMark {

	static InvokeVirtual iv;
	static int val;


/*
   0:	iload_1
   1:	iload_2
   2:	iadd
   3:	ireturn
*/
	public int add(int a, int b) {

		return a+b;
	}
		

/*
   30:	iload_2
   31:	getstatic	#2; //Field iv:Ljbe/micro/InvokeVirtual;
   34:	iload_3
   35:	iload_1
   36:	invokevirtual	#6; //Method add:(II)I
   39:	iadd
   40:	istore_2
*/
	public int test(int cnt) {

		int a = 0;
		int i;
		if (iv==null) {
			iv = new InvokeVirtualExt();
			val = 123;
		}

		for (i=0; i<cnt; ++i) {
			a += iv.add(i, cnt);
		}
		return a;
	}

/*
   30:	iload_2
   31:	iload_3
   32:	getstatic	#5; //Field val:I
   35:	iadd
   36:	iadd
   37:	istore_2
*/
	public int overhead(int cnt) {

		int a = 0;
		int i;
		if (iv==null) {
			iv = new InvokeVirtual();
			val = 123;
		}

		for (i=0; i<cnt; ++i) {
			a += i+val;
		}
		return a;
	}


	public String getName() {

		return "invoke";
	}

	public static void main(String[] args) {

		BenchMark bm = new InvokeVirtual();

		Execute.perform(bm);
	}
			
}
