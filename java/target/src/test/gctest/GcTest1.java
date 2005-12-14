package gctest;

import joprt.RtThread;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.GC;
import com.jopdesign.sys.Native;

// GcTest1
// Tests if GC collects the garbage object
// It is done by reusing one reference
// The test check if the heap size is constant and
// it checks that the new garbage object has a valid id
// compared to the previous garbage object id.

public class GcTest1 {

	public static void main( String s[] ) {
		
		
		// Firt test to see if GC collects the floating object
		new RtThread(20, 100000) {
			public void run() {
				String allocStr = "Allocation problem!";
				String refStr = "Reference problem";
				
				Garbage garbage = new Garbage();
				
				int prevId = garbage.id;
				
				GC.gc();
				
				int freeHeap = GC.freeMemory(); 
				
				for (;;) {
					
					if(freeHeap != GC.freeMemory())
						System.out.println(allocStr); //
					
                    garbage = new Garbage();
                    
                    if((garbage.id-prevId)!=1)
                    	System.out.println(refStr);
                    
                    prevId = garbage.id;
                    
                    GC.gc(); //Remove the old garbage object
                    
					waitForNextPeriod(); //GC is supposed to run while we are waiting.
				}
			}
		};

		RtThread.startMission();
        
		for(int i=0;i<10;++i){
			RtThread.sleepMs(1000);
		}
		System.exit(-1);
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