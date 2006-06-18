package wcet;

import com.jopdesign.sys.*;

public class SimpleMethod2 {

	static int ts, te, to;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		// measurement + return takes 22+22+21=65 cycles
		// WCET measured: 211
		// WCET analysed: 289-65 = 224
		// diff is 13 cycles:
		//	10 from wrong invoke foo 85 instead of 75
		//  2 from wrong invoke xxx 77 instead of 75
		//	1 ???
		measure();
//		System.out.println(te-ts-to);
	}
	
	static void measure() {
		ts = Native.rdMem(Const.IO_CNT);
		foo();
		te = Native.rdMem(Const.IO_CNT);		
	}
	
	static int foo() {
		xxx();
		return 123;
	}
	static int xxx() {
		return 456;
	}
	
}
