package jbe.micro;

import jbe.BenchMark;
import jbe.Execute;

public class InvokeStatic extends BenchMark {

	static int val;
	public InvokeStatic() {
		val = 123;
	}
/*
   0:	iload_0
   1:	iload_1
   2:	iadd
   3:	getstatic	#2; //Field val:I
   6:	iadd
   7:	ireturn
*/
	public static int add(int a, int b) {

		return a+b+val;
	}
		

/*
   9:	iload_2
   10:	iload_3
   11:	iload_1
   12:	invokestatic	#3; //Method add:(II)I
   15:	iadd
   16:	istore_2
*/
	public int test(int cnt) {

		int a = 0;
		int i;

		for (i=0; i<cnt; ++i) {
			a += InvokeStatic.add(i, cnt);
		}
		return a;
	}

/*
   9:	iload_2
   10:	iload_3
   11:	iload_1
   12:	iadd
   13:	getstatic	#2; //Field val:I
   16:	iadd
   17:	iadd
   18:	istore_2
*/
	public int overhead(int cnt) {

		int a = 0;
		int i;

		for (i=0; i<cnt; ++i) {
			a += i+cnt+val;
		}
		return a;
	}


	public String getName() {

		return "invokestatic";
	}

	public static void main(String[] args) {

		BenchMark bm = new InvokeStatic();

		Execute.perform(bm);
	}
			
}
