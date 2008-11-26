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

package gcinc;

import java.util.Vector;

import util.Dbg;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.GC;
import com.jopdesign.sys.Native;

import joprt.RtThread;

public class Latency {

	final static String TASK_SET = "hpclg";
	final static int TEST_TIME = 60;
	final static int ARRAY_SIZE = 4096/4;

	final static boolean USE_ARRAY = true;

	static class HFThread extends RtThread {

		public HFThread(int prio, int us) {
			super(prio, us);
			period = us;
		}

		int period;
		int expected;
		int max, min;
		int cnt;
		boolean notFirst;
		
		public void run() {

			for (;;) {
				waitForNextPeriod();
				int t = Native.rdMem(Const.IO_US_CNT);
				if (!notFirst) {
					expected = t+period;
					notFirst = true;
				} else {
					int diff = t-expected;
					if (diff>max) max = diff;
					if (diff<min) min = diff;
//					if (++cnt==1000000) {
//						result();
//					}				
					expected += period;					
				}
				work();
			}
		}
		
		void work() {
			// nothing for the HF thread
		}
		
		void result() {
			Dbg.wr("max=", max);
			Dbg.wr("max=", min);
			for (;;);
		}
	}
	
//	static Vector v;
	static SimpleList sl;
	
	static class MFThread extends HFThread {

		int nr;

		public MFThread(int prio, int us) {
			super(prio, us);
		}
		
		void work() {
			if (USE_ARRAY) {
				sl.append(new int[ARRAY_SIZE]);
			} else {
				sl.append(new Integer(nr));				
			}
//			synchronized (v) {
//				v.addElement(new Integer(nr));				
//			}
			++nr;
		}
	}
	static class PooledMFThread extends HFThread {

		int nr;

		int [][] pool;
		int poolIndex;

		public PooledMFThread(int prio, int us) {
			super(prio, us);
			pool = new int[sl.POOL_SIZE][ARRAY_SIZE];
			poolIndex = 0;
		}
		
		void work() {
			if (USE_ARRAY) {
				sl.appendPooled(pool[poolIndex]);
				poolIndex++;
				if (poolIndex >= sl.POOL_SIZE) {
					poolIndex = 0;
				}
			} else {
				sl.append(new Integer(nr));				
			}
//			synchronized (v) {
//				v.addElement(new Integer(nr));				
//			}
			++nr;
		}
	}
	static class LFThread extends HFThread {

		int expNr;
		
		public LFThread(int prio, int us) {
			super(prio, us);
		}
		
		void work() {
			
			Object o;
			while ((o = sl.remove())!=null) {
				if (!USE_ARRAY) {
					if (((Integer) o).intValue()!=expNr) {
						System.out.println("List problem");					
					}
				} else {
//					int[] ia = (int []) o;
//					if (ia[0]!=expNr) {
//						System.out.println("List problem");											
//					}
				}
				++expNr;					
			}
//			int size;
//			synchronized (v) {
//				size = v.size();
//			}
//			while (size!=0) {
//				Object o;
//				synchronized (v) {
//					o = v.remove(0);
//				}
//				if (((Integer) o).intValue()!=expNr) {
//					System.out.println("Vector problem");					
//				}
//				++expNr;
//				synchronized (v) {
//					size = v.size();
//				}
//			}
		}
	}
	
	static class GCThread extends RtThread {

		public GCThread() {
			super(1, PERIOD_GC);
			GC.setConcurrent();
		}
		
		public void run() {
			for (;;) {
				Dbg.wr('G');
				GC.gc();
				waitForNextPeriod();
			}
		}
	}
	
	static class StackThread extends HFThread {

		public StackThread(int prio, int us) {
			super(prio, us);
		}
		
		void work() {
			for (;;) {
				factorial(5);
			}
		}

		int factorial(int n) {
			if (n > 1) {
				return factorial(n-1)*n;
			} else {
				waitForNextPeriod();
				return 1;
			}
		}
	}

	/**
	 * Use Dbg instead 
	 * @author martin
	 *
	 */
	static class LogThread extends RtThread {
	
		public LogThread(int prio, int us) {
			super(prio, us);
		}

		public void run() {
			int cnt = 0;
			int subCnt = 0;
			for (;;) {				
				waitForNextPeriod();
				// emulate a period of 1 second
				if (++subCnt >= (1000*1000)/PERIOD_LOG) {
					subCnt = 0;
					if (hft!=null) {
						Dbg.wr("hft max=", hft.max);
						Dbg.wr("hft min=", hft.min);
					}

					if (++cnt >= TEST_TIME) {
						synchronized(System.out) {
							System.out.println("JVM exit!");
						}
					}
				}
			}
		}

	}
	
	static HFThread hft;

	// Changes in the scheduler to get low latency values:
	//		TIM_OFF to 2
	//		omit time adjustments on a missed deadline in
	//		waitForNextPeriod()
	// Results for 100 MHz:
	// 		107 is without jitter when running it alone
	// TODO verify the following results:
	// with output thread 10 us
	// with prod/cons threads (no GC) 16 us
	// with GC 72 us (77 us)
	public static final int PERIOD_HIGH   = 107; // 211;
	public static final int PERIOD_MEDIUM = 2003; // 1009;
	public static final int PERIOD_LOW    = 10007; // 10853;
	public static final int PERIOD_STACK  = 15013;
	public static final int PERIOD_LOG    = 25000;
	public static final int PERIOD_GC     = 50021; // 200183;
	
	// for slower JOP versions (<100MHz)
//	public static final int PERIOD_HIGH = 200;
//	public static final int PERIOD_MEDIUM = 2000;
//	public static final int PERIOD_LOW = 20000;
//	public static final int PERIOD_GC = 400000;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Dbg.initSerWait();

//		v = new Vector(20);
		sl = new SimpleList();
		
		if (TASK_SET.indexOf('h') >= 0) {
			hft = new HFThread(6, PERIOD_HIGH);
		}
		if (TASK_SET.indexOf('p') >= 0) {
			new MFThread(5, PERIOD_MEDIUM);
		} else if (TASK_SET.indexOf('q') >= 0) {
			new PooledMFThread(5, PERIOD_MEDIUM);
		} 
		if (TASK_SET.indexOf('c') >= 0) {
			new LFThread(4, PERIOD_LOW);
		}
		if (TASK_SET.indexOf('s') >= 0) {
			new StackThread(3, PERIOD_STACK);
		}
		if (TASK_SET.indexOf('l') >= 0) {
			new LogThread (2, PERIOD_LOG);
		}		
		if (TASK_SET.indexOf('g') >= 0) {
			new GCThread();
		}		

		RtThread.startMission();
		
		// that one is mandatory to get low latency!
		// check RtThreadImpl why.
		for(;;);
	}

}
