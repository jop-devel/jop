package wcet;

import joprt.RtThread;
import wcet.lift.LiftControl;
import wcet.lift.TalIo;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Dispatch {

	static int ts, te, to;


	public static void main(String[] args) {

		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		A a = new A();
		B b = new B();
		C c = new C();
		
//		measure(a);
//		System.out.println(te-ts-to);
//		measure(b);
//		System.out.println(te-ts-to);
//		measure(c);
//		System.out.println(te-ts-to);
	}
	static void measure(A x) {
		ts = Native.rdMem(Const.IO_CNT);
		x.loop();
		te = Native.rdMem(Const.IO_CNT);		
	}
	
	static class A {
		
		void loop() {
			int val = 123;
			for (int i=0; i<10; ++i) { // @WCA loop=10
				val *= i;
			}
		}
	}
	static class B extends A {
		
		void loop() {
			int val = 123;
			for (int i=0; i<100; ++i) { // @WCA loop=100
				val *= i;
			}
		}
	}
	static class C extends B {
		
		void loop() {
			int val = 123;
			for (int i=0; i<1000; ++i) { // @WCA loop=1000
				val *= i;
			}
		}
	}

}
