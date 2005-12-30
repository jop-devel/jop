/*
 * Created on 09.12.2005
 *
 */
package gctest;

import util.Timer;
import joprt.RtThread;

import com.jopdesign.sys.GC;

public class Periodic {

	static final int SIZE = 1000;
//	static int[] ia;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		new RtThread(2, 50000) {
			public void run() {

				int i, iteration = 0;
//				This does not work because we don't get the
//				roots from the other threads!
				int [] ia = null;
				
				waitForNextPeriod();

				for (;;) {
					if (ia!=null) {
						for (i=0; i<SIZE; ++i) {
							if (ia[i] != iteration*SIZE + i) {
								System.out.println("GC Error");
								System.exit(-1);
							}
						}
					}
					++iteration;
					System.out.print("Alloc");
					ia = new int[SIZE];
					for (i=0; i<SIZE; ++i) {
						ia[i] = iteration*SIZE + i;
					}
					waitForNextPeriod();
				}
			}
		};

		new RtThread(3, 20000) {
			public void run() {

				for (;;) {
					System.out.print("m=");
					System.out.print(GC.freeMemory());
					waitForNextPeriod();
				}
			}
		};

		//
		// GC thread
		//
		new RtThread(1, 500000) {
			public void run() {

				GC.setConcurrent();
				for (;;) {
					System.out.print("G");
					GC.gc();
					waitForNextPeriod();
				}
			}
		};

		GC.gc();
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
