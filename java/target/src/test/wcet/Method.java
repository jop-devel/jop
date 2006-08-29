package wcet;

import com.jopdesign.sys.*;

public class Method {

	static int ts, te, to;

	static boolean dummy = true;
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Method m = new Method();
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		// WCET with var. block cache: x
		// WCET with two block cache: 1116
		// WCET analysed: 1312
		measure();
		System.out.println(te-ts-to);
	}

	static void measure() {
		ts = Native.rdMem(Const.IO_CNT);
		for (int i=0; i<10; ++i) { // @WCA loop=10
            if (dummy) {
                b();
            } else {
                d();
            }
		}
		te = Native.rdMem(Const.IO_CNT);
	}

	static void b() {
		int i;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		for (i=0; i<5; ++i) { // @WCA loop=5
			c();
		}
	}
	static void c() {

		int a, b, c, d, e;
		int i = 123;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;

	}
	static void d() {

		int a, b, c, d, e;
		int i = 123;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;

	}
}
