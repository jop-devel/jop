package jbe.micro;

import jbe.BenchMark;
import jbe.Execute;

public class Array extends BenchMark {

	volatile static int[] arr;
	volatile static int abc;

	public Array() {

		arr = new int[1024];
		for (int i=0; i<100; ++i) arr[i] = 27+i;
		abc = 123;
	}

	public int test(int cnt) {

		int a = 0;
		int i;

/*
   9:	iload_2
   10:	getstatic	#2; //Field arr:[I
   13:	iload_3
   14:	sipush	1023
   17:	iand
   18:	iaload
   19:	iadd
   20:	istore_2
   21:	iinc	3, 1
*/
		for (i=0; i<cnt; ++i) {
			a += arr[i&0x3ff];
		}
		return a;
	}

	public int overhead(int cnt) {

		int a = 0;
		int i;

/*
   9:	iload_2
   10:	getstatic	#3; //Field abc:I
   13:	sipush	1023
   16:	iand
   17:	iadd
   18:	istore_2
   19:	iinc	3, 1
*/
		for (i=0; i<cnt; ++i) {
			a += abc&0x3ff;
		}
		return a;
	}


	public String getName() {

		return "iaload";
	}

	public static void main(String[] args) {

		BenchMark bm = new Array();

		Execute.perform(bm);
	}
			
}
