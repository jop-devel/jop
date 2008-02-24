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
import util.Timer;

// GC test 7. Allocation of one local referenced object. Testing if the object 
// survives the concurrent collection (it must).

public class GCTest7 {
  public static void main(String s[]) {
  	
	  new RtThread(2, 5000000) {
			public void run() {
				int c=0;
				for (;;) {
					c++;
					System.out.println("T1");
					SomeObject so = new SomeObject(c);
      		for(int i=0;i<5000;i++){
      		  System.out.print("T1 ");
      		  new Object();
      		}
      		System.out.println("");
      		waitForNextPeriod();
  		    System.out.println("T2");
  		    if(so.getTest() != c){
  		      System.out.println("Thread 1 fail");
            System.out.println(so.getTest());  		     
  		      System.exit(-1);
  		    }else{
  		      System.out.println("Thread 1 ok");
  		    }
  		    //waitForNextPeriod();
					
				}
			}
		};
		
   	new RtThread(1, 5000000) {
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

		for(int i=0;i<5;i++){
			RtThread.sleepMs(2000);
		  System.out.println("M");
			Timer.wd();
		}
		System.out.println("Test 7 ok");
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