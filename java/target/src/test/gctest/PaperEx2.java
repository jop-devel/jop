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

public class PaperEx2 {

	static Object mutex;
	
	// We have to use static data for our experiments because
	// the current GC prototype does not get the roots from the
	// other threads stack frames.
	static class Data {
		int[] n;	// the data
		Data next;	// a simple list for the producer/consumer
	}
	// used by the worker
	static Data da[];
	// references used by producer and consumer
	static Data prod, cons;
	
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

	static class Producer extends Worker {
		
		public Producer(int nr, int prio, int period, int wcet, int cnt) {
			super(nr, prio, period, wcet, cnt);
		}

		public void run() {

	        for (;;) {
	        	System.out.print(ch);
	            da[nr].n = new int[cnt];
	            busyWait(wcet);
	            // synchronize list access between producer
	            // and consumer
	            synchronized (mutex) {
	            	// we also avoid with this int. disabeling
	            	// that the GC ignores the local reference d.
	            	// However, as the GC runs at a lower
	            	// priority there is no real problem.
	            	Data d = new Data();
	            	d.n = da[nr].n;
	            	d.next = prod;
	            	prod = d;
				}
	            da[nr].n = null;
	            if (!waitForNextPeriod()) {
	            	System.out.println("Producer missed deadline!");
	            }
	        }
		}
	}

	static class Consumer extends Worker {
		
		public Consumer(int nr, int prio, int period, int wcet, int cnt) {
			super(nr, prio, period, wcet, cnt);
		}

		public void run() {

	        for (;;) {
	        	System.out.print(ch);
	        	synchronized (mutex) {
	        		cons = prod;	// take the current data
	        		prod = null;	// from the producer list
				}
	        	busyWait(wcet);
	        	// set the list free
	        	cons = null;
	            if (!waitForNextPeriod()) {
	            	System.out.println("Consumer missed deadline!");
	            }
	        }
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int i;

		new RtThread(1, 63*1000) {
			public void run() {

				
				GC.setConcurrent();
				for (int i=0; i<30; ++i) {
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
		prod = null;
		cons = null;
		for (i=0; i<da.length; ++i) {
			da[i] = new Data();
		}
		// about memory consumption:
		// allocates an integer array of n*256-1 elements
		// plus the size field results in exact n*1024
		// bytes.
		// Producer allocates two more words => -3
		new Producer(0, 4, 5*1000, 500, 1*1024/4-3);
		new Worker(1, 3, 10*1000, 3*1000, 3*1024/4-1);
		new Consumer(2, 2, 30*1000, 2*1000, 0);
		
		System.out.println("GC Example 2 (producer/consumer + worker thread)");

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
