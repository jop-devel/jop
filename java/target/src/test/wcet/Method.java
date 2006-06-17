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
		// cnt with var. block cache: 15425
		// cnt with single block cache: 16779
		measure();
		println(te-ts-to);
	}
	static void println(int x) {
		System.out.println(x);
	}
	
	static void measure() {
		ts = Native.rdMem(Const.IO_CNT);
		for (int i=0; i<5; ++i) { // @WCA loop=5
			x();		
		}
		te = Native.rdMem(Const.IO_CNT);		
	}
	static void analyze() {
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
	static void y() {
		int i=0;
		z();
		return;
	}
	
	static void z() {
		int i=0;
		return;
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
