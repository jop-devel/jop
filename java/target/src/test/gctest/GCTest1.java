package gctest;

import joprt.RtThread;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.GC;
import com.jopdesign.sys.Native;

// GCTest1
// Tests if GC collects the garbage object. The GC is explicitly invoked.

public class GCTest1 {

	public static void main( String s[] ) {
		new RtThread(20, 1000000) {
			public void run() {
				String allocStr = "Allocation problem!";
				String refStr = "Reference problem";
				
				Garbage garbage = new Garbage();
				int prevId = garbage.id;
				GC.gc();
				int freeHeap = GC.freeMemory(); 
				for (;;) {
					if(freeHeap != GC.freeMemory()){
						System.out.println(allocStr);
					}
          garbage = new Garbage();
                    
          if((garbage.id-prevId)!=1){
           	System.out.println(refStr);
           	System.exit(-1);
          }          
          prevId = garbage.id;
                    
          GC.gc(); //Remove the old garbage object
                    
					waitForNextPeriod(); 
				}
			}
		};

		RtThread.startMission();
        
		for(int i=0;i<10;++i){
			RtThread.sleepMs(1000);
		}
		System.out.println("Test 1 OK");
	}
}

class Garbage {
	static int cnt = 0;
	public int id;
	public Garbage(){
	  cnt++;
	  id = cnt;
		System.out.print("Garbage object id:");
		System.out.println(id);
	}
}