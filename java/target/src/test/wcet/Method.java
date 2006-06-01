package wcet;

import com.jopdesign.sys.*;

public class Method {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int ts, te, to;
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		System.out.println(to);
		ts = Native.rdMem(Const.IO_CNT);
		// cnt with var. block cache: 15425
		// cnt with single block cache: 16779
		foo();
		te = Native.rdMem(Const.IO_CNT);
		System.out.println(te-ts-to);
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
