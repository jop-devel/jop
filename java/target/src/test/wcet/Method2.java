package wcet;

import com.jopdesign.sys.*;

public class Method2 {

	static int ts, te, to;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Method2 m = new Method2();
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		// WCET with var. block cache: x
		// WCET with two block cache: 15542
		// WCET analysed: 16738
		measure();
		System.out.println(te-ts-to);
	}
	
	static void measure() {
		ts = Native.rdMem(Const.IO_CNT);
		foo();		
		te = Native.rdMem(Const.IO_CNT);		
	}
	
	
	static void foo() {
		
		for (int i=0; i<10; ++i) { // @WCA loop=10
			a();
			b();
		}
	}

	static void a() {
		
		int val = 123;
		for (int i=0; i<10; ++i) { // @WCA loop=10
			val *= val;
		}
	}

	static void b() {
		
		int val = 123;
		for (int i=0; i<5; ++i) { // @WCA loop=5
			val += c();
		}
		for (int i=0; i<5; ++i) { // @WCA loop=5
			val += val;
		}
	}
	
	static int c() {
		
		return 456;
	}
}
