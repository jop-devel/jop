package jbe.micro;

import jbe.BenchMark;
import jbe.Execute;

public class Iinc extends BenchMark {

	public int test(int cnt) {

		int a = 0;
		int i;

		for (i=0; i<cnt; ++i) {
			++a;
		}
		return a;
	}

	public int overhead(int cnt) {

		int a = 0;
		int i;

		for (i=0; i<cnt; ++i) {
		}
		return i;
	}

	public String getName() {

		return "iinc";
	}

	public static void main(String[] args) {

		BenchMark bm = new Iinc();

		Execute.perform(bm);
	}
			
}
