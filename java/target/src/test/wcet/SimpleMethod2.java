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
		// WCET analysed: 278-65 = 213
		// diff is 2 cycles:
		//	    As the invokestatic of the native methods get substituted
		//	    by the special bytecode, the method length gets shorter (1 byte
		//	    for the special bytecode instead of 3 bytes for the
		//	    invokestatic). Therefore, foo() is now 5 words instead of the
		//	    original 6 words. That means the miss from the xxx() return is 2
		//	    cycles less then in the original, analyzed method.
		measure();
//		System.out.println(te-ts-to);
	}
	
	public static void measure() {
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
