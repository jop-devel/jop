package gctest;

import joprt.RtThread;
import util.Dbg;
import com.jopdesign.sys.*;

// A test of the GCStackWalker
// It calls the gcsw from the main thread, the main thread
//   from the "GC" thread, the "GC" thread from the "GC" thread
// Status: It is supposed to (when ready) return the potential
//   references. It is perhaps a slight improvement over a total
//   stack walk.

// RtThread needs these three methods
//public static int[] getStack(int num){
//	return ref[num].stack;
//}
//
//public static int getSP(int num){
//	return ref[num].sp;
//}
//
//public static int getActive(){
//	return active;
//}

public class GCTest3 {

	public static void main(String s[]) {
    System.out.println("HelloWorld");	  
	  //GCStkWalk.init();
    long l1, l2;
    l1=l2=1;
    long l3 = l1 + l2;      
	  SomeClass1 sc1 = new SomeClass1();
	  SomeClass2 sc2 = new SomeClass2(sc1);
System.exit(0);
	  
	  new RtThread(20, 100000) {
			public void run() {
				for (;;) {
					System.out.println("Walk main from \"GC\" thread");
					GCStkWalk.swk(0,false,true);
          System.exit(0);
					waitForNextPeriod();
				}
			}
		};
		RtThread.startMission();
		for(;;){
			RtThread.sleepMs(1000);
		}
	}
}
class SomeClass1 {
	int i,j;
	SomeClass1 sc1inst;  
	void someMethod(){
		sc1inst = this;
		int ii = i+j;
		SomeClass1 sc1instlocal = this;
		System.out.println("Walk someMethod");
		//It should not be called like this, but we are testing...
		GCStkWalk.swk(0,true,true);
	}

}
class SomeClass2 {
	public SomeClass2(SomeClass1 sc1){
		sc1.someMethod();
	}
}
