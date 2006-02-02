package gctest;

import joprt.RtThread;
import util.Dbg;
import com.jopdesign.sys.*;

// A test of the GCStackWalker to see of the threads are scheduled. TODO: Do some GC:-)

public class GCTest6 {

  public static void main(String s[]) {
	  
	  new RtThread(18, 500000) {
			public void run() {
				for (;;) {
     		  System.out.print("Thread 1");
     		  waitForNextPeriod();
				}
			}
		};

	  new RtThread(19, 500000) {
			public void run() {
				for (;;) {
          System.out.print("Thread 2");
					waitForNextPeriod();
				}
			}
		};
    
		RtThread.startMission();

		for(int i=0;i<3;i++){
  		System.out.println("Thread 0");
			RtThread.sleepMs(500);
		}
		System.out.println("Test 6 ok");
	} //main
}