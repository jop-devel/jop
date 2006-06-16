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
		ts = Native.rdMem(Const.IO_CNT);
		// cnt with var. block cache: 15425
		// cnt with single block cache: 16779
		int i=0;
		c();
		te = Native.rdMem(Const.IO_CNT);
		System.out.println(te-ts-to);
	}
	
	void x() {
		int i=0;
		i=1;
		y();
//		te = aba(0);
		int[] x = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
		return;
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
static int aba(int x) { return x;};
	
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
