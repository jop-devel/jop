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

// A test of the GCStackWalker
// It looks for 3 references in the main method. It is done both from main
// and from another thread. It is a kind of regression test.

public class GCTest3 {

	public static void main(String s[]) {
    System.out.println("Starting GCTest3");	  
      
	  SomeClass1 sc1 = new SomeClass1();
	  SomeClass2 sc2 = new SomeClass2(sc1);
		
    new RtThread(20,1000000){
    	public void run(){
		    for(int i=0;i<3;i++){
          System.out.println("T1");
			    // Call from other thread
			    int roots[] = GCStkWalk.swk(0,false,true);
			    // using javap -c -verbose GCTest3 reveals that the refs are in slot 0,1,2
			    // the offset is 8 from looking at the output
			    if(roots[8+0]!=1 ||roots[8+1]!=1 ||roots[8+2]!=1){
				    System.out.println("Root refs not found correctly");
				    System.exit(-1);
			    }
    	    
    	    waitForNextPeriod();
    	  }
    	  System.out.println("Test OK from thread");
    	}
    };
		
		RtThread.startMission();
		
		for(int i=0;i<3;i++){
			System.out.println("M");
			// Call from active thread
			int roots[] = GCStkWalk.swk(0,true,true);
			// using javap -c -verbose GCTest3 reveals that the refs are in slot 0,1,2
			// the offset is 8 from looking at the output
			if(roots[8+0]!=1 ||roots[8+1]!=1 ||roots[8+2]!=1){
				System.out.println("Root refs not found correctly");
				System.exit(-1);
			}
			RtThread.sleepMs(1000);
		}
		System.out.println("Test OK from main");
	}
}

class SomeClass1 {
	int i,j;
	SomeClass1 sc1inst;  
	void someMethod(){
		sc1inst = this;
		int ii = i+j;
		SomeClass1 sc1instlocal = this;
		System.out.println("someMethod");
	}

}
class SomeClass2 {
	public SomeClass2(SomeClass1 sc1){
		sc1.someMethod();
	}
}