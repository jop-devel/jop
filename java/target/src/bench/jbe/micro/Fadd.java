package jbe.micro;

import jbe.BenchMark;
import jbe.Execute;

public class Fadd extends BenchMark {

/*
    FLOAD 2
    FLOAD 3
    FADD
    FLOAD 3
    FADD
    FSTORE 2
*/
	public int test(int cnt) {

		float a = 0;
		float b = 123;
		int i;

		for (i=0; i<cnt; ++i) {
			a = a+b+b;
		}
		return (int) a;
	}

/*
    FLOAD 2
    FLOAD 3
    FADD
    FSTORE 2
*/
	public int overhead(int cnt) {

		float a = 0;
		float b = 123;
		int i;

		for (i=0; i<cnt; ++i) {
			a = a+b;
		}
		return (int) a;
	}

	int overheadMinus(int cnt) {

		return 0;
	}

	public String getName() {

		return "fload_3 fadd";
	}

	public static void main(String[] args) {

		Fadd bm = new Fadd();

		Execute.perform(bm);
	}
			
}
