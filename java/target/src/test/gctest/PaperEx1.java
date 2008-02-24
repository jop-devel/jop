/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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

public class PaperEx1 {

	static Object mutex;
	
	// We have to use static data for our experiments because
	// the current GC prototype does not get the roots from the
	// other threads stack frames.
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
					// dump the results
//					GC.dump();
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
		// about memory consumption:
		// allocates an integer array of n*256-1 elements
		// plus the size field results in exact n*1024
		// bytes.
		new Worker(0, 3, 5*1000, 1*1000, 1*1024/4-1);
		new Worker(1, 2, 10*1000, 3*1000, 3*1024/4-1);
		
		// dummy thread to get the same static memory
		// consumption for both examples
		new Worker(2, 0, 1000*1000, 10, 0);
		
		System.out.println("GC Example 1 (two worker threads)");

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
