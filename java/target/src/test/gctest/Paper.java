/*
 * Created on 13.12.2005
 *
 */
package gctest;

import util.Timer;
import joprt.RtThread;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.GC;
import com.jopdesign.sys.Native;

public class Paper {

	static Object mutex;
	
	// We have to use static data for our experiments because
	// the current GC does not get the roots from the other
	// threads stack frames.
	static class Data {
		int[] n;
	}
	static Data da[];
	
	static class Worker extends RtThread {
		
		int cnt;
		int wcet;
		int nr;
		char ch;
		
		public Worker(int nr, int prio, int period, int wcet, int cnt) {
			super(prio, period);
			this.wcet = wcet;
			this.cnt = cnt;
			this.nr = nr;
			ch = (char) ('0'+nr);
		}
		public void run() {

	        for (;;) {
	        	System.out.print(ch);
	            da[nr].n = new int[cnt];
	            busyWait(wcet);
	            da[nr].n = null;
	            if (!waitForNextPeriod()) {
	            	System.out.println("Worker missed deadline!");
	            }
	        }
		}
		

	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int i;

		new RtThread(1, 85*1000) {
			public void run() {

				
				GC.setConcurrent();
				for (int i=0; i<5; ++i) {
					System.out.print("G");
//					int ts;
//					synchronized (mutex) {
//						ts = Timer.us();
						GC.gc();
//						ts = Timer.us()-ts;
//					}
//					System.out.print("GC took ");
//					System.out.print(ts);
//					System.out.println(" us");
//					System.out.print("g");
					if (!waitForNextPeriod()) {
						System.out.println("GC missed deadline!");
					}
				}
				synchronized (mutex) {
					GC.dump();
					System.exit(0);
				}
			}
		};
		
		// initialize static data
		mutex = new Object();
		da = new Data[3];
		for (i=0; i<da.length; ++i) {
			da[i] = new Data();
		}
		new Worker(0, 3, 5*1000, 1*1000, 1*1024/4-1);
		new Worker(1, 2, 10*1000, 3*1000, 3*1024/4-1);
		
		// dummy thread to get the same static memory
		// consumption for both examples
		new Worker(2, 0, 1000*1000, 10, 0);

		RtThread.startMission();

		// sleep
		for (i=0;i<3*2;++i) {
			System.out.print("M");
			Timer.wd();
			RtThread.sleepMs(500);
		}
		System.exit(0);
	}

}
