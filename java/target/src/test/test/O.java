
package test;

import util.Dbg;

class O {

	public static void main( String s[] ) {

		Dbg.init();
		x();

	}

	private static void x() {

		int i = 123;

		Dbg.wr('o');

		for (;;) ;
	}
}
