package jbe.micro;

import jbe.BenchMark;
import jbe.Execute;

public class BranchTaken extends BenchMark {

/*
   18:	iload_2
   19:	iload_1
   20:	if_icmplt	28
   23:	iload_3
   24:	iload	4
   26:	iadd
   27:	istore_3
   28:	iload_3
   29:	iload	5
   31:	iadd
   32:	istore_3
*/
	public int test(int cnt) {

		int i;
		int a = 0;
		int b = 123;
		int c = 456;

		for (i=0; i<cnt; ++i) {
			if (i>=cnt) {
				a = a+b;
			}
			a = a+c;
		}
		return a;
	}

/*
   18:	iload_3
   19:	iload	5
   21:	iadd
   22:	istore_3
*/
	public int overhead(int cnt) {

		int i;
		int a = 0;
		int b = 123;
		int c = 456;

		for (i=0; i<cnt; ++i) {
			a = a+c;
		}
		return a;
	}

	public String getName() {

		return "if_icmplt taken";
	}

	public static void main(String[] args) {

		BenchMark bm = new BranchTaken();

		Execute.perform(bm);
	}
			
}
