package jbe.micro;

import jbe.BenchMark;
import jbe.Execute;

public class InvokeInterface extends BenchMark {

	If invif;

	public InvokeInterface() {

		invif = new IfImp();
	}
/*
   9:	iload_2
   10:	aload_0
   11:	iload_3
   12:	iload_3
   13:	invokevirtual	#2; //Method add:(II)I
   16:	iadd
   17:	istore_2
*/
	public int test(int cnt) {

		int a = 0;
		int i;

		for (i=0; i<cnt; ++i) {
			a += invif.add(i, i);
		}
		return a;
	}

/*
   9:	iload_2
   10:	iload_3
   11:	iload_3
   12:	iadd
   13:	iadd
   14:	istore_2
*/
	public int overhead(int cnt) {

		int a = 0;
		int i;

		for (i=0; i<cnt; ++i) {
			a += i+i;
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
