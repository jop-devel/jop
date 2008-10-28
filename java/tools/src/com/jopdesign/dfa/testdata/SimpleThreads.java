package com.jopdesign.dfa.testdata;

import joprt.RtThread;

public class SimpleThreads {

	static X x;
	static Object obj;
	
	public static void main(String [] args) {
		
		RtThread t1 = new T1(1, 1000);
		RtThread t2 = new T2(2, 1000);

		RtThread.startMission();
	}
}

class X {
	void foo() {}
}
class Y extends X {
	void foo() {
		SimpleThreads.obj = SimpleThreads.x;
	}
}
class Z extends X {}

class T1 extends RtThread {
	
	public T1(int prio, int us) {
		super(prio, us);
	}
	
	public void run() {
		SimpleThreads.x = new Y();
		SimpleThreads.x.foo();
	}
}

class T2 extends RtThread {	

	public T2(int prio, int us) {
		super(prio, us);
	}

	public void run() {
		SimpleThreads.x = new Z();
	}
}

class T3 extends RtThread {	

	public T3(int prio, int us) {
		super(prio, us);
	}

	public void run() {
		SimpleThreads.x = new X();
	}
}	
