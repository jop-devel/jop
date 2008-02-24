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

import com.jopdesign.sys.Const;
import com.jopdesign.sys.GC;
import com.jopdesign.sys.Native;

import joprt.RtThread;

public class Issue {

	static class HFThread extends RtThread {

		public HFThread(int prio, int us) {
			super(prio, us);
			period = us;
		}

		int period;
		int expected;
		int max, min;
		int cnt;
		
		public void run() {
			int t;

			waitForNextPeriod();
			t = Native.rdMem(Const.IO_US_CNT);
			expected = t+period;
			
			for (;;) {
				waitForNextPeriod();
				t = Native.rdMem(Const.IO_US_CNT);
				int diff = t-expected;
				if (diff>max) {
					max = diff;
				} else if (diff<min) {
					min = diff;
				}
				if (++cnt==1000000) {
					result();
				}				
				expected = expected+period;
				work();
			}
		}
		
		void work() {
			// nothing for the HF thread
		}
		
		void result() {
			System.out.println("max="+max);
			System.out.println("min="+min);
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
			sl.append(new Integer(nr));
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
				if (((Integer) o).intValue()!=expNr) {
					System.out.println("List problem");					
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
				System.out.print("G");
				GC.gc();
				waitForNextPeriod();
			}
		}
	}
	
	static class LogThread extends RtThread {
	
		public LogThread(int prio, int us) {
			super(prio, us);
		}

		public void run() {
			for (;;) {
				waitForNextPeriod();
				System.out.println();
				if (hft!=null) {
					System.out.print("hft max=");
					System.out.println(hft.max);
					System.out.print("hft min=");
					System.out.println(hft.min);					
					
				}
				if (mft!=null) {
					System.out.print("mft max=");
					System.out.println(mft.max);
					System.out.print("mft min=");
					System.out.println(mft.min);											
				}
			}
		}

	}
	
	static HFThread hft;
	static MFThread mft;

	// 200 is without jitter when running it alone
	// 500 without jitter when a second dummy thread runs
	// change to 200us on the 100 MHz version
	// at 100 MHz, RtThreadImp TIM_OFF at 2:
	// 200 us without jitter when run alone
	// with output thread 10 us
	// with prod/cons threads (no GC) 16 us
	// with GC 72 us (77 us)
//	public static final int PERIOD_HIGH = 30;
//	public static final int PERIOD_MEDIUM = 500;
//	public static final int PERIOD_LOW = 10000;
//	public static final int PERIOD_GC = 200000;
	public static final int PERIOD_HIGH = 107;
	public static final int PERIOD_MEDIUM = 1009;
	public static final int PERIOD_LOW = 10853;
	public static final int PERIOD_GC = 200183;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

//		v = new Vector(20);
		sl = new SimpleList();
		
		hft = new HFThread(5, PERIOD_HIGH);
		mft = new MFThread(4, PERIOD_MEDIUM);
		new LFThread(3, PERIOD_LOW);
		
		new GCThread();
		
		new LogThread (2, 1000*1000);
				
		RtThread.startMission();
		
		// that one is mandatary to get low latency!
		// check RtThreadImpl why.
		for (;;);
	}

}
