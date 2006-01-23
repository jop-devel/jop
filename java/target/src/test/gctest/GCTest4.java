package gctest;

import joprt.RtThread;
import util.Dbg;
import com.jopdesign.sys.*;

// A test of the GCStackWalker

public class GCTest4 {

  public static void main(String s[]) {
	  
	  new RtThread(18, 500000) {
	  	SomeObject so;
			public void run() {
				for (;;) {
					so = new SomeObject(1);
      		for(int i=0;i<1;i++){
      		  System.out.print("Thread 1: new Object:");
//      		  System.out.print(i);
//      		  System.out.println("");
      		  new Object();
      		}
  		    if(so.getTest() != 1){
  		      System.out.println("Thread 1 fail");
            System.out.println(so.getTest());  		     
  		      System.exit(-1);
  		    }else{
  		      System.out.println("Thread 1 ok");
  		    }
					waitForNextPeriod();
				}
			}
		};

	  new RtThread(19, 500000) {
	  	SomeObject so;
			public void run() {
				for (;;) {
					so = new SomeObject(2);
      		for(int i=0;i<1;i++){
      		  System.out.print("Thread 2: new Object:");
//      		  System.out.print(i);
//      		  System.out.println("");
      		  new Object();
      		}
  		    if(so.getTest() != 2){
  		      System.out.println("Thread 2 fail");
            System.out.println(so.getTest());  		     
  		      System.exit(-1);
  		    }else{
  		    	System.out.println("Thread 2 ok");
  		    }
					waitForNextPeriod();
				}
			}
		};
    
		RtThread.startMission();

		for(;;){
	  	SomeObject so; 
	  	so = new SomeObject(0);
  		for(int i=0;i<1;i++){
  		  System.out.print("RtThread 0: new Object:");
//  		  System.out.print(i);
//  		  System.out.println("");
  		  new Object();
  		}

  		if(so.getTest() != 0){
  		   System.out.println("Thread 0 fail");
  		   System.out.println(so.getTest());
  		   System.exit(-1);
  		}else{
  			 System.out.println("Thread 0 ok");
  		}
			RtThread.sleepMs(500);
		}
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

