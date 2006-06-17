package wcet;

import com.jopdesign.sys.*;

public class Method {

	static int ts, te, to;

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
//		System.out.println(te-ts-to);
	}
	
	static void measure() {
		ts = Native.rdMem(Const.IO_CNT);
		y();
		te = Native.rdMem(Const.IO_CNT);		
	}
	
	static void y() {
		for (int i=0; i<5; ++i) { // @WCA loop=5
			x();		
		}		
	}
	static void x() {

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
}
