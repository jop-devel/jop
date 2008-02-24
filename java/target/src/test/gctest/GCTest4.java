/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006, Rasmus Ulslev Pedersen

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
	  	SomeObject2 so;
			public void run() {
				for (;;) {
					waitForNextPeriod();
					so = new SomeObject2(1);
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
	  	SomeObject2 so;
			public void run() {
				waitForNextPeriod();
				for (;;) {
					waitForNextPeriod();
					so = new SomeObject2(2);
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
	  	SomeObject2 so; 
	  	so = new SomeObject2(0);
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

class SomeObject2 {
	int test;
	
	SomeObject2(int test){
	  this.test = test;
	}
	
	int getTest(){
		return test;
	}
}