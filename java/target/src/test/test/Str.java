package test;

import util.Dbg;

/**
*	Str.java ... test internal strings
*/

public class Str {

	public static void main(String[] args) {

		int i, j, k;

		char[] x = new char[10];

		x[0] = 'a';

		Dbg.initSer();

		f("ABC");
		f("XYZ0123");
		f("ABC");
		f("AB");
		f("uni\u2297xyz");
		f("last");
		f(new String());

		Dbg.wr('a');

		for (;;) ;
	}

	static void f(String s) {

		int i = s.length();
		for (int j=0; j<i; ++j) {
			Dbg.wr(s.charAt(j));
		}
	}
}
