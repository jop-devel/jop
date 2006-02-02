package gctest;

import joprt.RtThread;
import util.Dbg;
import com.jopdesign.sys.*;

// A test of the GCStackWalker. A concurrent test.
// Tests if some threads (three) can keep the reference to an object.
// waitForNextPeriod() is placed between each source line to test for GC problems
// for interrupts.

public class GCTest4 {

  public static void main(String s[]) {
	  
	  new RtThread(18, 100010) {
	  	SomeObject so;
			public void run() {
				for (;;) {
					waitForNextPeriod();
					so = new SomeObject(1);
					waitForNextPeriod();
      		for(int i=0;i<100;i++){
      			waitForNextPeriod();
      		  System.out.print("Thread 1: new Object:");
      		  waitForNextPeriod();
      		  System.out.print(i);
      		  waitForNextPeriod();
      		  System.out.println("");
      		  waitForNextPeriod();
      		  new Object();
      		  waitForNextPeriod();
      		}
      		waitForNextPeriod();
  		    if(so.getTest() != 1){
  		    	waitForNextPeriod();
  		      System.out.println("Thread 1 fail");
  		      waitForNextPeriod();
            System.out.println(so.getTest());  		     
            waitForNextPeriod();
  		      System.exit(-1);
  		      waitForNextPeriod();
  		    }else{
  		    	waitForNextPeriod();
  		      System.out.println("Thread 1 ok");
  		      waitForNextPeriod();
  		    }
					waitForNextPeriod();
				}
			}
		};

	  new RtThread(19, 100000) {
	  	SomeObject so;
			public void run() {
				waitForNextPeriod();
				for (;;) {
					waitForNextPeriod();
					so = new SomeObject(2);
					waitForNextPeriod();
      		for(int i=0;i<1;i++){
      			waitForNextPeriod();
      		  System.out.print("Thread 2: new Object:");
      		  waitForNextPeriod();
      		  System.out.print(i);
      		  waitForNextPeriod();
        		System.out.println("");
        		waitForNextPeriod();
      		  new Object();
      		  waitForNextPeriod();
      		}
      		waitForNextPeriod();
  		    if(so.getTest() != 2){
  		    	waitForNextPeriod();
  		      System.out.println("Thread 2 fail");
  		      waitForNextPeriod();
            System.out.println(so.getTest());
            waitForNextPeriod();  		     
  		      System.exit(-1);
  		    }else{
  		    	waitForNextPeriod();
  		    	System.out.println("Thread 2 ok");
  		    	waitForNextPeriod();
  		    }
					waitForNextPeriod();
				}
			}
		};
		
		new RtThread(1, 100000) {
			public void run() {
				GC.setConcurrent();
				for (;;) {
				  System.out.println("G");
				  GC.gc();
				  if (!waitForNextPeriod()) {
				    System.out.println("GC missed deadline!");
				    System.exit(-1);
				  }
				}
			}
		};
    
		RtThread.startMission();

		for(int j=0;j<3;j++){
	  	SomeObject so; 
	  	so = new SomeObject(0);
  		for(int i=0;i<100;i++){
  		  System.out.print("RtThread 0: new Object:");
  		  System.out.print(i);
  		  System.out.println("");
  		  new Object();
  		}

  		if(so.getTest() != 0){
  		   System.out.println("Thread 0 fail");
  		   System.out.println(so.getTest());
  		   System.exit(-1);
  		}else{
  			 System.out.println("Thread 0 ok");
  		}
			RtThread.sleepMs(5000);
		}
		System.out.println("Test 4 OK");
	} //main
}

class SomeObject {
	int test;
	
	SomeObject(int test){
	  this.test = test;
	}
	
	int getTest(){
		return test;
	}
}