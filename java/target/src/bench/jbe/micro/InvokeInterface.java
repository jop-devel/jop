package jbe.micro;

import jbe.BenchMark;
import jbe.Execute;

public class InvokeInterface extends BenchMark {

	static If iv;
	static int val;

/*
   30:	iload_2
   31:	getstatic	#2; //Field iv:Ljbe/micro/If;
   34:	iload_3
   35:	iload_1
   36:	invokeinterface	#6,  3; //InterfaceMethod jbe/micro/If.add:(II)I
   41:	iadd
   42:	istore_2
*/
	public int test(int cnt) {

		int a = 0;
		int i;
		if (iv==null) {
			iv = new IfImp();
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
			iv = new IfImpTwo();
			val = 123;
		}

		for (i=0; i<cnt; ++i) {
			a += i+val;
		}
		return a;
	}


	public String getName() {

		return "invokeinterface";
	}

	public static void main(String[] args) {

		BenchMark bm = new InvokeInterface();

		Execute.perform(bm);
	}
			
}
