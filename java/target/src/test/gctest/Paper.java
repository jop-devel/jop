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

	static int n[];
	
	static class Worker extends RtThread {
		
		int cnt;
		int wcet;
		
		public Worker(int prio, int period, int wcet, int cnt) {
			super(prio, period);
			this.wcet = wcet;
			this.cnt = cnt;
		}
		public void run() {

	        for (;;) {
	        	System.out.print("W");
	            n = new int[cnt];
	            busyWait(wcet);
	            n = null;
	            if (!waitForNextPeriod()) {
	            	System.out.println("Worker missed deadline");
	            }
	        }
		}
		

	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new Worker(2, 10*1000, 1*1000, 1000/4-1);

		new RtThread(1, 200*1000) {
			public void run() {

				GC.setConcurrent();
				for (;;) {
					System.out.print("G");
					GC.gc();
					waitForNextPeriod();
				}
			}
		};
		
		RtThread.startMission();

		// sleep
		for (int i=0;i<5;++i) {
			System.out.print("M");
			Timer.wd();
			RtThread.sleepMs(1000);
		}
		System.exit(0);
	}

}
